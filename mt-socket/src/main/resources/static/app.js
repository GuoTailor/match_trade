var ws = null;
var path = "/socket/room";

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('echo').disabled = !connected;
}

function connect() {
    var token = document.getElementById('token').value;
    var host = window.location.host;
    log(host.toString());
    var url = "ws://" + host.toString() + path
    ws = new WebSocket(url + "?roomId=27&bearer=" + token);

    ws.onopen = function () {
        setConnected(true);
        log('Info: Connection Established.');
        //ws.send("{\"order\":\"/order\", \"data\": {}, \"req\":12}")
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

function clean() {
    var console = document.getElementById('logging');
    console.innerHTML = "";
}