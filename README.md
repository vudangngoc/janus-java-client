
# janus-java-client
This library provide a Java client to manage Janus gateway WebRTC. Working features:
+ Start a live stream to Janus server
+ Subscribe a live stream from Janus server
+ Handle some events from Janus then forward to clients
+ Support Video room plugin: create/join/leave/delete a video room. Other features of Video room plugin will be supported soon.

Everything is early state, feel free to contribute and request features!
# Signaling server
Janus server and client need to exchange connection information, there is a working example in the /signaling folder (include frontend with jQuery, backend with Java support websocket and signaling process)

# JanusClient
JanusClient take care about how to interactive with Janus server: create Janus session, attach to plugin, send message to Janus
+ public JanusClient(MessageCallback messageCallback, String address): Constructor of class
  + messageCallback: There are some Janus's messages don't have transactionId then Janus client cannot find
      a handler to process. Those messages will forward to user's context
  + address: URL of Janus's websocket, if the URL doesn't start with "ws" nor "wss", a "ws://" will be appended at start of the URL
+ public boolean connect(): Connect Janus client to Janus server, 
  + return true if connect successful 
+ public Long createSession(): Create a Janus session, this function should be called after the method connect() is success
  + return a sessionId in Long
+ public long attachToPlugin(Long sessionId, String pluginName, JSONObject data): Attach to any plugin of Janus server
  + sessionId: Janus session ID of user
  + pluginName: plugin will handle the command
  + data: data of command
  + return handleId of user in context of the plugin
+ public void sendToSession(String transactionId, Long sessionId, JSONObject data, JanusTransactionAbstractHandler handler): Send a command/event to a plugin
  + transactionId: a random string that the client can use to match response messages from the Janus server.
  + sessionId: Janus session ID of user
  + data: information about the command
  + handler: object will process returned message
+ public long destroySession(Long sessionId): Kill an exists Janus session
  + sessionId: Janus session ID of user
# VideoRoomAdaptor
VideoRoomAdaptor provide APIs to work with Video Room plugin: create/join/leave/delete a video room. Most of methods require
+ sessionId: Janus sessionId of user 
+ handleId:  Unique Id of user in the context of plugin, created when user attach to plugin
All APIs:
+ public Long attachToVideoRoom(Long sessionId): Create a handleId for interacting with Videoroom plugin
    + sessionId: Janus session ID of user
    + return a handleId in Long for future interaction
+ public long createRoom(Long sessionId, Long handleId):
  + return a unique room name that has just created
+ public JSONObject getAllRooms(Long sessionId, Long handleId):
  + return A JSON represent all rooms with detail information
+ public JSONObject publisherJoinRoom(Long sessionId, Long handleId, long roomName, String displayName): "publishers are those participant handles that are able (although may choose not to, more on this later) publish media in the room"
  + roomName: unique room name in long
  + displayName: a name for display in VideoRoom, can be duplicated
  + return Information about the room
+ public String startLive(Long sessionId, Long handleId, String sdp)
  + spd: SDP offer describe connection info, to start handshake process
  + return SDP answer from Janus server
+ public void sendConnectionInfo(Long sessionId, Long handleId, JSONObject candidate): Client send ICE candidates to Janus server. A RTCConnection can have multiple candidate, then call this method multiple times.
  + candidate: information about a candidate
+ public void sendCompleteIceGathering(Long sessionId, Long handleId): Client notice that they send all candidates, signaling server will send a special event to tell Janus server.
+ public JSONObject subscriberJoinRoom(Long sessionId, Long handleId, long roomName, String displayName, long[] feederIds, Long privateId): "subscribers are NOT participants, but simply handles that will be used exclusively to receive media from one or more publishers in the room"
  + roomName: Unique room name to join
  + displayName: A name to show to other participants in the room
  + feederIds: List of streams to subscribe, Janus support to subscribe multiple stream, but currently, the library support only one
  + return A SDP offer from Janus, clients have to process this message then return a SDP answer with the next method
+ public String sendViewerSDPAnswer(Long sessionId, Long handleId, String sdp, Long roomId): After receive SDP offer from Janus, subscriber has to process and return a SDP answer
  + sdp: SDP answer
  + roomId: Unique room name to join
  + return output of subscriber join
+ public JSONObject stopPublishStream(Long sessionId, Long handleId): stop publish a stream
  + return output of action
+ public void leaveRoom(Long sessionId, Long handleId): a publisher/subscriber left room and stop publish/subscribe a stream
+ public void destroyRoom(Long sessionId, Long roomId, Long handleId): delete a room
  + roomId: unique room name to destroy
# Examples
```java
public class TestMainFlow {
    @Test
    public void testVideoRoom() {
        VideoRoomAdaptor videoRoomAdaptor = new VideoRoomAdaptor(client);
        Long sessionId = client.createSession();
        Long handleId = videoRoomAdaptor.attachToVideoRoom(sessionId);
        System.out.printf(videoRoomAdaptor.getAllRooms(sessionId, handleId).toString());
        videoRoomAdaptor.publisherJoinRoom(sessionId, handleId, 1234, "janus-java-client");
        videoRoomAdaptor.leaveRoom(sessionId, handleId);
    }
}
```
Please refer to file TestMainFlow and folder /signaling for detail information