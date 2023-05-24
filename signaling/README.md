
# Signaling server
This server is build for testing Java Janus client library, it isn't ready for production. The server provide a websocket with messages:

- Mandatory fields:
    - type: a string describe action of message
    - plugin: the plugin will handle this message, currently, we support only "videoroom"
- Create room:
    - Input: {"plugin" : "video_room", "type" : "create_room"}
    - Output: {"room_name":1234}
- Join room as a publisher: user will be added to publisher of room
    - Input: {"plugin" : "video_room", "type" : "join_room", "room_name" : 1234, "role" : "publisher", "display_name" : "A display name"}
    - Output: {
      "videoroom": "joined",
      "room": 1234,
      "description": "Demo Room",
      "id": 7103631288288938,
      "private_id": 788383200,
      "publishers": []
      }
- Join room as a subscriber:
    - Input: {"plugin" : "video_room", "type" : "join_room", "room_name" : 1234, "role" : "subscriber", "display_name" : "A display name", "feeds":[123,234]}
    - Output:{
      "videoroom": "joined",
      "room": 1234,
      "description": "Demo Room",
      "id": 7103631288288938,
      "private_id": 788383200,
      "publishers": []
      }
- Start a live video: send a SDP offer to start WebRTC connection, signaling server will return back a SDP answer
    - Input: {"plugin" : "video_room", "type" : "publish_stream", "sdp" : "v=0 o=- 3532969134585152172 2 IN IP4 127.0.0.1 s=- t=0 0"}
    - Output: {"sdp" : "v=0 o=- 3532969134585152172 2 IN IP4 217.61.26.125 s=VideoRoom 1234 t=0 0"}
- Send connection information (ICE candidate gathering)
    - Input: {"plugin" : "video_room", "type" : "connection_info", "ice_candidate" : [{
      "candidate": "candidate:1727282165 1 udp 2122260223 172.31.64.1 55071 typ host generation 0 ufrag UFaq network-id 1",
      "sdpMid": "0",
      "sdpMLineIndex": 0
      }]}
    - Output: N/A
- Subscribe a publisher stream: After join room, signaling server will return data of room which contain list of room publishers, a subscriber can choose which stream to view.
    - Input: {"plugin" : "video_room", "type" : "subscribe_stream", "streams": [123,234,345]}
    - Output:
- Leave room:
    - Input: {"plugin" : "video_room", "type" : "leave_room"}
    - Output: N/A
- Keep Websocket alive:
    - Input: {"type" : "ping"}
    - Output: {"type" : "pong"}

To run the server, build it with maven by "mvn clean package -DskipTests" then run "java -jar target/java_client-[version]-jar-with-dependencies.jar"