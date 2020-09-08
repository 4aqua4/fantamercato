var stompClient = null;
var username = null;
var userRole = null;

var myBudget = 0;
var myGKCount = 0;
var myDFCount = 0;
var myMFCount = 0;
var myFWCount = 0;

var users = [];

const ROLE_GK = 'P';
const ROLE_DF = 'D';
const ROLE_MF = 'C';
const ROLE_FW = 'A';

const MAX_GK = 3;
const MAX_DF = 8;
const MAX_MF = 8;
const MAX_FW = 6;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#main-container").show();
        $("#login-container").hide();
    }
    else {
        $("#main-container").hide();
        $("#login-container").show();
    }
//    $("#greetings").html("");
}

function connect() {
	username = $("#username").val(); //JSON.stringify({'name': $("#username").val()})
    var socket = new SockJS('/fanta-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({'userId': username}, function (frame) {
        setConnected(true);
//        console.log('Connected: ' + frame);
/*
        stompClient.subscribe('/topic/players', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
        
        stompClient.subscribe('/user/queue/reply', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
*/
        stompClient.subscribe('/user/queue/single', function (serverMsg) {
            //showGreeting(JSON.parse(serverMsg.body).content);
        	console.log('ricevuto single...............'+JSON.parse(serverMsg.body).type);
        	switch(JSON.parse(serverMsg.body).type){
	        	case "playersList":
	        		fillGoalkeepersList(JSON.parse(serverMsg.body).goalkeepers);
	        		fillDefendersList(JSON.parse(serverMsg.body).defenders);
	        		fillMidfieldersList(JSON.parse(serverMsg.body).midfielders);
	        		fillStrikersList(JSON.parse(serverMsg.body).strikers);
	        		//if ADM
	        		fillAdmPlayersTable("adm-GK", ROLE_GK, JSON.parse(serverMsg.body).goalkeepers);
	        		fillAdmPlayersTable("adm-DF", ROLE_DF, JSON.parse(serverMsg.body).defenders);
	        		fillAdmPlayersTable("adm-MF", ROLE_MF, JSON.parse(serverMsg.body).midfielders);
	        		fillAdmPlayersTable("adm-FW", ROLE_FW, JSON.parse(serverMsg.body).strikers);
	        		break;
	        	case "teamsList":
	        		fillTeams(username, JSON.parse(serverMsg.body).teams);
	        		sendMsg('currentOffer','');
	        		break;
	        	case "currentOffer":
	        		setLastOffer(username, JSON.parse(serverMsg.body).content);
	        		setNextBidder(username, JSON.parse(serverMsg.body).nextBidder);
	        		break;
	        	case "saveResult":
	        		showSeveResult(JSON.parse(serverMsg.body).content);
	        		break;
	        	case "":
	        		alert("nessuno");
	        		break;
        	
        	}
        	
        });
        
        stompClient.subscribe('/topic/all', function (serverMsg) {
            console.log('ricevuto broad...............'+JSON.parse(serverMsg.body).type);
        	switch(JSON.parse(serverMsg.body).type){
	        	case "bidderTurn":
	        		setBidderTurn(username, JSON.parse(serverMsg.body).nextBidder)
	        		break;
	        	case "offer":
	        		setLastOffer(username, JSON.parse(serverMsg.body).content);
	        		setNextBidder(username, JSON.parse(serverMsg.body).nextBidder);
	        		break;
	        	case "quitOffer":
	        		setNextBidder(username, JSON.parse(serverMsg.body).nextBidder);
	        		break;
	        	case "endTurn":
	        		setTurnResult(username, JSON.parse(serverMsg.body).content);
	        		break;
	        	case "userStatusChanged":
	        		setUserStatusChanged(username, JSON.parse(serverMsg.body).content);
	        		break;
	        	case "":
	        		alert("nessuno");
	        		break;
	    	
	    	}
        });
        
        
        
        sendMsg('playersList','');
        sendMsg('teamsList','');
        
        
    });
}

/*
function connect() {
    var socket = new SockJS('/fanta-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}
*/
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}
/*
function sendName() {
    stompClient.send("/app/fantamercato", {}, JSON.stringify({'name': $("#name").val()}));
}
*/

function start(){
	broadcastMsg("bidderTurn", "");
}

function sendMsg(msgType, msgContent) {
//    console.log("msgType:"+msgType+" msgContent:"+msgContent);
    stompClient.send("/app/fantamercato/single", {}, JSON.stringify({'username': $("#username").val(), 'type': msgType, 'content': msgContent}));
//    stompClient.send("/app/fantamercato/single", {}, JSON.stringify({'name': $("#username").val()}));
//    stompClient.send("/app/fantamercato/broad", {}, JSON.stringify({'name': $("#username").val()+"---"}));
}

//BROAD da
function broadcastMsg(msgType, msgContent) {
//    console.log("msgType:"+msgType+" msgContent:"+msgContent);
    stompClient.send("/app/fantamercato/broad", {}, JSON.stringify({'username': $("#username").val(), 'type': msgType, 'content': msgContent}));
}
//a

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}


//PLAYERS
function fillGoalkeepersList(gkList){
	let dropdown = $('#goalkeepers-dropdown');
	dropdown.empty();
	
	dropdown.append('<option selected="true" disabled> </option>');
	dropdown.prop('selectedIndex', 0);
	
	$.each(gkList, function (key, entry) {
		var txt = ""+entry.name+" ("+entry.realTeam+")";
		dropdown.append($('<option></option>').attr('value', entry.id).text(txt));
	});
}

function fillDefendersList(dList){
	let dropdown = $('#defenders-dropdown');
	dropdown.empty();
	
	dropdown.append('<option selected="true" disabled> </option>');
	dropdown.prop('selectedIndex', 0);
	
	$.each(dList, function (key, entry) {
		dropdown.append($('<option></option>').attr('value', entry.id).text(entry.name+" ("+entry.realTeam+")"));
	});
}

function fillMidfieldersList(mfList){
	let dropdown = $('#midfielders-dropdown');
	dropdown.empty();
	
	dropdown.append('<option selected="true" disabled> </option>');
	dropdown.prop('selectedIndex', 0);
	
	$.each(mfList, function (key, entry) {
		var txt = ""+entry.name+" ("+entry.realTeam+")";
		dropdown.append($('<option></option>').attr('value', entry.id).text(txt));
	});
}

function fillStrikersList(sList){
	let dropdown = $('#strikers-dropdown');
	dropdown.empty();
	
	dropdown.append('<option selected="true" disabled> </option>');
	dropdown.prop('selectedIndex', 0);
	
	$.each(sList, function (key, entry) {
		var txt = ""+entry.name+" ("+entry.realTeam+")";
		dropdown.append($('<option></option>').attr('value', entry.id).text(txt));
	});
}

//TEAMS
function fillTeams(username, tList){

	$.each(tList, function (key, entry) {	
		let divId = "team_"+entry.username
		if(! $('#'+divId).length){
			//creo div
			if(entry.username == username){
				
userRole = entry.role;
if(entry.role == 'A'){
//	$( "#admin-btn" ).show();
	$( "#start-btn" ).show();
}else{
//	$( "#admin-btn" ).hide();
	$( "#start-btn" ).hide();
}
				$('#myteam').append('<div id="'+divId+'" class="panel panel-success"/>');
			}else{
				$('#teams').append('<div id="'+divId+'" class="panel panel-info col-md-3"/>');
				$('#teams').append('<div class="col-md-1"/>');
setConnectionStatus(divId,entry.status);
			}
		}
		if(entry.username == username){
			//aggiorno budget e numero giocatori
			myBudget = entry.budget;
			myGKCount = entry.goalkeepers.length;
			myDFCount = entry.defenders.length;
			myMFCount = entry.midfielders.length;
			myFWCount = entry.forwards.length;
			
		}

		users[entry.username] = entry.teamname+" ("+entry.president+")";
		$('#'+divId).html(createTeamDivContent(entry));
		
	});
}


function createTeamDivContent(entry){
	let divTitle = users[entry.username];//entry.teamname+" ("+entry.president+")";
	let divContent = 	'<div class="panel-heading">';
	divContent += '<h3 class="panel-title">';
	divContent += divTitle;
	divContent += '<div class="pull-right">';
	divContent += '<span class"badge"><b>'+entry.budget+'</b></span>';
	divContent += '</div>';
	divContent += '</h3>';
	divContent += '</div>';
	divContent += '<div class="panel-body">';
	divContent += createRolePlayersUl(entry.goalkeepers, translateRole(ROLE_GK));
	divContent += createRolePlayersUl(entry.defenders, translateRole(ROLE_DF));
	divContent += createRolePlayersUl(entry.midfielders, translateRole(ROLE_MF));
	divContent += createRolePlayersUl(entry.forwards, translateRole(ROLE_FW));
	divContent += '</div>';
	return divContent;
}

function createRolePlayersUl(players, role){
	let playersRole = 	'<h5>'+role+'</h5>';
	/*
	if(isMy){
		playersRole += 	'<ul id="my-'+role+'" class="list-group list-group-flush">';
	}else{
		playersRole += 	'<ul class="list-group list-group-flush">';
	}
	*/
	playersRole += 	'<ul class="list-group list-group-flush">';
	$.each(players, function (key, entry) {
		playersRole += '<li class="list-group-item">'+entry.name+'('+entry.realTeam+')';
		playersRole += '<span class="badge">'+entry.value+'</span>';
		playersRole += '</li>';
		
		
	});
	playersRole +=		'</ul>';
	return playersRole;
}

//OFFER
function setLastOffer(username, content){
	
	var obj = $.parseJSON(content);
	try{
		$('#lastPrice').val(obj['price']);
		$('#currPlayer').val(obj['playerId']);
		$('#currPlayerRole').val(obj['playerRole']);
		$('#currPlayerName').text(obj['playerName']);	
		$('#price').val(obj['price']);
		$('#currOfferBy').text(users[obj['offerBy']]);	
	}catch (e) {
		// TODO: handle exception
		console.log('eccezione gestita LastOffer...');
	}	
}

function clearOfferDiv(){
	$('#lastPrice').val(0);
	$('#currPlayer').val('');
	$('#currPlayerRole').val('');
	$('#currPlayerName').text('');	
	$('#price').val(0);
	$('#currOfferBy').text('');	
}

function setBidderTurn(username, uId){
	//operazioni di inizio turno
	$( "#start-btn" ).hide();
	
	
	if(uId == username){
		$( "#players-dropdown" ).show();
	}else{
		$( "#players-dropdown" ).hide();
	}
	//mostra div offerta e nasconde div risultato
	clearOfferDiv();
	$( "#offer-div" ).show();
	$( "#offer-result-div" ).html("");
	$( "#offer-result-div" ).hide();

	setNextBidder(username, uId);

	$( "#offer-div" ).show();
}

function setNextBidder(username, uId){
	$('#nextBidder').text(users[uId]);	
	if(uId == username){
		$( "#makeOffer" ).show();
		$( "#quitOffer" ).show();
		$("#price").prop("readonly", false);
	}else{
		$( "#makeOffer" ).hide();
		$( "#quitOffer" ).hide();
		$("#price").prop("readonly", true);
	}
}

function setTurnResult(username, content){
	//operazioni di fine turno
	if(userRole == 'A'){
		$( "#start-btn" ).show();
	}
	
	$( "#offer-div" ).hide();
	$( "#offer-result-div" ).html(createTurnResultDivContent(content));
	$( "#offer-result-div" ).show();
	
    sendMsg('playersList','');
    sendMsg('teamsList','');

}

function createTurnResultDivContent(content){
	var obj = $.parseJSON(content);

	let divContent = '<h2>AGGIUDICATO</h2>';
	divContent += '<div>';
	divContent += '<b>'+obj['playerName']+'</b>';
	divContent += '<br/>Comprato da ';
	divContent += '<b>'+users[obj['offerBy']]+'</b>';
	divContent += '<br/>prezzo: ';
	divContent += '<b>'+obj['price']+'</b>';
	divContent += '</div>';
	return divContent;
}

function choosePlayer(sel){
	$( "#currPlayer" ).val(sel.value);
	$( "#currPlayerName" ).text(sel.options[sel.selectedIndex].text);	
	$( "#currPlayerRole" ).val(sel.name);	
}

function makeOffer(){
	if(checkOffer()){
		$( "#players-dropdown" ).hide();
		msgContent = JSON.stringify({'offerBy': $("#username").val(), 'playerId': $("#currPlayer").val(), 'playerName': $("#currPlayerName").text(), 'playerRole': $("#currPlayerRole").val(), 'price': $("#price").val()});
		broadcastMsg("offer", msgContent);
	}
}

function quitOffer(){
	$( "#players-dropdown" ).hide();
	msgContent = "";
	broadcastMsg("quitOffer", msgContent);
}


$(function () {
	
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#start-btn" ).click(function() { start(); });
//    $( "#send" ).click(function() { sendName(); });
//    $( "#sendMsg" ).click(function() { sendMsg(); });
    $( "#makeOffer" ).click(function() { makeOffer(); });
    $( "#quitOffer" ).click(function() { quitOffer(); });
    
    /*
    $("#goalkeepers-dropdown").change(function() { choosePlayer(this); });
    $("#defenders-dropdown").change(function() { choosePlayer(this); });
    $("#midfielders-dropdown").change(function() { choosePlayer(this); });
    $("#strikers-dropdown").change(function() { choosePlayer(this); });
    */
    $("select").change(function() { choosePlayer(this); });
    //ADMIN
    $( "#admin-btn" ).click(function() { openAdmin(); });
    $( "#closeAdmin" ).click(function() { closeAdmin(); });
    /*
    $( "#saveGKs" ).click(function() { savePlayers(ROLE_GK); });
    $( "#saveDFs" ).click(function() { savePlayers(ROLE_DF); });
    $( "#saveMFs" ).click(function() { savePlayers(ROLE_MF); });
    $( "#saveFWs" ).click(function() { savePlayers(ROLE_FW); });
    */

});


function checkOffer(){
	var prevOfferedPrice = $('#lastPrice').val();
	var offeredPrice = $("#price").val();
//console.log("prevOfferedPrice="+prevOfferedPrice+" offeredPrice="+offeredPrice);
	if(offeredPrice <= 0){
		alert("L'offerta dev'essere maggiore di 0!");
		return false;
	}else if(offeredPrice <= prevOfferedPrice){
		alert("L'offerta dev'essere maggiore di quella precedente!");
		return false;
	}
	
	if(!checkCountRole()){
		return false;
	}
	if(!checkBudget()){
		return false;
	}
	return true;
}

function checkCountRole(){
	var retVal = false;
	var curRole = $("#currPlayerRole").val();
	switch (curRole) {
	  case ROLE_GK:
	    if(myGKCount < MAX_GK)
	    	retVal = true;
	    else
	    	alert('Hai gi\u00E0 '+myGKCount+' '+translateRole(ROLE_GK));
	    break;
	  case ROLE_DF:
		    if(myDFCount < MAX_DF)
		    	retVal = true;
		    else
		    	alert('Hai gi\u00E0 '+myDFCount+' '+translateRole(ROLE_DF));
	    break;
	  case ROLE_MF:
		    if(myMFCount < MAX_MF)
		    	retVal = true;
		    else
		    	alert('Hai gi\u00E0 '+myMFCount+' '+translateRole(ROLE_MF));
	    break;
	  case ROLE_FW:
		    if(myFWCount < MAX_FW)
		    	retVal = true;
		    else
		    	alert('Hai gi\u00E0 '+myFWCount+' '+translateRole(ROLE_FW));
	    break;
	  default:
	    console.log("Spiacenti, non abbiamo " + curRole + ".");
	}
	
	return retVal;
}

function checkBudget(){
	var retVal = false;
	var offeredPrice = $("#price").val();
	
	var availableBudget = myBudget-minRequiredBudget();

	if(offeredPrice<=availableBudget){
		retVal = true;
	}else{
		alert('Budget insufficiente puoi spendere al massimo '+availableBudget);
	}

	return retVal;
}

function minRequiredBudget(){
	return (MAX_GK - myGKCount)+(MAX_DF - myDFCount)+(MAX_MF - myMFCount)+(MAX_FW - myFWCount);
}

function translateRole(role){
	var retVal = '';
	switch (role) {
	  case ROLE_GK:
		  retVal='Portieri'
	    break;
	  case ROLE_DF:
		  retVal='Difensori'
	    break;
	  case ROLE_MF:
		  retVal='Centrocampisti'
	    break;
	  case ROLE_FW:
		  retVal='Attaccanti'
	    break;
	  default:
	    console.log("Spiacenti, non abbiamo " + expr + ".");
	}
	return retVal;

}

//ConnectionStatus
function setUserStatusChanged(username, content){
	var obj = JSON.parse(content);	
	setConnectionStatus("team_"+obj.userId,obj.status);
}

function setConnectionStatus(uId, status){
	if(status == 1){
		$('#'+uId).removeClass("panel-danger");	
		$('#'+uId).addClass("panel-info");	
	}else{
		$('#'+uId).removeClass("panel-info");	
		$('#'+uId).addClass("panel-danger");	
	}
}


//ADMIN
function openAdmin(){
	$( "#main-container" ).hide();
	$( "#admin-container" ).show();
}
function closeAdmin(){
	$( "#admin-container" ).hide();
	$( "#main-container" ).show();
}

function fillAdmPlayersTable(divId, role, plyList){

	let divRolePlayers = $('#'+divId);
	divRolePlayers.empty();
	
	divRolePlayers.append("<h3>"+role+"</h3>");
	
	divRolePlayers.append("<button id=\"import-"+role+"-btn\" class=\"btn btn-default\" onclick=\"importPlayers('"+role+"');\">importa "+translateRole(role)+"</button>");
	divRolePlayers.append("<button class=\"btn btn-default\" onclick=\"importPlayers();\">importa tutti</button>");

	divRolePlayers.append("<table class='table table-bordered'>");

	divRolePlayers.append("<thead class='thead-dark'>");
	divRolePlayers.append("<th scope='col'> id </th>");	
	divRolePlayers.append("<th scope='col'> Ruolo </th>");	
	divRolePlayers.append("<th scope='col'> Nome </th>");	
	divRolePlayers.append("<th scope='col'> Squadra </th>");	
	divRolePlayers.append("</thead>");

	divRolePlayers.append("<tbody>");
	$.each(plyList, function (key, entry) {
		divRolePlayers.append("<tr id='"+entry.id+"'>");

		divRolePlayers.append("<th scope='row'>"+entry.id+"</th>");
		divRolePlayers.append("<td>"+entry.role+"</td>");
		divRolePlayers.append("<td>"+entry.name+"</td>");
		divRolePlayers.append("<td>"+entry.realTeam+"</td>");

		divRolePlayers.append("</tr>");
	});
	
	divRolePlayers.append("</tbody>");
	divRolePlayers.append("</table>");

}

function importPlayers(role){
	console.log("role:::"+role);
	sendMsg('importPlayers',role);	
}

function importTeams(){
	sendMsg('importTeams');	
}

function showSeveResult(res){
	alert(res);
}

