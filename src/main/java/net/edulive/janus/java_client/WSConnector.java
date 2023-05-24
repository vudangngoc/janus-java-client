package net.edulive.janus.java_client;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.json.JSONObject;

class WSConnector extends WebSocketClient{
	public WSConnector(URI serverUri, JanusClient callback, NetworkEventHandler networkHandler) {
		super(serverUri,new Draft_6455(Collections.emptyList(), Collections.singletonList(new Protocol("janus-protocol"))), Collections.EMPTY_MAP);
		this.callback = callback;
		this.networkHandler = networkHandler;
	}

	private NetworkEventHandler networkHandler;
	private JanusClient callback;

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		networkHandler.onOpen(convert(handshakedata));
	}

	@Override
	public void onMessage(String message) {
		callback.onMessage(message);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		networkHandler.onClose(new JSONObject().put("code", code).put("reason", reason).put("remote", remote));
	}

	@Override
	public void onError(Exception ex) {
		networkHandler.onError(ex);
	}

	private JSONObject convert(ServerHandshake handshakedata) {
		JSONObject result = new JSONObject();
		result.put("httpStatus", handshakedata.getHttpStatus())
		.put("httpMessage", handshakedata.getHttpStatusMessage());
		Iterator<String> fields = handshakedata.iterateHttpFields();
		while(fields.hasNext()) {
			String field = fields.next();
			result.put(field, handshakedata.getFieldValue(field));
		}

		return result;
	}
}