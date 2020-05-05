var ws = null;
var url = "ws://101.37.34.61:85/socket/room";
var token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6NSwicm9sZXMiOiJbXCJBRE1JTlwiLFwiVVNFUlwiXSIsIm5iZiI6MTU4ODY0ODkw" +
    "MCwiZXhwIjoxNTg5MjUzNzAwfQ.dDvF5esVCtI0Hdne7SZv4udVKGdSCUTHr4OnsacArsU";

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('echo').disabled = !connected;
}

function connect() {
    ws = new WebSocket(url + "?roomId=D12&bearer=" + token);
    ws.onopen = function () {
        setConnected(true);
        log('Info: Connection Established.');
    };

    ws.onmessage = function (event) {
        log(event.data);
    };

    ws.onclose = function (event) {
        setConnected(false);
        log('Info: Closing Connection.');
    };
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
    setConnected(false);
}

function echo() {
    if (ws != null) {
        var message = document.getElementById('message').value;
        log('Sent to server :: ' + message);
        ws.send(message);
    } else {
        alert('connection not established, please connect.');
    }
}

function log(message) {
    var console = document.getElementById('logging');
    var p = document.createElement('p');
    p.appendChild(document.createTextNode(message));
    console.appendChild(p);
}