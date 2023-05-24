package net.edulive.janus.java_client;

import java.util.concurrent.CompletableFuture;
import org.json.JSONObject;

class DestroySessionHandler extends JanusTransactionAbstractHandler<JSONObject> {

	public DestroySessionHandler(CompletableFuture<JSONObject> futureJob, String transactionId, long sessionId) {
		super(futureJob, transactionId, sessionId);
	}

	@Override
	public boolean process(JSONObject janusMessage) {
		switch (janusMessage.getString("janus")) {
		case "success":
			this.getResult().complete(janusMessage);
			break;
		default:
			this.getResult().cancel(true);
			break;
		}
		return false;
	}

}
