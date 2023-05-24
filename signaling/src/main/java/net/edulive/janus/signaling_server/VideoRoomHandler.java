package net.edulive.janus.signaling_server;

import net.edulive.janus.java_client.JanusClient;

public class VideoRoomHandler {
    JanusClient janusClient = new JanusClient((sessionId, message) -> {
        return true;
    },"localhost:8188");
}
