package net.edulive.janus.java_client.videoroom;

import java.util.concurrent.CompletableFuture;

import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONObject;

class StartLiveHandler extends JanusTransactionAbstractHandler<JSONObject> {

	public StartLiveHandler(CompletableFuture<JSONObject> futureJob, String transactionId, long sessionId) {
		super(futureJob, transactionId, sessionId);
	}

	@Override
	public boolean process(JSONObject janusMessage) {
		switch (janusMessage.getString("janus")) {
		case "event":
			this.getResult().complete(janusMessage);
			return false;
		case "ack":
			this.ack = true;
			return true;
		default:
			this.getResult().cancel(true);
			return false;
		}
	}

}
