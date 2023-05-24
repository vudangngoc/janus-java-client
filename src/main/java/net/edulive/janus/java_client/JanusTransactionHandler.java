package net.edulive.janus.java_client;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

interface JanusTransactionHandler {
	long getCreateTime();
	long getLastAccess();
	long getSessionId();
	String getTransactionId();
	boolean getACK();
	CompletableFuture<?> getResult();

	/**
	 * Handle message from Janus server
	 * @param janusMessage the message from Janus
	 * @return <code>false</code> if no further message to handle
	 */
	boolean process(JSONObject janusMessage);
}
