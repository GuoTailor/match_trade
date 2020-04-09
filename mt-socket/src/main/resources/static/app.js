var ws = null;
var url = "ws://localhost:85/socket/echo";
var token = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwicm9sZXMiOiJbW1wiU1VQRVJfQURNSU5cIixudWxsXSxbXCJVU0VSXCI" +
    "sMV1dIiwibmJmIjoxNTg2NDAxMzc3LCJleHAiOjE1ODcwMDYxNzd9.1HZ_mUTXsGQ4SRx1W-pxlCQHfhJc84-CutOsNwBsRWw";


function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('echo').disabled = !connected;
}

function connect() {
    ws = new WebSocket(url, [token]);
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