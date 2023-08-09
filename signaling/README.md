
# Signaling server
This server is build for testing Java Janus client library, it isn't ready for production. The server provide a websocket with messages:

- Mandatory fields:
    - type: a string describe action of message
    - plugin: the plugin will handle this message, currently, we support only "videoroom"
- Create room:
    - Input: {"plugin" : "video_room", "type" : "create_room"}
    - Output: {"room_name":1234}
- Get rooms info:
    - Input: {"plugin":"video_room","type":"room_info"}
    - Output: {"videoroom":"success","list":[{...}],"type":"room_info_result"}
- Join room as a publisher: user will be added to publisher of room
    - Input: {"plugin" : "video_room", "type" : "join_room", "room_name" : 1234, "role" : "publisher", "display_name" : "A display name"}
    - Output: {"role":"publisher","videoroom":"joined","description":"Demo Room","publishers":[],"private_id":2790219919,"id":1995015638107375,"type":"join_room_result","room":1234}
- Join room as a subscriber:
    - Input: {"plugin":"video_room","type":"join_room","room_name":"1234","role":"subscriber","display_name":"b","feeds":[1995015638107375]}
    - Output: {"role":"subscriber","type":"join_room_result","sdp":"..."}
- Start a live video: send a SDP offer to start WebRTC connection, signaling server will return back a SDP answer
    - Input: {"plugin":"video_room","type":"publish_stream","sdp":"..."}
    - Output: {"type":"publish_stream_result","sdp":"..."}
- Send connection information (ICE candidate gathering)
    - Input: {"plugin":"video_room","type":"connection_info","ice_candidate":{"candidate":"candidate:1218416269 1 udp 1686052607 14.248.68.107 50262 typ srflx raddr 192.168.0.158 rport 50262 generation 0 ufrag krCJ network-id 1 network-cost 10","sdpMid":"0","sdpMLineIndex":0,"usernameFragment":"krCJ"}}
    - Output: N/A
- Send finish ICE gathering
    - Input: {"plugin":"video_room","type":"ice_complete"}
    - Output: N/A
- Subscribe a publisher stream: After join room, signaling server will return data of room which contain list of room publishers, a subscriber can choose which stream to view.
    - Input: {"plugin" : "video_room", "type" : "subscribe_stream", "streams": [123,234,345]}
    - Output:
- Leave room:
    - Input: {"plugin" : "video_room", "type" : "leave_room"}
    - Output: N/A
- Keep Websocket alive:
    - Input: N/A
    - Output: {"type":"keep_alive"}

To run the server, build it with maven by "mvn clean package -DskipTests" then run "java -jar target/java_client-[version]-jar-with-dependencies.jar"
Then access to URL http://localhost:7070/ you'll get a test page 
- For publisher: Fill a name --> click Connect --> click Join Room --> click Start Stream
- For subscriber: Fill a name --> click Connect --> click Join Room, test page will automatically play available stream