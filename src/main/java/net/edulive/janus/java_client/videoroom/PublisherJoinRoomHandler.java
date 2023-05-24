package net.edulive.janus.java_client.videoroom;

import java.util.concurrent.CompletableFuture;

import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONObject;

class PublisherJoinRoomHandler extends JanusTransactionAbstractHandler<JSONObject> {

	public PublisherJoinRoomHandler(CompletableFuture<JSONObject> futureJob, String transactionId, long sessionId) {
		super(futureJob, transactionId, sessionId);
	}

	@Override
	public boolean process(JSONObject janusMessage) {
		switch (janusMessage.getString("janus")) {
		case "event":
			this.getResult().complete(janusMessage.getJSONObject("plugindata").getJSONObject("data"));
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
