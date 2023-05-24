package net.edulive.janus.java_client;

import org.json.JSONObject;

interface NetworkEventHandler {

	void onOpen(JSONObject convert);

	void onClose(JSONObject put);

	void onError(Exception ex);

}
