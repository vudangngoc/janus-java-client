$(document).ready(function () {
    const localVideo = document.getElementById('ownerStreamVideo');
    const remoteVideo = document.getElementById('subscriberStreamVideo');

    

    // Tạo biến socket
    var socket = null;
    // khởi tạo Peer Connection
    var peerConnection = new RTCPeerConnection(
        {'iceServers': [{
            'urls': 'stun:stun.l.google.com:19302'
          }]}
    );

    peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            var message = JSON.stringify({
                "plugin": "video_room", "type": "connection_info", "ice_candidate": event.candidate
            });
            displayResult("Publisher send ICE candidate: " + message);
            sendToWebSocket(message)
        }
    };

    peerConnection.onnegotiationneeded = (event) => {
        console.log("On Negotiation Need: " + JSON. stringify(event));
    }

    peerConnection.onicecandidateerror = (event) => {
        console.log("On ICE Negotiation Failure: " + JSON. stringify(event));
    }

    peerConnection.onsignalingstatechange = (event) => {
        console.log("On Signaling state change: " + JSON. stringify(event));
    }

    peerConnection.onconnectionstatechange  = (event) => {
        console.log("On Conection state change: " + JSON. stringify(event));
    }

    peerConnection.addEventListener("icegatheringstatechange", (ev) => {
        switch (peerConnection.iceGatheringState) {
          case "new":
            /* gathering is either just starting or has been reset */
            break;
          case "gathering":
            /* gathering has begun or is ongoing */
            break;
          case "complete":
            /* gathering has ended */
            console.log("On Finish ICE gathering: " + JSON. stringify(ev));
            var message = JSON.stringify({ "plugin": "video_room", "type": "ice_complete"});
            displayResult("Publisher finish ICE gathering: " + message);
            sendToWebSocket(message)
            break;
        }
    });
    
    var subscribePeerConnection = new RTCPeerConnection(
        {'iceServers': [{
            'urls': 'stun:stun.l.google.com:19302'
          }]}
    );
    // peerConnection.ontrack = function (event) {
    //     console.log('onTrack')
    //     remoteVideo.srcObject = event.streams[0];
    // };
    subscribePeerConnection.ontrack = event => {
        if(!event.streams)
            return;
        if(!event.track)
            return;
        const stream = event.streams[0];
        remoteVideo.srcObject = stream;
        var playPromise = remoteVideo.play();
        if (playPromise !== undefined) {
            playPromise.then(_ => {
                $('#subscriberStreamStatus').removeClass('inactive').addClass('active')
                $('#subscriberStreamStatus').html('Live')
            })
            .catch(error => {
              // Auto-play was prevented
              // Show paused UI.
            });
          }
        
    //     // const videoElement = document.querySelector('video');
    //     // if ('srcObject' in videoElement) {
    //     //     videoElement.srcObject = stream;
    //     // } else {
    //     //     videoElement.src = URL.createObjectURL(stream);
    //     // }
    };

    subscribePeerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            var message = JSON.stringify({"plugin": "video_room", "type": "connection_info_subscriber", "ice_candidate": event.candidate});
            displayResult("Subscriber send ICE candidate: " + message);
            sendToWebSocket(message)
        }
    };

    subscribePeerConnection.onnegotiationneeded = (event) => {
        if (subscribePeerConnection.signalingState != "stable") return;
        console.log("On Negotiation Need: " + JSON. stringify(event));
    }

    subscribePeerConnection.onicecandidateerror = (event) => {
        console.log("On ICE Negotiation Failure: " + JSON. stringify(event));
    }

    subscribePeerConnection.onsignalingstatechange = (event) => {
        console.log("On Signaling state change: " + JSON. stringify(event));
    }

    subscribePeerConnection.onconnectionstatechange  = (event) => {
        console.log("On Conection state change: " + JSON. stringify(event));
    }

    subscribePeerConnection.addEventListener("icegatheringstatechange", (ev) => {
        switch (subscribePeerConnection.iceGatheringState) {
          case "new":
            /* gathering is either just starting or has been reset */
            break;
          case "gathering":
            /* gathering has begun or is ongoing */
            break;
          case "complete":
            /* gathering has ended */
            console.log("On Finish ICE gathering: " + JSON. stringify(ev));
            var message = JSON.stringify({ "plugin": "video_room", "type": "ice_complete_subscriber"});
            displayResult("Subscriber finish ICE gathering: " + message);
            sendToWebSocket(message)
            break;
        }
    });
    
    // Ẩn toàn bộ phần nội dung trừ phần nội dung của tab hiện tại
    $(".tabcontent:not(:first)").hide();

    // Xử lý sự kiện click trên các tab
    $(".tablinks").click(function () {
        // Xóa class active khỏi tất cả các tab
        $(".tablinks").removeClass("active");
        // Thêm class active cho tab hiện tại
        $(this).addClass("active");
        // Ẩn toàn bộ phần nội dung
        $(".tabcontent").hide();
        // Lấy id của phần nội dung tương ứng với tab hiện tại
        var tabContentId = $(this).attr("data-tab");
        // Hiển thị phần nội dung tương ứng
        $("#" + tabContentId).show();
    });

    // Hiển thị video

    // Khi click vào nút Connect
    $('form').submit(function (event) {
        event.preventDefault(); // Ngăn chặn form submit

        // Kết nối đến websocket và gửi message
        connectToWebSocket();
    });

    // Khi click vào nút Join Room
    $('#joinRoom').on('click', function () {
        var name = $('#name').val();
        // connectToWebSocket();
        var message = JSON.stringify({ "plugin": "video_room", "type": "join_room", "room_name": $('#room_name').val(), "role": "publisher", "display_name": name });
        displayResult("Publisher join room: " + message);
        sendToWebSocket(message)
    })
    $('#create_room').on('click', function () {
        var name = $('#name').val();
        var message = JSON.stringify({ "plugin": "video_room", "type": "create_room"});
        displayResult("Create room: " + message);
        sendToWebSocket(message)
    })
    $('#room_info').on('click', function () {
        var message = JSON.stringify({ "plugin": "video_room", "type": "room_info"});
        displayResult("Get rooms info: " + message);
        sendToWebSocket(message)
    })
    $('#leave_room').on('click', function () {
        var message = JSON.stringify({ "plugin": "video_room", "type": "leave_room"});
        displayResult("Publisher leave room: " + message);
        sendToWebSocket(message)
    })
    $('#stop-streaming-subscriber').on('click', function () {
        subscriberLeftRoom()
    })
    $('#start-streaming-subscriber').on('click', function () {
        subcribeAPublisher($('#current_id-subscriber').val())
    })
    // Bật camera và micro
    $("#start-streaming").on('click', function () {
        navigator.mediaDevices.getUserMedia({ video: true, audio: true })
            .then(async function (stream) {
                // Lưu stream vào biến global
                window.localStream = stream;

                // Hiển thị stream lên video elemen
                localVideo.srcObject = stream;
                localVideo.play();
                // Tạo peer connection
                $('#ownerStreamStatus').removeClass('inactive').addClass('active')
                $('#ownerStreamStatus').html('Live')
                // Thêm track của stream vào peer connection
                stream.getTracks().forEach(track => {
                    peerConnection.addTrack(track, stream);
                });

                // Tạo SDP offer
                const offer = await peerConnection.createOffer();
                await peerConnection.setLocalDescription(offer);

                var outMessage = JSON.stringify({
                    "plugin": "video_room",
                    "type": "publish_stream",
                    "sdp": offer.sdp
                });
                displayResult("Send SDP offer: " + outMessage);
                // Gửi SDP offer đến server thông qua websocket
                sendToWebSocket(outMessage);
            })
            .catch(function (err) {
                console.log("Không thể bật camera hoặc micro: " + err.message);
            });
    });

    // Tắt camera và micro
    $("#stop-streaming").on('click', function () {
        var message = JSON.stringify({
            "plugin": "video_room",
            "type": "unpublish"
        });
        displayResult("Publisher stop publish video stream: " + message);
        sendToWebSocket(message);
        // Dừng stream
        window.localStream.getTracks().forEach(function (track) {
            track.stop();
        });
        // Dừng hiển thị video
        localVideo.srcObject = null;

    });
    // Hàm kết nối đến websocket
    function connectToWebSocket() {
        // Kiểm tra nếu đã kết nối thì không làm gì
        if (socket !== null && socket.readyState === WebSocket.OPEN) {
            console.log("Websocket already connected.");
            return;
        }

        // Khởi tạo websocket
        socket = new WebSocket($("#server").val());

        // Xử lý sự kiện khi kết nối thành công
        socket.onopen = function (event) {
            console.log("Websocket connected.");
            displayResult("Websocket connected.")

        };

        // Xử lý sự kiện khi nhận được message từ server
        socket.onmessage = async function (event) {
            console.log("Received message:", event.data);
            const message = JSON.parse(event.data);
            displayResult("Received message: " + event.data)
            if(message.type == undefined){
                // message from Janus server
                handleEvent(message);
            }
            switch (message.type) {
                case "join_room_result":
                    if(message.role == "publisher"){
                        $('#current_id').val(message.id)
                        subcribeAPublisher(message.publishers)
                    } else {
                        // handle answer for subscriber
                        handleAnswerForSubscriber(message);
                    }
                    break
                case "publish_stream_result":
                    handleStartStreamResult(message)
                    break
                case "create_room_result":
                    handleCreateRoomResult(message)
                    break   
            }

        };

        // Xử lý sự kiện khi đóng kết nối
        socket.onclose = function (event) {
            displayResult("Websocket closed.")
            console.log("Websocket closed.");
        };
    }

    function handleEvent(event){
        if(event.plugindata.plugin != undefined){
            switch (event.plugindata.plugin) {
                case "janus.plugin.videoroom":
                    handleVideoRoomEvent(event.plugindata.data)
                    break;
            
                default:
                    break;
            }
        } else {
            switch (event.janus) {
                case "webrtcup":
                    subscribeStream()
                    break;
                case "hangup":
                    
                    break;
                default:
                    break;
            }
        }
    }

    function handleVideoRoomEvent(data){
        displayResult("Handle VideoRoom event data: " + JSON.stringify(data));
        if(data.publishers != undefined && data.publishers.length > 0){
            subcribeAPublisher(data.publishers[data.publishers.length - 1].id)
        }
        if(data.unpublished != undefined && data.unpublished == $('#current_id-subscriber').val()){
            subscriberLeftRoom()
        }
    }

    function handleCreateRoomResult(message){
        displayResult("Handle Create room result: " + JSON.stringify(message));
        $('#room_name').val( message.room_name)
    }

    function handleAnswerForSubscriber(data){
        if(data.sdp !== undefined){
            var offer = new RTCSessionDescription({
                sdp: data.sdp,
                type: 'offer'
            }
            );
            displayResult("Set remote description for remote video");

            subscribePeerConnection.setRemoteDescription(offer)
            .then(() => {
                subscribePeerConnection.createAnswer()
                .then((answer) => {
                    displayResult("Set local description for remote video");
                    subscribePeerConnection.setLocalDescription(answer)
                    return answer
                }).then((answer) => {
                    var message = JSON.stringify({
                        "plugin": "video_room",
                        "type": "sdp_answer_subscriber",
                        "room_name": $('#room_name').val(),
                        "sdp": answer.sdp
                    });
                    displayResult("Send answer SDP for remote video: " + message);
                    sendToWebSocket(message);
                });
            // })
            
        });
    }
}
function subscriberLeftRoom(){
    var message = JSON.stringify({ "plugin": "video_room", "type": "leave_room_subscriber"})
    displayResult("subscriber left room: " + message)
    sendToWebSocket(message)
}
    function subcribeAPublisher(publishers) {
        if(publishers && publishers.length > 0){
            
            var feeds = publishers.map(p => p.id)
            var message = JSON.stringify({ "plugin": "video_room", "type": "join_room", "room_name": $('#room_name').val(), "role": "subscriber", "display_name": $('#name').val(), "feeds":feeds });
            displayResult("Subscribing: " + message)
            sendToWebSocket(message)
        }
    }
    function handleStartStreamResult(message) {
        if (message.sdp !== undefined) {
            var answer = new RTCSessionDescription({
                sdp: message.sdp,
                type: 'answer'
            }
            );
            console.log("Set answer SDP to remote description");
            peerConnection.setRemoteDescription(answer).catch(function(error) {
                console.log('Error set SDP answer:', error);
              });//.then(() => createMyStream());
            

        }
    }

    // Hàm gửi message đến websocket
    function sendToWebSocket(message) {
        // Kiểm tra nếu chưa kết nối thì không gửi message
        if (socket === null || socket.readyState !== WebSocket.OPEN) {
            console.log("Websocket is not connected.");
            return;
        }

        // Gửi message đến websocket
        socket.send(message);
    }
    // Hàm hiển thị result
    function displayResult(message) {
        const currentValue = $('#result').val();
        $('#result').val(currentValue + '\n' + message)
    }

});
