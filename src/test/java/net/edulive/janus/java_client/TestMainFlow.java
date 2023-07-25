package net.edulive.janus.java_client;

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
    private static JanusClient client = new JanusClient((sessionId,message) ->  true,"wss://janus.conf.meetecho.com/ws");
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
}
