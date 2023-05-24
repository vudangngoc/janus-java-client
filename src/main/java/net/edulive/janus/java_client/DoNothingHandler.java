package net.edulive.janus.java_client;

import org.json.JSONObject;

public class DoNothingHandler extends JanusTransactionAbstractHandler{

    public DoNothingHandler(String transactionId, long sessionId) {
        super(null, transactionId, sessionId);
    }

    @Override
    public boolean process(JSONObject janusMessage) {
        return true;
    }
}
