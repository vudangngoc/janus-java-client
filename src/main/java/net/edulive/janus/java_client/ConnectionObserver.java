package net.edulive.janus.java_client;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;

public class ConnectionObserver implements PeerConnectionObserver {
    @Override
    public void onSignalingChange(RTCSignalingState state) {
        PeerConnectionObserver.super.onSignalingChange(state);
    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state) {
        PeerConnectionObserver.super.onConnectionChange(state);
    }

    @Override
    public void onIceConnectionChange(RTCIceConnectionState state) {
        PeerConnectionObserver.super.onIceConnectionChange(state);
    }

    @Override
    public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
        PeerConnectionObserver.super.onStandardizedIceConnectionChange(state);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        PeerConnectionObserver.super.onIceConnectionReceivingChange(receiving);
    }

    @Override
    public void onIceGatheringChange(RTCIceGatheringState state) {
        PeerConnectionObserver.super.onIceGatheringChange(state);
    }

    @Override
    public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {

    }

    @Override
    public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
        PeerConnectionObserver.super.onIceCandidateError(event);
    }

    @Override
    public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {
        PeerConnectionObserver.super.onIceCandidatesRemoved(candidates);
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
        PeerConnectionObserver.super.onDataChannel(dataChannel);
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
