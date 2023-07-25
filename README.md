
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
# VideoRoomAdaptor
VideoRoomAdaptor provide APIs to work with Video Room plugin: create/join/leave/delete a video room

# Examples
Please reffer to file TestMainFlow and foler /signaling for detail information