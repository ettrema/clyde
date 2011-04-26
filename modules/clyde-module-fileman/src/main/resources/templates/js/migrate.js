
function showMigrator() {
    log("showMigrator");
    $('#migrator').dialog({
        modal: true,
        width: 800
    }
    )
    initStatus();
}

/**
 *
 */
function initStatus() {
    $.ajax({
        type: "GET",
        dataType: "json",
        url: "migrator",
        success: function(response){
            log('got status response', response);
            if( response && response.statuses ) {
                showStatus(response);
            } else {
                $("#headline").html("No migration in progress");
                $("#migrateFiles tbody").html("<tr><td colspan='5'>No files</td></tr>");
            }
        },
        error: function(response) {
            
        }
    });
}

/**
 * Query for file changes
 */
function queryFiles() {
    log("queryFiles");
    ajaxLoadingOn();
    $.ajax({
        type: "GET",
        data: "command=query",
        dataType: "json",
        url: "migrator",
        success: function(response){
            ajaxLoadingOff();
            log('got migrator response', response, "destHost:" ,response.destHost);
            $("#headline").html("Migration query results");
            showQuery(response);
        },
        error: function(response) {
            ajaxLoadingOff();
        }
    });
}

function showQuery(report) {
    log('showReport', report.statuses);
    var tbody = $("#migrateFiles tbody");
    tbody.html("");
    for( i=0; i<report.statuses.length; i++) {
        var status = report.statuses[i];
        var tr = $("<tr>");
        tr.append("<td><input type='checkbox' name='resourceId_" + i + "' value='" + status.localId + "'/></td>");
        tr.append("<td>" + status.localHref + "</td>");
        tr.append("<td>" + toDisplayDate(status.localModDate) + "</td>");
        tr.append("<td>" + toDisplayDate(status.remoteMod) + "</td>");
        tr.append("<td>" + status.comment + "</td>");
        tbody.append(tr);
    }
}


var timerStatus;

function showStatus(report) {
    log('showReport - ', report);

    if( report.finished ) {
        $("#headline").html("Migration complete");
    } else {
        $("#headline").html("Migration is running");
    }


    var tbody = $("#migrateFiles tbody");
    tbody.html("");
    for( i=0; i<report.statuses.length; i++) {
        var status = report.statuses[i];
        var tr = $("<tr>");
        tr.append("<td>" + status.uploaded + "</td>");
        tr.append("<td>" + status.localHref + "</td>");
        tr.append("<td>" + toDisplayDate(status.localModDate) + "</td>");
        tr.append("<td>" + toDisplayDate(status.remoteMod) + "</td>");
        tr.append("<td>" + status.comment + "</td>");
        tbody.append(tr);
    }

    if( !report.finished ) {
        timerStatus = window.setTimeout(function() {
            initStatus();
        },2000);
    }
}

function startMigration() {
    log('startMigration');
    clearTimeout(timerStatus);
    ajaxLoadingOn();
    $.ajax({
        type: "POST",
        data: $("#migrateForm").serialize(),
        dataType: "json",
        url: "migrator",
        success: function(response){
            ajaxLoadingOff();
            log('got migrator response', response);
            showStatus(response);
        },
        error: function(response) {
            ajaxLoadingOff();
        }
    });
}

function stopMigration() {
    log('stopMigration');
    ajaxLoadingOn();
    $.ajax({
        type: "POST",
        data: "command=stop",
        url: "migrator",
        success: function(response){
            ajaxLoadingOff();
            log('got migrator response', response);
        },
        error: function(response) {
            ajaxLoadingOff();
        }
    });
}

function toDisplayDate(dt) {
    if( dt ) {
        return (dt.day+1) + "/" + (dt.month+1) + "/" + (dt.year+1900) + " " + dt.hours + ":" + dt.minutes;
    } else {
        return "";
    }
}