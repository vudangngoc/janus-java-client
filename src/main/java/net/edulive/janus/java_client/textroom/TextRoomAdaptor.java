package net.edulive.janus.java_client.textroom;

import net.edulive.janus.java_client.JanusClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.edulive.janus.java_client.JanusKeywords.JANUS_JANUS;

public class TextRoomAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(TextRoomAdaptor.class);

    private final JanusClient janusClient;

    public TextRoomAdaptor(JanusClient janusClient) {
        this.janusClient = janusClient;
    }

    public Long attachToTextRoom(Long sessionId) {
        logger.debug("Attach to video room plugin with sessionId: {}", sessionId);
        JSONObject data = new JSONObject().put(JANUS_JANUS, "attach")
                .put("plugin", "janus.plugin.textroom")
                .put("opaque_id", UUID.randomUUID().toString());
        String transactionId = UUID.randomUUID().toString();
        CompletableFuture<JSONObject> result = new CompletableFuture<>();
        janusClient.sendToSession(transactionId, sessionId, data, new AttachToTextRoomHandler(result, transactionId, sessionId));
        try {
            return result.get().getLong("id");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            logger.error("Error in attachToVideoRoom", e);
            Thread.currentThread().interrupt();
        }
        return 0L;
    }
}
