package net.edulive.janus.java_client.observer;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionObserver implements PeerConnectionObserver {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionObserver.class);
    @Override
    public void onSignalingChange(RTCSignalingState state) {
        System.out.println("Signaling state changed: " + state);
    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state) {
        System.out.println("Connection state changed: " + state);
    }

    @Override
    public void onIceConnectionChange(RTCIceConnectionState state) {
        System.out.println("Ice connection state changed: " + state);
    }

    @Override
    public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
        System.out.println("Standardized ice connection state changed: " + state);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        System.out.println("Ice connection receiving changed: " + receiving);
    }

    @Override
    public void onIceGatheringChange(RTCIceGatheringState state) {
        System.out.println("Ice gathering state changed: " + state);
    }

    @Override
    public void onIceCandidate(RTCIceCandidate iceCandidate) {
        System.out.println("Ice candidate: " + iceCandidate);
        if (iceCandidate == null) return;

        final String candidate = String.format(
                "{\"sdpMid\":\"%s\", \"sdpMLineIndex\":%d, \"candidate\":\"%s\"}",
                iceCandidate.sdpMid,
                iceCandidate.sdpMLineIndex,
                iceCandidate.sdp
        );


        final String payload = String.format("{\"message\":\"icecandidate\",\"candidate\":%s}", candidate);

        //sendMessage(payload);
    }

    @Override
    public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
        System.out.println("Ice candidate error: " + event);
    }

    @Override
    public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {
        System.out.println("Ice candidates removed: " + candidates);
    }

    @Override
    public void onAddStream(MediaStream stream) {
        PeerConnectionObserver.super.onAddStream(stream);
    }

    @Override
    public void onRemoveStream(MediaStream stream) {
        PeerConnectionObserver.super.onRemoveStream(stream);
    }

    @Override
    public void onDataChannel(RTCDataChannel dataChannel) {
        System.out.println("Data channel: " + dataChannel);
    }

    @Override
    public void onRenegotiationNeeded() {
        PeerConnectionObserver.super.onRenegotiationNeeded();
    }

    @Override
    public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
        PeerConnectionObserver.super.onAddTrack(receiver, mediaStreams);
    }

    @Override
    public void onRemoveTrack(RTCRtpReceiver receiver) {
        PeerConnectionObserver.super.onRemoveTrack(receiver);
    }

    @Override
    public void onTrack(RTCRtpTransceiver transceiver) {
        PeerConnectionObserver.super.onTrack(transceiver);
    }
}
