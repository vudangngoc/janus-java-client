package net.edulive.janus.java_client.videoroom;

import net.edulive.janus.java_client.JanusClient;
import net.edulive.janus.java_client.DoNothingHandler;
import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.edulive.janus.java_client.JanusKeywords.*;

public class VideoRoomAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(VideoRoomAdaptor.class);

    public VideoRoomAdaptor(JanusClient janusClient) {
        this.janusClient = janusClient;
    }

    private final JanusClient janusClient;

    /**
     * Create a handleId for interacting with Videoroom plugin
     *
     * @param sessionId Janus session ID of user
     * @return handleId of user in the context of Videoroom plugin
     */
    public Long attachToVideoRoom(Long sessionId) {
        logger.debug("Attach to video room plugin with sessionId: {}", sessionId);
        JSONObject data = new JSONObject().put(JANUS_JANUS, "attach")
                .put("plugin", "janus.plugin.videoroom")
                .put("opaque_id", UUID.randomUUID().toString());
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new AttachToVideoRoomHandler(result, transactionId, sessionId));
        try {
            return result.get().getLong("id");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in attachToVideoRoom", e);
            Thread.currentThread().interrupt();
        }
        return 0L;
    }

    /**
     * Join room as a publisher
     *
     * @param sessionId   Janus sessionId of user
     * @param handleId    Unique Id of user in the context of plugin, created when user attach to plugin
     * @param roomName    Unique room name to join
     * @param displayName A name to show to other participants in the room
     * @return Information about the room
     */
    public JSONObject publisherJoinRoom(Long sessionId, Long handleId, long roomName, String displayName) {
        JSONObject message = new JSONObject()
                .put(JANUS_REQUEST, "join")
                .put("ptype", "publisher")
                .put("room", roomName)
                .put("display", displayName);
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", message).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new PublisherJoinRoomHandler(result, transactionId, sessionId));
        try {
            return result.get();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in publisherJoinRoom", e);
            Thread.currentThread().interrupt();
        }
        return new JSONObject();
    }

    /**
     * @param sessionId   Janus sessionId of user
     * @param handleId    Unique Id of user in the context of plugin, created when user attach to plugin
     * @param roomName    Unique room name to join
     * @param displayName A name to show to other participants in the room
     * @param feederIds   List of streams to subscribe
     * @return An SDP message from Janus
     */
    public JSONObject subscriberJoinRoom(Long sessionId, Long handleId, long roomName, String displayName, long[] feederIds, Long privateId) {
        JSONArray streams = new JSONArray();
        for (long feederId : feederIds) {
            streams.put(new JSONObject().put("feed", feederId));
        }
        JSONObject message = new JSONObject()
                .put(JANUS_REQUEST, "join")
                .put("ptype", "subscriber")
                .put("private_id", privateId)
                .put("room", roomName)
                .put("display", displayName);
        message.put("streams", streams);
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", message).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new JanusTransactionAbstractHandler<>(result, transactionId, sessionId) {
            @Override
            public boolean process(JSONObject janusMessage) {
                if (janusMessage.get("janus").equals("ack")) {
                    this.ack = true;
                    return true;
                }
                if (janusMessage.has("jsep")) {
                    this.getResult().complete(janusMessage.getJSONObject("jsep"));
                } else {
                    this.getResult().complete(janusMessage);
                }
                return false;
            }
        });
        try {
            return result.get();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in subscriberJoinRoom", e);
            Thread.currentThread().interrupt();
        }
        return new JSONObject();
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @return Unique room name
     */
    public long createRoom(Long sessionId, Long handleId) {
        JSONObject message = new JSONObject().put(JANUS_REQUEST, "create").put("notify_joining", true);
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE)
                .put("body", message)
                .put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<Long> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new CreateRoomHandler(result, transactionId, sessionId));
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in createRoom", e);
            Thread.currentThread().interrupt();
        }
        return 0L;
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @param roomId    Unique room name
     */
    public void destroyRoom(Long sessionId, Long roomId, Long handleId) {
        JSONObject message = new JSONObject().put(JANUS_REQUEST, "destroy").put("room", roomId);
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", message).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        janusClient.sendToSession(transactionId, sessionId, data, null);
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @return A JSON represent all rooms with detail infor
     */
    public JSONObject getAllRooms(Long sessionId, Long handleId) {
        JSONObject message = new JSONObject().put(JANUS_REQUEST, "list");
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", message).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new JanusTransactionAbstractHandler<>(result, transactionId, sessionId) {
            @Override
            public boolean process(JSONObject janusMessage) {
                if (janusMessage.getString("janus").equals("ack")) {
                    this.ack = true;
                    return true;
                }
                this.getResult().complete(janusMessage.getJSONObject("plugindata").getJSONObject("data"));
                return false;
            }
        });
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in getAllRooms", e);
            Thread.currentThread().interrupt();
        }
        return new JSONObject();
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @param sdp       SDP offer describe connection info
     * @return A SDP message from Janus
     */
    public String startLive(Long sessionId, Long handleId, String sdp) {
        String transactionId = UUID.randomUUID().toString();
        JSONObject body = new JSONObject("{\"request\": \"configure\", \"audio\": true, \"video\": true}");
        JSONObject jsep = new JSONObject().put("type", "offer").put("sdp", sdp);
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", body).put(JANUS_HANDLE_ID, handleId).put("jsep", jsep);
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new StartLiveHandler(result, transactionId, sessionId));
        try {
            return result.get().getJSONObject("jsep").getString("sdp");
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in startLive", e);
            Thread.currentThread().interrupt();
        }
        return "";
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @param candidate ICE candidate ready for connect
     */
    public void sendConnectionInfo(Long sessionId, Long handleId, JSONObject candidate) {
        String transactionId = UUID.randomUUID().toString();
        JSONObject origin = new JSONObject().put(JANUS_JANUS, "trickle").put(JANUS_HANDLE_ID, handleId);
        DoNothingHandler handler = new DoNothingHandler(transactionId, sessionId);
        JSONObject connectionInfo = origin.put("candidate", candidate);
        janusClient.sendToSession(transactionId, sessionId, connectionInfo, handler);
    }

    /**
     * Send event to notice client is finish ICE gathering
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     */
    public void sendCompleteIceGathering(Long sessionId, Long handleId) {
        String transactionId = UUID.randomUUID().toString();
        JSONObject origin = new JSONObject().put(JANUS_JANUS, "trickle").put(JANUS_HANDLE_ID, handleId);
        DoNothingHandler handler = new DoNothingHandler(transactionId, sessionId);
        janusClient.sendToSession(transactionId, sessionId, origin.put("candidate", new JSONObject().put("completed", true)), handler);
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @param sdp       A message describe connection info
     * @param roomId    Unique room name
     * @return A SDP message
     */
    public JSONObject sendViewerSDPAnswer(Long sessionId, Long handleId, String sdp, Long roomId) {
        JSONObject body = new JSONObject().put(JANUS_REQUEST, "start").put("room", roomId);
        JSONObject jsep = new JSONObject().put("type", "answer").put("sdp", sdp);
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put(JANUS_HANDLE_ID, handleId).put("body", body).put("jsep", jsep);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();

        janusClient.sendToSession(transactionId, sessionId, data, new JanusTransactionAbstractHandler<>(result, transactionId, sessionId) {
            @Override
            public boolean process(JSONObject janusMessage) {
                if(janusMessage.getString("janus").equals("ack")){
                    this.ack = true;
                    return false;
                }
                this.futureJob.complete(janusMessage.getJSONObject("plugindata"));
                return false;
            }
        });
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in sendViewerSDPAnswer", e);
            Thread.currentThread().interrupt();
        }
        return new JSONObject();
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     */
    public void leaveRoom(Long sessionId, Long handleId) {
        JSONObject message = new JSONObject().put(JANUS_REQUEST, "leave");
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", message).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new ViewerLeaveRoomHandler(result, transactionId, sessionId));
        try {
            result.get().getLong("session_id");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in leaveRoom", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     */
    public JSONObject stopPublishStream(Long sessionId, Long handleId) {
        JSONObject message = new JSONObject().put(JANUS_REQUEST, "unpublish");
        JSONObject data = new JSONObject().put(JANUS_JANUS, JANUS_MESSAGE).put("body", message).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new ViewerLeaveRoomHandler(result, transactionId, sessionId));
        try {
            return result.get();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in stopPublishStream", e);
            Thread.currentThread().interrupt();
        }
        return new JSONObject();
    }

    /**
     * @param sessionId Janus sessionId of user
     * @param handleId  Unique Id of user in the context of plugin, created when user attach to plugin
     * @param feeds     Ids of stream to subscribe
     */
    public JSONObject subscriptStream(Long sessionId, Long handleId, long[] feeds) {
        JSONObject message = new JSONObject().put(JANUS_REQUEST, "subscribe");
        JSONArray streams = new JSONArray();
        for (long feed : feeds) {
            streams.put(new JSONObject().put("feed", feed));
        }
        message.put("streams", streams).put(JANUS_HANDLE_ID, handleId);
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, message, new JanusTransactionAbstractHandler<>(result, transactionId, sessionId) {
            @Override
            public boolean process(JSONObject janusMessage) {
                this.getResult().complete(janusMessage);
                return false;
            }
        });
        try {
            return result.get();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in subscriptStream", e);
            Thread.currentThread().interrupt();
        }
        return new JSONObject();
    }
}
