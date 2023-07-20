package net.edulive.janus.java_client;

import net.edulive.janus.java_client.videoroom.VideoRoomAdaptor;
import org.json.JSONObject;
import org.junit.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

public class TestMainFlow extends AbstractTestCase{

    @BeforeClass
    public static void setup() {
        client.connect();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static JanusClient client = new JanusClient((sessionId,message) ->  true,"localhost:8188");
    @AfterClass
    public static void shutdown() {
        client.close();
    }
    @Test
    public void testVideoRoom(){

        VideoRoomAdaptor videoRoomAdaptor = new VideoRoomAdaptor(client);
        Long sessionId = client.createSession();
        Long handleId = videoRoomAdaptor.attachToVideoRoom(sessionId);
        long roomName = videoRoomAdaptor.createRoom(sessionId,handleId);
        JSONObject joinRoomResult = videoRoomAdaptor.publisherJoinRoom(sessionId,handleId,roomName, "NgocVD-KMS");
        System.out.println(joinRoomResult.toString());
        Long subscriberSessionId = client.createSession();
        Long subscriberHandleId = videoRoomAdaptor.attachToVideoRoom(subscriberSessionId);
        videoRoomAdaptor.subscriberJoinRoom(subscriberSessionId,subscriberHandleId,roomName,"",new long[]{0L}, 0l);
        videoRoomAdaptor.destroyRoom(sessionId,roomName,handleId);
    }
}
