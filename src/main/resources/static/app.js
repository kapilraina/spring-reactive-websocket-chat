
var username;
var clientWebSocket;
//connect();
fetchinitialdata();
$("#connect").prop('disabled', false);
$("#disconnect").prop('disabled', true);
function connect() {
    console.log(window.location);
    console.log(window.location.host);

    if (clientWebSocket !== undefined) {
        console.log("Existing socket state : " + clientWebSocket.readyState);
    }



    try {
        if (window.location.protocol === "http:") {
            clientWebSocket = new WebSocket("ws://" + window.location.host + "/ws/chat");
        }
        if (window.location.protocol === "https:") {
            clientWebSocket = new WebSocket("wss://" + window.location.host + "/ws/chat");
        }

    } catch (error) {
        console.error("Error Opening Socket : " + error);
    }
    console.log("New socket state : " + clientWebSocket.readyState);
    clientWebSocket.onopen = function () {
        // console.log("clientWebSocket.onopen", clientWebSocket);
        //console.log("clientWebSocket.readyState", clientWebSocket.);
        // fetchinitialdata();
        console.log("Connected socket state : " + clientWebSocket.readyState);
        events("Opened connection : " + JSON.stringify(clientWebSocket));
        $("#connect").prop('disabled', true);
        $("#disconnect").prop('disabled', false);
        sendChat("Joined", "JOIN")
        populateActiveUsers();
    }
    clientWebSocket.onclose = function (closeEvent) {
        // console.log("clientWebSocket.onclose", clientWebSocket, error);
        console.log("DisConnected socket state : " + clientWebSocket.readyState);
        $("#connect").prop('disabled', false);
        $("#disconnect").prop('disabled', true);
        events("Closed connection : " + JSON.stringify(clientWebSocket));
    }
    clientWebSocket.onerror = function (error) {
        //  console.log("clientWebSocket.onerror", clientWebSocket, error);
        events("An error occured : " + JSON.stringify(error));
    }
    clientWebSocket.onmessage = function (data) {
        // console.log("clientWebSocket.onmessage", clientWebSocket, error);
        message(data.data);
        return false;
    }
}
function events(responseEvent) {
    document.querySelector(".events").innerHTML += responseEvent + "<br>";
    console.log(responseEvent);
}


function message(message) {

    let messageObj = JSON.parse(message);
    console.log(message);

    if (messageObj['username'] === username) {
       
        $('.messagescontainer').append(
            "<br/><div class='messagerow ownmessage'>" +
            "<div class='column left'><label class='username'>[me] </label></div>" +
            "<div class='column middle '><label class='message'>" + messageObj['message'] + "</label></div>" +
            "<div class='column right'><label class='timestamp'>" + messageObj['timestamp'] + "</label></div>" +
            "</div>");
    }
    else {
        $('.messagescontainer').append(
            "<br/><div class='messagerow othersmessage'>" +
            "<div class='column left'><label class='username'>[" + messageObj['username'] + "] </label></div>" +
            "<div class='column middle'><label class='message'>" + messageObj['message'] + "</label></div>" +
            "<div class='column right'><label class='timestamp'>" + messageObj['timestamp'] + "</label></div>" +
            "</div>");
    }

    $(".messagescontainer").scrollTop($(".messagescontainer")[0].scrollHeight + Number(30));

    checkActiveUserList(messageObj);
}




function disconnect() {
    console.log("Who Clicked Disconnect ?");
    sendChat("Leaving", "LEAVE");
    $('.activeuserscontainer').empty();
    clientWebSocket.close(1000);
    // clientWebSocket.onclose({});// Hack
}

function sendChat(message, chattype) {
    if (clientWebSocket !== undefined && clientWebSocket.readyState === 1) {
        console.log("SendChat socket state : " + clientWebSocket.readyState);
        //clientWebSocket.send(JSON.stringify({'username': $("#username").val(),'messageText': $("#chatmessage").val()}));
        if (message != undefined && message.trim() !== "") {
            clientWebSocket.send(JSON.stringify({ 'username': username, message, 'type': chattype }));
            $("#chatmessage").val("");
        }
    }
    else {
        console.log("Socket Not Ready");
        //connect();
    }

    // $("#messages").scrollTop($("#messages")[0].scrollHeight+Number(30));
}

function fetchinitialdata() {

    $.get("/chat/initialdata", function (data) {

        username = data['currentUsername'];
        //randomvector =  data.randomvector;
        console.log("Logged in User : " + username);
        console.log("All Logged in Users  : " + JSON.stringify(data.activeUsers));
        $("#usersection").text("Welcome " + username + " !");

    });
}


function populateActiveUsers() {

    $.get("/chat/initialdata", function (data) {

        $('.activeuserscontainer').empty();
        $('.activeuserscontainer').append("<div id='au_" + username + "'>" + username + "(you)</div>");
        $.each(data.activeUsers, function (pos, val) {
            addActiveUserInContainer(val);
        });
    });
}

function checkActiveUserList(messageObj) {
    let u = messageObj['username'];
    // console.log(auid + "  " + $('#'+auid).length );
    if (u !== username && messageObj['type'] === 'JOIN') {
        addActiveUserInContainer(u);
    }
    if (u !== username && messageObj['type'] === 'LEAVE') {
        removeActiveUserInContainer(u);
    }
}

function addActiveUserInContainer(u) {
    let auid = "au_" + u;
    if (username !== u && $('#' + auid).length === 0) {

        $('.activeuserscontainer').append(
            "<div id='" + auid + "'>" + u + "</div>"
        );
        $('#' + auid).fadeIn("slow");
    }
}

function removeActiveUserInContainer(u) {
    let auid = "au_" + u;
    $('#' + auid).remove();
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () { connect(); });
    $("#disconnect").click(function () { disconnect(); });
    $("#send").click(function () { sendChat($("#chatmessage").val(), "CHAT"); });

    $('#chatmessage').on("keypress", function (e) {
        if (e.keyCode == 13) {
            sendChat($("#chatmessage").val(), "CHAT");
            return false; // prevent the button click from happening
        }
    });
});