
var currentFolderUrl;
var currentFolderTemplate;
var currentThumbId;
var thumbs;

 
 
function initTree() {
    initUser();

    $("#tree").bind("loaded.jstree", function (e, data) {
        checkInitialLoad();
    });


    $("#tree").jstree({
        // the list of plugins to include
        "plugins" : [ "themes", "json_data", "search", "contextmenu" ],
        // Plugin configuration

        // I usually configure the plugin that handles the data first - in this case JSON as it is most common
        "json_data" : {
            // I chose an ajax enabled tree - again - as this is most common, and maybe a bit more complex
            // All the options are the same as jQuery's except for `data` which CAN (not should) be a function
            "ajax" : {
                // the URL to fetch the data
                "url" : function(n) {
                    var url = toUrl(n);
                    //var url = selectFolder(n);
                    url = toPropFindUrl(url);
                    return url;
                },
                dataType: "json",

                // this function is executed in the instance's scope (this refers to the tree instance)
                // the parameter is the node being loaded (may be -1, 0, or undefined when loading the root nodes)
                "data" : function (n) {
                    // the result is fed to the AJAX request `data` option
                    return "";
                },
                "error" : function(data) {
                    
                },
                "success" : function (data) {
                    var newData=new Array();
                    // Add some properties, and drop first result
                    $.each(data, function(key, value) {
                        if( value.iscollection ) {
                            log('tree item', value.href, value.templateName);
                            if( key > 0 && isDisplayableFileHref(value.href) ) {
                                value.state = "closed"; // set the initial state
                                value.data = value.name; // copy name to required property
                                value.metadata = value;
                                value.attr = {
                                    id : createNodeId(value.href), // set the id attribute so we know its href
                                    "class" : value.templateName
                                };
                                newData[newData.length] = value;
                            }
                        }
                    });
                    // checkInitialLoad();
                    return newData;
                }
            }
        },
        "themes": {
            "theme": "apple"			
        },
        "ui" : {
            "select_limit" : 1,
            "select_multiple_modifier" : "alt",
            "selected_parent_close" : "select_parent"
        },
        contextmenu : {
            items : function(node){
                log("items", node);
                return getContextMenuItems(node.data('jstree'));
            }
        },

        // Configuring the search plugin
        "search" : {
            // As this has been a common question - async search
            // Same as above - the `ajax` config option is actually jQuery's object (only `data` can be a function)
            "ajax" : {
                "url" : "/static/v.1.0rc2/_demo/server.php",
                // You get the search string as a parameter
                "data" : function (str) {
                    return {
                        "operation" : "search",
                        "search_str" : str
                    };
                }
            }
        }
    });
}


// Bind the click event to opening the clicked folder
$("#tree li").live("click", function(e) {
    clickedFolder($(this));
    return false;
})

$("#tree ins").live("click", function(e) {
    return false;
})



//  Get the url for the folder associated with the given node, and
// load its contents, and set the current folder to its url
function clickedFolder(n) {
    var template = n.data('jstree').templateName;
    log('clickedFolder', template);
    var nodeId = "#" + $(n).attr("id");
    var url = toUrl(n);
    $("#tree").jstree("open_node", nodeId);
    loadFolder(url, template);
}



// map of url's keyed by node id
var nodeMap = new Array();

// map of node id's keyed by href (relative to base path eg href=Documents/Folder1
var hrefMap = new Array();
var nodeMapNextId = 0;

// Just get the url for the given node (a LI element)
function toUrl(n) {
    // n should be an LI
    var url = n.attr ? nodeMap[n.attr("id")] : "";
    return url;
}

function toNodeId(url) {
    var nodeId = hrefMap[url];
    return nodeId;
}

function createNodeId(href) {
    var newId = "node_" + nodeMapNextId;
    nodeMapNextId = nodeMapNextId + 1;
    var newHref = href.replace(basePath(), "");
    nodeMap[newId] = newHref;
    hrefMap[newHref] = newId;
    return newId;
}

// path is a partial path, like Documents/folder1
function toPropFindUrl(path) {
    var url;
    if( path == "") path = "/";
    url = basePath() + path + "_DAV/PROPFIND?fields=name,clyde:allowedTemplateNames,clyde:streamingVideoHref,getcontenttype>contentType,clyde:thumbHref,href,iscollection,getlastmodified>modifiedDate,getcontentlength>contentLength,clyde:templateName&depth=1";
    log("toPropFindUrl", url);
    return url;
}


function basePath() {
    // Note this is used to strip the path from raw hrefs
    return "";
}

function accountRootPath() {
    return "/";
}

function accountRootPathNoSlash() {
    return "";
//return "/sites/" + accountName;
}

function refreshCurrentFolder() {
    loadFolder(currentFolderUrl, currentFolderTemplate);

}

// Called when a folder is selected. Loads the thumbs for the folder and shows
// the first previewv
function loadFolder(folderUrl, template) {
    log('loadFolder', folderUrl, "currentFolderUrl", currentFolderUrl, "template", template);
    //    if( currentFolderUrl == folderUrl ) {
    //        return;
    //    }

    currentFolderUrl = folderUrl;
    currentFolderTemplate = template;

    initFolderUpload();

    var s = window.location.href;
    if( s.indexOf("#") >= 0 ) {
        s = s.split("#")[0];
    }
    window.location.href = s + "#" + currentFolderUrl;
    document.title = "View folder: " + currentFolderUrl;


    highLightFolder(folderUrl);

    selectFolder(folderUrl, template);
}

function highLightFolder(url) {
    $(".jstree-clicked").removeClass("jstree-clicked");
    var nodeId = toNodeId(url);
    $("#" + nodeId + " > a").addClass("jstree-clicked");
}



function selectFolder(folderHref, template) {
    log('selectFolder: ' + folderHref, "template", template);
    if( template == null || template == "" || template == "folder") {
        // if no template, just a plain folder so load files
        loadFilesTable(folderHref);
    } else {
        // if has template, then assume there is a management page for it
        loadFolderIntoIframe(folderHref);
    }
}

function loadFolderIntoIframe(folderHref) {
    var frame = "<iframe id='previewContent' src='" + folderHref + "index.html'></iframe>";
    $("#preview").html(frame);
}
function loadIframe(href) {
    var frame = "<iframe id='previewContent' src='" + href + "'></iframe>";
    $("#preview").html(frame);
}

function loadFilesTable(folderUrl) {
    var thumbsDiv = $("#thumbs");
    thumbsDiv.html(" <table width='100%' height='100%' border='0' cellspacing='0' cellpadding='0'><tr><td valign='middle' height='100%' align='center'><img class='ajaxLoading' src='../templates/images/loading-icon.gif' /></td></tr></table>");

    // load thumbs
    var url = toPropFindUrl(folderUrl);
    log('loadFilesTable', url);
    $.getJSON(url, function(response) {
        log('got json response');
        thumbsDiv.html("");
        var allThumbs = response;
        thumbs = new Array(); // reset the array of displayable thumbs
        currentThumbId = 0;
        for( i=1; i<allThumbs.length; i++) { // i=1 because want to skip first which is current folder
            var file = allThumbs[i];
            log('file', file.href);
            file.getType = function() {
                return findType(this);
            };
            file.getIcon = function() {
                return findIcon(this);
            };
            file.getPreview = function() {
                return ".." + basePath() + currentFolderUrl + "_sys_regs/" + this.name;
            };
            if( file.getType() != "hidden") {
                thumbs[thumbs.length] = file;
            } else {
                log('is hidden');
            }
        }
        if( thumbs.length == 0 ) {
            log('no thumbs');
            showNoThumbs();
        } else {
            log('load files');
            loadCurrentFiles();
        }
    });
}

function loadCurrentFiles() {
    var thumbsDiv = $("#preview");
    var table = $("<table class='filesList'><thead><tr><th>Name</th><th>Size</th><th>Modified</th></tr></thead><tbody></tbody></table>");
    thumbsDiv.html("").append(table);
    var tbody = $("tbody", table);
    for( i=0; i<thumbs.length; i++) {
        var file = thumbs[i];
        buildThumbRow(file, i, tbody);
    }
    var h = $("#preview").height() - 100;
    $(table).dataTable({
        "bJQueryUI": true,
        "sScrollY": h + "px",
        "bPaginate": false
    });
}
function showNoThumbs(thumbsDiv) {
    $("#facebookShare").hide();
    $("#preview").html("<h2>There are no files in this folder</h2>");
}
function buildThumbRow(file, i, thumbsTable) {
    var row = $("<tr>");
    thumbsTable.append(row);
    row.append("<td>" + file.name + "</td>");
    row.append("<td>" + file.getType() + "</td>");
    row.append("<td>" + toFileSize(file.contentLength) + "</td>");
    row.click(function() {
        log("row click", file.href);
        selectTableItem(file);

    });
}
function selectTableItem(file) {
    var t = file.getType();
    if( t == "folder") {
        var nodeId = "#" + toNodeId(file.href);
        $("#tree").jstree("open_node", nodeId);
        loadFolder(file.href, file.templateName);
    } else if( t == "image") {
        loadIframe(file.href);
    } else if( t == "flv") {
        loadFlv(file.href);
    } else {
        loadIframe(file.href);
    }
}

/**
 *  Called to invoke the edit page for a
 */
function doEdit() {
    log("doEdit", currentFolderUrl);
    var frame = $(".preview iframe");
    if( frame.length == 0 ) {
        loadIframe(currentFolderUrl + ".edit");
    } else {
        var src = frame.attr("src");
        if( endsWith(src, ".edit")) {
            alert("You are already on the edit page for this resource");
        } else {
            loadIframe(src + ".edit");
        }
    }
}



function closeModal() {
    $.modal.close();
}

// Turns a href like /files/Pictures/abc/ into Pictures/abc
function toDisplayFolder(href) {
    var s = href;
    s = s.substring(0,s.length-1); // lose trailing slash
    return s;
}

function confirmDelete(href, name, callback) {
    if( confirm("Are you sure you want to delete " + name + "?")) {
        deleteFile(href, callback);
    }
}

function deleteFile(href, callback) {
    ajaxLoadingOn();
    $.ajax({
        type: 'DELETE',
        url: href,
        dataType: "json",
        success: function(resp) {
            log('deleted', href);
            ajaxLoadingOff();
            callback();
        },
        error: function(resp) {
            log("failed", resp);
            ajaxLoadingOff();
            alert("Sorry, an error occured deleting a module status. Please check your internet connection");
        }
    });
}

function ajaxLoadingOn(sel) {
    log('ajax ON', sel);
    $("#ajaxLoading").dialog({
        modal: true,
        width: "400px",
        resizable: false,
        dialogClass: "noTitle"
    });
}

function ajaxLoadingOff(sel) {
    log('ajax OFF', sel);
    $("#ajaxLoading").dialog('close');
}
function initFolderUpload() {
    var button = $('#filemanUpload');
    log('initUploads', button);

    new AjaxUpload(button,{
        action: currentFolderUrl + '_DAV/PUT?_autoname=true',
        name: 'upload',
        autoSubmit: true,
        responseType: 'json',
        onSubmit : function(file, ext){
            ajaxLoadingOn();
            this.disable();
        },
        onComplete: function(file, response){
            ajaxLoadingOff();
            this.enable();
            refreshCurrentFolder();
        }
    });
}