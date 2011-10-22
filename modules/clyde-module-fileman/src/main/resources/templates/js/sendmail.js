var sendMailHref; // url that contains the component
var sendMailName; // name of the component to trigger
var sendMailContainer; // the modal which contains the form and table

/**
 * href is the address of the resource containing the send mail component. Must
 * be suffixed with a slash if not empty!
 * 
 * name is the name of the component.
 * 
 * container is a jquery object referencing the div that holds the buttons and
 * status table
 * 
 * onRequestedCallback - optional - callback after start/stop request is completed
 * 
 * onFinishedCallback - optional - callback when a job is finished
 */
function initSendMail(href, name, container, onRequestedCallback, onFinishedCallback) {
	sendMailHref = href;
	sendMailName = name;
	sendMailContainer = container;
	
	$(".btnSendMailRefresh", container).click( function() {
		initSendMailStatus(href, name, onFinishedCallback);
	});
	$(".btnSendMailStart", container).click( function() {
		startSendMail(href, name, onRequestedCallback);
	});
	$(".btnSendMailStop", container).click( function() {
		stopSendMail(href, name, onRequestedCallback);
	});
	
}

function showSendMail() {
	log("showSendMail");
	sendMailContainer.dialog({
		modal: true,
		width: 600,
		minHeight: 400
	});
	initSendMailStatus(sendMailHref, sendMailName);
}

/**
 *
 */
function initSendMailStatus(href, name, onFinishedCallback) {
	log("initSendMailStatus", href, name);
	$.ajax({
		type: "GET",
		dataType: "json",
		data: "fields=clyde:status,clyde:running",
		url: href + name,
		success: function(response){
			log('got status response', response);
			if( response && response.length > 0  ) {
				showSendMailStatus(response[0].status);
				log("done showsendmailst");
				if( response[0].running ) {
					log("enable timer...");
					window.setTimeout(function() {
						initSendMailStatus(href, name);
					},2000);
				} else {
					log("on finished..");
					if( onFinishedCallback ) {
						onFinishedCallback();
					}
				}				
			} else {
				log("no status information");
				showSendMailStatus(null);
			}		
			log("button control");
			sendMailButtonControl(response);
			log("button control done");
		},
		error: function(response) {
			log("error", response);
		}
	});
}


function showSendMailStatus(status) {
	try {
		log('showSendMailStatus', status);
		var tbody = $("#sendMailRecipients tbody", sendMailContainer);
		tbody.html("");
		if( status != null && status.length > 0) {
			$("#sendMailRecipients", sendMailContainer).show();	
			for( i=0; i<status.length; i++) {
				var recipStatus = status[i];
				log("recipStatus", recipStatus, i);
				var tr = $("<tr>");
				tr.append("<td>" + recipStatus.email + "</td>");
				tr.append("<td>" + toRecipStatusText(recipStatus) + "</td>");
				tbody.append(tr);
			}
		} else {
			log("no status");
			//$("#headline", sendMailContainer).html("No email job in progress progress");
			$("#sendMailRecipients", sendMailContainer).hide();	
		}
	} catch(e) {
		log("Caught exception", e);
	}
}


function sendMailButtonControl(response) {
	log('sendMailButtonControl', response);
	if( response ) {
		log('has response');
		if( response[0].running ) {
			$(".btnSendMailStart", sendMailContainer).hide();
			$(".btnSendMailStop", sendMailContainer).show();
		} else {
			// job running
			$(".btnSendMailStop", sendMailContainer).hide();
			$(".btnSendMailStart", sendMailContainer).show();
		}
	} else {
		log("no report, so none running");
		$(".btnSendMailStart", sendMailContainer).hide();
		$(".btnSendMailStop", sendMailContainer).show();
	}
}

function startSendMail(href, name, onRequestedCallback) {
	log('startSendMail');
	clearTimeout(timerStatus);
	setSendMailStatus(href, name, true, onRequestedCallback);
}

function stopSendMail(href, name) {
	log('stopSendMail');
	setSendMailStatus(href, name, false);
}

/**
 * href - href of resource which contains the component
 * name - name of the component
 * running - true to start the job, false to cancel
 * onRequestedCallback(response, isSuccess) - called when request has been sent
 */
function setSendMailStatus(href, name, running, onRequestedCallback) {
	log('setStatus', name, running);
	ajaxLoadingOn();
	$.ajax({
		type: "POST",
		data: "clyde:running=" + running,
		url: href + name + "/_DAV/PROPPATCH",
		success: function(response){
			ajaxLoadingOff();
			log('got sendmail response', response);
			try {
				if( onRequestedCallback ) {
					onRequestedCallback(response, true);
				} else {
					initSendMailStatus(href, name);
				}				
			} catch(e) {
				log("exception", e);
			}
		},
		error: function(response) {
			ajaxLoadingOff();
			if( onRequestedCallback ) {
				onRequestedCallback(response, false);
			}		
		}
	});
}

function toRecipStatusText(recipStatus) {
	var s = recipStatus.status;
	if( recipStatus.retries > 0 ) {
		s += " (" + recipStatus.retries + " attempts)";
	}
	return s;
}