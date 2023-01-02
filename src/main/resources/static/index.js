$(document).ready(function () {
    const url = 'ws://localhost:8080/broadcast';

    stompClient = Stomp.client(url);

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/broadcast', function (message) {
            let li = document.createElement('li');
            li.textContent = message.body;
            document.getElementById('messages').appendChild(li);
        });
        sendConnection('connected to server');
    }, function (err) {
        alert('error' + err);
    });



    function sendConnection(message) {
        var text = message;
        sendBroadcast({'from': 'server', 'text': text});
    }

    function sendBroadcast(json) {
        stompClient.send("/app/broadcast", {}, JSON.stringify(json));
    }
});