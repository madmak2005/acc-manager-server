var stompClient = null;

function setConnectedPhysics(connected) {
    $("#connectPhysics").prop("disabled", connected);
    $("#disconnectPhysics").prop("disabled", !connected);
    if (connected) {
        $("#conversationPhysics").show();
    }
    else {
        $("#conversationPhysics").hide();
    }
    $("#physics").html("");
}

function setConnectedGraphics(connected) {
    $("#connectGraphics").prop("disabled", connected);
    $("#disconnectGraphics").prop("disabled", !connected);
    if (connected) {
        $("#conversationGraphics").show();
    }
    else {
        $("#conversationGraphics").hide();
    }
    $("#graphics").html("");
}

function setConnectedStatic(connected) {
    $("#connectStatic").prop("disabled", connected);
    $("#disconnectStatic").prop("disabled", !connected);
    if (connected) {
        $("#conversationStatic").show();
    }
    else {
        $("#conversationStatic").hide();
    }
    $("#static").html("");
}

function connectPhysics() {
    var socket = new SockJS('/gs-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnectedPhysics(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/acc/physics', function (physics) {
            showPhysics(JSON.parse(physics.body));
        });
    });
}

function connectGraphics() {
    var socket = new SockJS('/gs-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnectedGraphics(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/acc/graphics', function (graphics) {
            showGraphics(JSON.parse(graphics.body));
        });
    });
}

function connectStatic() {
    var socket = new SockJS('/gs-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnectedStatic(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/acc/static', function (static) {
            showStatic(JSON.parse(static.body));
        });
    });
}

function disconnectPhysics() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnectedPhysics(false);
    console.log("Disconnected");
}

function disconnectGraphics() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnectedGraphics(false);
    console.log("Disconnected");
}

function disconnectStatic() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnectedStatic(false);
    console.log("Disconnected");
}

function showPhysics(message) {
    var c = JSON.parse(message.content);
    console.log('packetId: ' + c.packetId);
    $("#physics").replaceWith('<tbody id="physics"><tr><td>' + JSON.stringify(c, null, 2) + '</td></tr></tbody>');
}

function showGraphics(message) {
    var c = JSON.parse(message.content);
    console.log('packetId: ' + c.packetId);
    $("#graphics").replaceWith('<tbody id="graphics"><tr><td>' + JSON.stringify(c, null, 2) + '</td></tr></tbody>');
}

function showStatic(message) {
    var c = JSON.parse(message.content);
    console.log('packetId: ' + c.packetId);
    $("#static").replaceWith('<tbody id="static"><tr><td>' + JSON.stringify(c, null, 2) + '</td></tr></tbody>');
}


$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connectPhysics" ).click(function() { connectPhysics(); });
    $( "#disconnectPhysics" ).click(function() { disconnectPhysics(); });
    $( "#connectGraphics" ).click(function() { connectGraphics(); });
    $( "#disconnectGraphics" ).click(function() { disconnectGraphics(); });    
    $( "#connectStatic" ).click(function() { connectStatic(); });
    $( "#disconnectStatic" ).click(function() { disconnectStatic(); });

});

