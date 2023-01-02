$(document).ready(function () {
    var userName = 'nobody';

    var url = /*[['ws://'+${#httpServletRequest.serverName}+':'+${#httpServletRequest.serverPort}+@{/broadcast}]]*/ 'ws://localhost:8080/broadcast';
    /*]]>*/
    stompClient = Stomp.client(url);
    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/broadcast', function (message) {
            var li = document.createElement('li');
            li.textContent = message.body;
            document.getElementById('messages').appendChild(li);
        });
        sendConnection('connected to server');
    }, function (err) {
        alert('error' + err);
    });

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
        var text = message;
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


});