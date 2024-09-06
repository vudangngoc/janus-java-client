package net.edulive.janus.java_client.observer;

import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class SessionObserver implements SetSessionDescriptionObserver {
    private static final Logger logger = LoggerFactory.getLogger(SessionObserver.class);
    CompletableFuture<Boolean> future;
    public SessionObserver(CompletableFuture<Boolean> future) {
        this.future = future;
    }

    @Override
    public void onSuccess() {
        System.out.println("Session description set successfully");
        future.complete(true);
    }

    @Override
    public void onFailure(String s) {
        System.out.println("Session description set failed: " + s);
        future.complete(false);
    }
}
