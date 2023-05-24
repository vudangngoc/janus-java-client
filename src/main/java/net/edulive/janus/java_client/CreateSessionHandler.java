package net.edulive.janus.java_client;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

class CreateSessionHandler extends JanusTransactionAbstractHandler<JSONObject> {

	public CreateSessionHandler(CompletableFuture<JSONObject> futureJob, String transactionId, long sessionId) {
		super(futureJob,transactionId,sessionId);
	}

	@Override
	public boolean process(JSONObject janusMessage) {
		switch (janusMessage.getString("janus")) {
		case "success":
			this.getResult().complete(janusMessage.getJSONObject("data"));
			break;
		default:
			this.getResult().cancel(true);
			break;
		}
		return false;
	}

}
