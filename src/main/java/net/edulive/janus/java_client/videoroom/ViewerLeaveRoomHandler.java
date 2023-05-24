package net.edulive.janus.java_client.videoroom;

import java.util.concurrent.CompletableFuture;

import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONObject;

class ViewerLeaveRoomHandler extends JanusTransactionAbstractHandler<JSONObject> {

	public ViewerLeaveRoomHandler(CompletableFuture<JSONObject> futureJob, String transactionId, long sessionId) {
		super(futureJob, transactionId, sessionId);
	}

	@Override
	public boolean process(JSONObject janusMessage) {
		switch (janusMessage.getString("janus")) {
		case "hangup":
		case "event":
			this.getResult().complete(janusMessage);
			break;
		case "ack":
			this.ack = true;
			return true;
		default:
			this.getResult().cancel(true);
			break;
		}
		return false;
	}

}
