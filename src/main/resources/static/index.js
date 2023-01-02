var stompClient = null;
var userName = $("#from").val();

function setConnected(connected) {
    $("#from").prop("disabled", connected);
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#sendmessage").show();
    } else {
        $("#sendmessage").hide();
    }
}

function connect() {
    userName = $("#from").val();
    if (userName == null || userName === "") {
        alert('Please input a nickname!');
        return;
    }
    /*<![CDATA[*/
    var url = /*[['ws://'+${#httpServletRequest.serverName}+':'+${#httpServletRequest.serverPort}+@{/broadcast}]]*/ 'ws://localhost:8080/broadcast';
    /*]]>*/
    stompClient = Stomp.client(url);
    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/broadcast', function (output) {
            showBroadcastMessage(createTextNode(JSON.parse(output.body)));
        });

        sendConnection(' connected to server');
        setConnected(true);
    }, function (err) {
        alert('error' + err);
    });
}

function disconnect() {
    if (stompClient != null) {
        sendConnection(' disconnected from server');

        stompClient.disconnect(function() {
            console.log('disconnected...');
            setConnected(false);
        });
    }
}

function sendConnection(message) {
    var text = userName + message;
    sendBroadcast({'from': 'server', 'text': text});
}

function sendBroadcast(json) {
    stompClient.send("/app/broadcast", {}, JSON.stringify(json));
}

function send() {
    var text = $("#message").val();
    sendBroadcast({'from': userName, 'text': text});
    $("#message").val("");
}

function createTextNode(messageObj) {
    return '<div class="row alert alert-info"><div class="col-md-8">' +
        messageObj.text +
        '</div><div class="col-md-4 text-right"><small>[<b>' +
        messageObj.from +
        '</b> ' +
        messageObj.time +
        ']</small>' +
        '</div></div>';
}

function showBroadcastMessage(message) {
    $("#content").html($("#content").html() + message);
    $("#clear").show();
}

function clearBroadcast() {
    $("#content").html("");
    $("#clear").hide();
}