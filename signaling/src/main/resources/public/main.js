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
            sendToWebSocket(JSON.stringify({
                "plugin": "video_room", "type": "connection_info", "ice_candidate": event.candidate
            }))
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
            sendToWebSocket(JSON.stringify({ "plugin": "video_room", "type": "ice_complete"}))
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
        remoteVideo.play();
        $('#subscriberStreamStatus').removeClass('inactive').addClass('active')
        $('#subscriberStreamStatus').html('Live')
    //     // const videoElement = document.querySelector('video');
    //     // if ('srcObject' in videoElement) {
    //     //     videoElement.srcObject = stream;
    //     // } else {
    //     //     videoElement.src = URL.createObjectURL(stream);
    //     // }
    };

    subscribePeerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            sendToWebSocket(JSON.stringify({
                "plugin": "video_room", "type": "connection_info_subscriber", "ice_candidate": event.candidate
            }))
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
            sendToWebSocket(JSON.stringify({ "plugin": "video_room", "type": "ice_complete_subscriber"}))
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
        sendToWebSocket(JSON.stringify({ "plugin": "video_room", "type": "join_room", "room_name": 1234, "role": "publisher", "display_name": name }))
    })
    // Khi click vào nút Join Room with subscriber
    $('#joinRoomWithSub').on('click', function () {
        var name = $('#name').val();
        // connectToWebSocket();
        sendToWebSocket(JSON.stringify({ "plugin": "video_room", "type": "join_room", "room_name": 1090575864348547, "role": "subscriber", "display_name": name }))
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

                // Gửi SDP offer đến server thông qua websocket
                sendToWebSocket(JSON.stringify({
                    "plugin": "video_room",
                    "type": "publish_stream",
                    "sdp": offer.sdp
                }));
            })
            .catch(function (err) {
                console.log("Không thể bật camera hoặc micro: " + err.message);
            });
    });

    // Tắt camera và micro
    $("#stop-streaming").on('click', function () {
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
            switch (message.type) {
                case "join_room_result":
                    var data = JSON.parse(event.data);
                    if(data.role == "publisher"){
                        subcribeAPublisher(data.publishers[0]); // subscribe existing stream
                    } else {
                        // handle answer for subscriber
                        handleAnswerForSubscriber(data);
                    }
                    break
                case "publish_stream_result":
                    handleStartStreamResult(message)
                    break
            }

        };

        // Xử lý sự kiện khi đóng kết nối
        socket.onclose = function (event) {
            displayResult("Websocket closed.")
            console.log("Websocket closed.");
        };
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
                    displayResult("Send answer SDP for remote video");
                    sendToWebSocket(JSON.stringify({
                        "plugin": "video_room",
                        "type": "sdp_answer_subscriber",
                        "room_name": 1234,
                        "sdp": answer.sdp
                    }));
                });
            // })
            
        });
    }
}
    function subcribeAPublisher(publisher) {
        if(publisher){
            displayResult("Subscribing: " + JSON.stringify(publisher))
            sendToWebSocket(JSON.stringify({ "plugin": "video_room", "type": "join_room", "room_name": 1234, "role": "subscriber", "display_name": $('#name').val(), "feeds":[publisher.id] }))
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
