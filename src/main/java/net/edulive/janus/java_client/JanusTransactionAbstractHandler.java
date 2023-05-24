package net.edulive.janus.java_client;

import java.util.concurrent.CompletableFuture;

public abstract class JanusTransactionAbstractHandler<T> implements JanusTransactionHandler {

	public JanusTransactionAbstractHandler(CompletableFuture<T> futureJob, String transactionId, long sessionId) {
		this.futureJob = futureJob;
		this.transactionId = transactionId;
		this.sessionId = sessionId;
	}
	protected CompletableFuture<T> futureJob;
	protected boolean ack = false;
	protected long createTime = System.currentTimeMillis();
	protected long lastAccess = System.currentTimeMillis();
	protected String transactionId;
	protected long sessionId;

	/**
	 *
	 * @return
	 */
	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public long getLastAccess() {
		return lastAccess;
	}

	@Override
	public long getSessionId() {
		return sessionId;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public boolean getACK() {
		return ack;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public CompletableFuture<T> getResult() {
		return futureJob;
	}
	
}
