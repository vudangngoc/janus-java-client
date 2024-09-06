package net.edulive.janus.java_client;

import dev.onvoid.webrtc.RTCPeerConnection;
import net.edulive.janus.java_client.textroom.TextRoomAdaptor;
import net.edulive.janus.java_client.videoroom.VideoRoomAdaptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMainFlow extends AbstractTestCase{

    @BeforeClass
    public static void setup() {
        client.connect();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static JanusClient client = new JanusClient((sessionId,message) ->  true,"ws://159.65.129.9:8188/ws");
    @AfterClass
    public static void shutdown() {
        client.close();
    }
    @Test
    public void testVideoRoom(){
        VideoRoomAdaptor videoRoomAdaptor = new VideoRoomAdaptor(client);
        Long sessionId = client.createSession();
        Long handleId = videoRoomAdaptor.attachToVideoRoom(sessionId);
        System.out.printf(videoRoomAdaptor.getAllRooms(sessionId,handleId).toString());
        videoRoomAdaptor.publisherJoinRoom(sessionId,handleId,1234, "janus-java-client");
        videoRoomAdaptor.leaveRoom(sessionId,handleId);
    }

    @Test
    public void testTextRoom(){
        TextRoomAdaptor textRoomAdaptor = new TextRoomAdaptor(client);
        Long sessionId = client.createSession();
        Long handleId = textRoomAdaptor.attachToTextRoom(sessionId);

        RTCPeerConnection connection = client.setupConnection(sessionId,handleId);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        System.out.printf(videoRoomAdaptor.getAllRooms(sessionId,handleId).toString());
//        videoRoomAdaptor.publisherJoinRoom(sessionId,handleId,1234, "janus-java-client");
//        videoRoomAdaptor.leaveRoom(sessionId,handleId);
    }
}
