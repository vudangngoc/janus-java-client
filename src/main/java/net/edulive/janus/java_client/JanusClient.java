package net.edulive.janus.java_client;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.edulive.janus.java_client.JanusKeywords.JANUS_JANUS;
import static net.edulive.janus.java_client.JanusKeywords.JANUS_TRANSACTION;


public class JanusClient {
    /**
     * Init a Janus client, but don't connect to the Janus server yet
     *
     * @param messageCallback There are some Janus's messages don't have transactionId then Janus client cannot find
     *                        a handler to process. Those messages will forward to user's context
     * @param address         Address of Janus's websocket
     */
    public JanusClient(MessageCallback messageCallback, String address) {
        this.messageCallback = messageCallback;
        this.address = address;
    }

    private static final Logger logger = LoggerFactory.getLogger(JanusClient.class);
    private WSConnector transporter;

    private final MessageCallback messageCallback;

    private WatchDog watchDog;
    private final String address;

    private final Set<Long> sessionIdMap = new HashSet<>();
    Map<String, JanusTransactionHandler> transactionHandlers = new HashMap<>();

    private void sendToRoot(String transactionId, JSONObject data) {
        if (!this.transporter.isOpen()) {
            this.connect();
        }
        this.transporter.send(data.put(JANUS_TRANSACTION, transactionId).toString());
    }

    /**
     * Send command to session
     *
     * @param transactionId a random string that the client can use to match response messages from the Janus server
     * @param sessionId     Janus session ID of user
     * @param data          information about the command
     * @param handler       object will process returned messages
     */
    public void sendToSession(String transactionId, Long sessionId, JSONObject data, JanusTransactionAbstractHandler handler) {
        if (handler != null) {
            this.transactionHandlers.put(transactionId, handler);
        }
        data.put(JANUS_TRANSACTION, transactionId)
                .put("session_id", sessionId);
        logger.info("Sending: {}", data);
        this.transporter.send(data.toString());
    }

    /**
     * attach a Janus session to a plugin
     *
     * @param sessionId  Janus session of user
     * @param pluginName plugin will handle the command
     * @param data       data of command
     * @return handleId of user in context of the plugin
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public long attachToPlugin(Long sessionId, String pluginName, JSONObject data) throws ExecutionException, InterruptedException {
        data.put(JANUS_JANUS, "attach").put("plugin", pluginName);
        CompletableFuture<Long> result = new CompletableFuture<>();
        String transactionId = UUID.randomUUID().toString();
        this.sendToSession(transactionId, sessionId, data, new JanusTransactionAbstractHandler<>(result, transactionId, sessionId) {
            @Override
            public boolean process(JSONObject janusMessage) {
                if (janusMessage.getString(JANUS_JANUS).equals("success")) {
                    this.getResult().complete(janusMessage.getJSONObject("data").getLong("id"));
                } else {
                    this.getResult().cancel(true);
                }
                return false;
            }
        });
        return result.get();
    }

    protected void onMessage(String message) {
        logger.info("Receive: {}", message);
        JSONObject incomeMessage = new JSONObject(message);
        if (handleSpecialEvent(incomeMessage)) {
            return;
        }
        if (!incomeMessage.has(JANUS_TRANSACTION)) {
            return;
        }
        String transactionId = incomeMessage.getString(JANUS_TRANSACTION);
        JanusTransactionHandler handler = this.transactionHandlers.get(transactionId);
        if (handler != null) {
            if (!handler.process(incomeMessage)) {
                this.transactionHandlers.remove(transactionId);
            }
        } else {
            logger.info("cannot found handler for transaction: {}", transactionId);
        }
    }

    private boolean handleSpecialEvent(JSONObject message) {
        String eventName = message.getString(JANUS_JANUS);
        switch (eventName) {
            case "event":
                if (message.has(JANUS_TRANSACTION)) {
                    break;
                }
            case "media": // Janus receive audio bytes event
            case "slowlink": // Janus cannot send NACKs to Peer
            case "hangup": // PeerConnection close
            case "webrtcup": // Janus connect successful
                this.messageCallback.handle(message.getLong("session_id"), message.toString());
                return true;
        }
        return false;
    }

    /**
     * Connect Janus client to Janus server
     *
     * @return <code>true</code> if connect success
     */
    public boolean connect() {
        transporter = new WSConnector(URI.create(address.startsWith("ws") ? address : "ws://" + address), this, new NetworkEventHandler() {

            @Override
            public void onOpen(JSONObject message) {
                logger.debug("OnOpen connection: {}", message);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(JSONObject message) {
                logger.debug("OnClose connection: {}", message);
            }
        });
        transporter.connect();
        watchDog = new WatchDog(this, this.sessionIdMap);
        Thread t = new Thread(watchDog);
        t.start();
        return true;
    }

    /**
     * Create a Janus session
     *
     * @return sessionId of new Janus session
     */
    public Long createSession() {
        if (transporter == null) {
            return 0L;
        }
        JSONObject data = new JSONObject().put(JANUS_JANUS, "create");
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        CreateSessionHandler handler = new CreateSessionHandler(result, UUID.randomUUID().toString(), 0);
        this.transactionHandlers.put(handler.getTransactionId(), handler);
        this.sendToRoot(handler.getTransactionId(), data);
        try {
            JSONObject jsonObject = result.get();
            long sessionId = jsonObject.getLong("id");
            this.sessionIdMap.add(sessionId);
            return sessionId;
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in createSession", e);
            Thread.currentThread().interrupt();
        }
        return 0L;
    }

    /**
     * Kill an exists Janus session
     *
     * @param sessionId
     * @return
     */
    public long destroySession(Long sessionId) {
        JSONObject data = new JSONObject().put(JANUS_JANUS, "destroy");
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        this.sendToSession(transactionId, sessionId, data, new DestroySessionHandler(result, transactionId, sessionId));
        try {
            long sessionToDestroy = result.get().getLong("session_id");
            this.sessionIdMap.remove(sessionToDestroy);
            return sessionToDestroy;
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in destroySession", e);
            Thread.currentThread().interrupt();
        }
        return 0L;
    }


    public String getAddress() {
        return this.address;
    }

    /**
     *
     */
    public void close() {
        this.watchDog.setShouldStop(true);
        this.transporter.close();
    }

    private static class WatchDog implements Runnable {
        public WatchDog(JanusClient transporter, Set<Long> sessionIdMap) {
            this.transporter = transporter;
            this.sessionIdMap = sessionIdMap;
        }

        private final Set<Long> sessionIdMap;
        private final JanusClient transporter;
        private boolean shouldStop = false;

        public void setShouldStop(boolean value) {
            this.shouldStop = value;
        }

        @Override
        public void run() {
            while (!this.shouldStop) {
                for (Long session : this.sessionIdMap) {
                    String transactionId = UUID.randomUUID().toString();
                    this.transporter.sendToSession(transactionId,
                            session,
                            new JSONObject().put(JANUS_JANUS, "keepalive"),
                            new DoNothingHandler(transactionId, session));
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    logger.error("Error while sleeping", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
