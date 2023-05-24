package net.edulive.janus.signaling_server;


import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import net.edulive.janus.java_client.JanusClient;
import net.edulive.janus.java_client.videoroom.VideoRoomAdaptor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class MessageHandler {

    public MessageHandler(String address) {
        janusClient = new JanusClient((janusSessionId, message) -> {
            String userId = janusToUserMap.get(janusSessionId);
            if (userId != null) {
                userContexts.get(userId).send(message);
            }
            return true;
        }, address);
        janusClient.connect();
        videoRoomAdaptor = new VideoRoomAdaptor(janusClient);
        Thread t = new Thread(new WatchDog(this.userContexts));
        t.start();
    }

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final JanusClient janusClient;
    private final VideoRoomAdaptor videoRoomAdaptor;
    private final Map<Long, String> janusToUserMap = new ConcurrentHashMap<>();
    private final Map<String, Long> userToJanus = new ConcurrentHashMap<>();
    private final Map<Long, Long> sessionToHandle = new ConcurrentHashMap<>();
    private final Map<String, WsConnectContext> userContexts = new ConcurrentHashMap<>();

    public void handleMessage(String user, WsMessageContext context) {
        logger.debug(context.message());
        JSONObject json = new JSONObject(context.message());
        if (!json.has("type")) {
            return;
        }
        Long janusSessionId = userToJanus.get(user);
        switch (json.getString("type")) {
            case "join_room":
                handleJoinRoom(user, context, json, janusSessionId);
                break;
            case "create_room":
                long roomName = videoRoomAdaptor.createRoom(janusSessionId, sessionToHandle.get(janusSessionId));
                context.send(new JSONObject().put("type","create_room_result").put("room_name", roomName));
                break;
            case "publish_stream":
                handlePublishStream(context, json, janusSessionId);
                break;
            case "subscribe_stream":
                List<Object> objectList = json.getJSONArray("streams").toList();
                long[] streams = new long[objectList.size()];
                for (int i = 0; i < objectList.size(); i++) {
                    streams[i] = ((Long) objectList.get(i)).longValue();
                }
                JSONObject result = videoRoomAdaptor.subscriptStream(janusSessionId,
                        sessionToHandle.get(janusSessionId),
                        streams);
                context.send(result);
                break;
            case "connection_info":
                if (!json.has("ice_candidate")) {
                    break;
                }
                videoRoomAdaptor.sendConnectionInfo(janusSessionId,
                        sessionToHandle.get(janusSessionId),
                        json.getJSONArray("ice_candidate"));
                break;
            case "unpublish":
                videoRoomAdaptor.stopPublishStream(janusSessionId, sessionToHandle.get(janusSessionId));
                break;
            case "leave_room":
                videoRoomAdaptor.leaveRoom(janusSessionId, sessionToHandle.get(janusSessionId));
                break;
            case "ping":
                context.send(new JSONObject().put("type", "pong"));
                break;
            default:
                context.send(new JSONObject().put("type", "error").put("error", "Unsupported message"));
        }
    }

    private void handlePublishStream(WsMessageContext context, JSONObject json, Long janusSessionId) {
        if (!json.has("sdp")) {
            return;
        }
        String answerSdp = videoRoomAdaptor.startLive(janusSessionId,
                sessionToHandle.get(janusSessionId),
                json.getString("sdp"));
        JSONObject result = new JSONObject().put("sdp", answerSdp).put("type", "publish_stream_result");
        logger.info("Sending publish stream result");
        logger.debug(result.toString());
        context.send(result.toString());
    }

    private void handleJoinRoom(String user, WsMessageContext context, JSONObject json, Long janusSessionId) {
        if (!json.has("room_name") || !json.has("role"))
            return;
        if (json.get("role").equals("publisher")) {
            context.send(videoRoomAdaptor.publisherJoinRoom(janusSessionId,
                            sessionToHandle.get(janusSessionId),
                            json.getLong("room_name"),
                            json.has("display_name") ? json.getString("display_name") : user)
                    .put("type", "join_room_result")
                            .put("role","publisher")
                    .toString());
        } else {
            if(!json.has("feeds")){
                context.send(new JSONObject().put("type","error").put("message","Need feedIds").toString());
                return;
            }
            int size = json.getJSONArray("feeds").length();
            long[] feeds =  new long[size];
            for(int i = 0; i<size; i++) {
                feeds[i] = ((Long)json.getJSONArray("feeds").get(i)).longValue();
            }
            JSONObject messageResult = videoRoomAdaptor.subscriberJoinRoom(janusSessionId,
                    sessionToHandle.get(janusSessionId),
                    json.getLong("room_name"),
                    json.has("display_name") ? json.getString("display_name") : user,
                    feeds);
            messageResult.put("type", "join_room_result").put("role","subscriber");
            context.send(messageResult.toString());
        }
    }

    public void createSession(String userId, WsConnectContext ctx) {
        CompletableFuture.supplyAsync(janusClient::createSession)
                .thenApply(sessionId -> {
                    janusToUserMap.put(sessionId, userId);
                    userToJanus.put(userId, sessionId);
                    userContexts.put(userId, ctx);
                    return sessionId;
                })
                .thenAccept(sessionId -> {
                    Long handleId = videoRoomAdaptor.attachToVideoRoom(sessionId);
                    sessionToHandle.put(sessionId, handleId);
                });
    }

    public void destroySession(String userId) {
        Long janusSessionId = userToJanus.remove(userId);
        userContexts.remove(userId);
        if (janusSessionId != null) {
            janusToUserMap.remove(janusSessionId);
            janusClient.destroySession(janusSessionId);
            sessionToHandle.remove(janusSessionId);
        }
    }

    private static class WatchDog implements Runnable {
        public WatchDog(Map<String, WsConnectContext> userContexts) {
            this.userContexts = userContexts;
        }

        private final Map<String, WsConnectContext> userContexts;
        public boolean shouldStop = false;

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                userContexts.values().stream().forEach(context -> {
                    context.send("{\"type\":\"keep_alive\"}");
                });
            }
        }
    }
}
