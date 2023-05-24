package net.edulive.janus.java_client;

public interface MessageCallback {
    boolean handle(long sessionId, String message);
}
