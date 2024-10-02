package net.edulive.janus.java_client;

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
    private static final JanusClient client = new JanusClient((sessionId, message) ->  true,"ws://125.212.229.11:8188/ws");
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
        textRoomAdaptor.attachToTextRoom(sessionId);

         textRoomAdaptor.getRooms();
        assert true;
    }
}
