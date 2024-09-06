package net.edulive.janus.java_client.observer;

import dev.onvoid.webrtc.RTCSessionDescription;
import net.edulive.janus.java_client.DoNothingHandler;
import net.edulive.janus.java_client.JanusClient;
import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CreateSessionDescriptionObserver implements dev.onvoid.webrtc.CreateSessionDescriptionObserver {
    private static final Logger logger = LoggerFactory.getLogger(CreateSessionDescriptionObserver.class);
    private final JanusClient janusClient;
    private final Long session_id;
    private final Long handle_id;

    public CreateSessionDescriptionObserver(JanusClient janusClient, Long session_id, Long handle_id) {
        this.janusClient = janusClient;
        this.session_id = session_id;
        this.handle_id = handle_id;
    }

    @Override
    public void onSuccess(RTCSessionDescription rtcSessionDescription) {
        System.out.println("CreateSessionDescriptionObserver.onSuccess: " + rtcSessionDescription);
        var sdp = rtcSessionDescription.sdp;
        JSONObject message = new JSONObject();
        message.put("janus", "message");
        message.put("handle_id", handle_id);
        message.put("body", new JSONObject().put("request", "ack"));
        message.put("jsep", new JSONObject().put("type", "answer").put("sdp", sdp));
        String transactionId = UUID.randomUUID().toString();

        janusClient.sendToSession(transactionId, session_id, message, new DoNothingHandler(transactionId, session_id));
    }

    @Override
    public void onFailure(String s) {
        logger.error("CreateSessionDescriptionObserver.onFailure: " + s);
    }
}
