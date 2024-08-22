package net.edulive.janus.java_client.textroom;

import net.edulive.janus.java_client.JanusTransactionAbstractHandler;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

class AttachToTextRoomHandler extends JanusTransactionAbstractHandler<JSONObject> {

	public AttachToTextRoomHandler(CompletableFuture<JSONObject> futureJob, String transactionId, long sessionId) {
		super(futureJob, transactionId, sessionId);
	}

	@Override
	public boolean process(JSONObject janusMessage) {
		if(janusMessage.getString("janus").equals("success")) {
			this.getResult().complete(janusMessage.getJSONObject("data"));
		} else {
			this.getResult().cancel(true);
		}
		return false;
	}

}
