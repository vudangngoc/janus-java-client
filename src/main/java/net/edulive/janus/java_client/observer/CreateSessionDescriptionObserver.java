package net.edulive.janus.java_client.observer;

import dev.onvoid.webrtc.RTCSessionDescription;

public class CreateSessionDescriptionObserver implements dev.onvoid.webrtc.CreateSessionDescriptionObserver {
    @Override
    public void onSuccess(RTCSessionDescription rtcSessionDescription) {
        System.out.println("CreateSessionDescriptionObserver.onSuccess" + rtcSessionDescription.sdp);
    }

    @Override
    public void onFailure(String s) {

    }
}
