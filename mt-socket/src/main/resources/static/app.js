var ws = null;

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('echo').disabled = !connected;
}

function connect() {
    const token = document.getElementById('token').value;
    var host = window.location.host;
    log(host.toString());
    var path = document.getElementById('path').value;
    var url = "ws://" + host.toString() + path
    var roomId = document.getElementById('roomId').value;
    ws = new WebSocket(url + "?roomId=" + roomId + "&bearer=" + token + "&userName=账务");

    ws.onopen = function () {
        setConnected(true);
        log('Info: Connection Established.');
        //ws.send("{\"order\":\"/order\", \"data\": {}, \"req\":12}") {"order":"/offer","data":{"price":"180"},"req":1}
        setInterval(function () {
            ws.send("{\"order\":\"/ping\",\"data\":{},\"req\":12}")
        }, 3000)
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