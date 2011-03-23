
var currentFolderUrl;
var currentThumbId;
var thumbs;

function initTree() {
    initUser();

    $("#tree").bind("loaded.jstree", function (e, data) {
        checkInitialLoad();
    });


    $("#tree").jstree({
        // the list of plugins to include
        "plugins" : [ "themes", "json_data", "search" ],
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
                            if( key > 0 && isDisplayableFileHref(value.href) ) {
                                value.state = "closed"; // set the initial state
                                value.data = value.name; // copy name to required property
                                value.attr = {
                                    id : createNodeId(value.href) // set the id attribute so we know its href
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
    })
}


// Bind the click event to opening the clicked folder
$("#tree li").live("click", function(e) {
    clickedFolder($(this));
    return false;
})

$("#tree ins").live("click", function(e) {
    //console.log('click ins', $(this).parent().attr("id"));
    return false;
})



//  Get the url for the folder associated with the given node, and
// load its contents, and set the current folder to its url
function clickedFolder(n) {
    var nodeId = "#" + $(n).attr("id");
    var url = toUrl(n);
    $("#tree").jstree("open_node", nodeId);
    clickedFolderUrl(url);
}

function clickedFolderUrl(url) {
    loadFolder(url);
}
function selectFolder(n) {
    var url = toUrl(n);
    loadFolder(url);
    return url;
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
    if( jsonDev ) {
        url = ".." + basePath() + path + "DAV/PROPFIND.txt?fields=name,clyde:streamingVideoHref,getcontenttype>contentType,clyde:thumbHref,href,iscollection,getlastmodified>modifiedDate,getcontentlength>contentLength&depth=1";
    } else {
        url = ".." + basePath() + path + "_DAV/PROPFIND?fields=name,clyde:streamingVideoHref,getcontenttype>contentType,clyde:thumbHref,href,iscollection,getlastmodified>modifiedDate,getcontentlength>contentLength&depth=1";
    }
    return url;
}


function basePath() {
    // Note this is used to strip the path from raw hrefs
    return accountRootPath() + "files/";
}

function accountRootPath() {
    return accountRootPathNoSlash() + "/";
}

function accountRootPathNoSlash() {
    return "/sites/" + accountName;
}

// Expand the tree node associated with the folder url, and load its contents
// into the thumbs area
function openAndLoadFolder(folderUrl) {
    var nodeId = "#" + toNodeId(folderUrl);
    $("#tree").jstree("open_node", nodeId);
    loadFolder(folderUrl);
}

// Called when a folder is selected. Loads the thumbs for the folder and shows
// the first preview
function loadFolder(folderUrl) {
    if( currentFolderUrl == folderUrl ) {
        return;
    }

    currentFolderUrl = folderUrl;

    var s = window.location.href;
    if( s.indexOf("#") >= 0 ) {
        s = s.split("#")[0];
    }
    window.location.href = s + "#" + currentFolderUrl;
    document.title = "View folder: " + currentFolderUrl;


    highLightFolder(folderUrl);
    var url = toPropFindUrl(folderUrl);

    var thumbsDiv = $("#thumbs");
    thumbsDiv.html(" <table width='100%' height='100%' border='0' cellspacing='0' cellpadding='0'><tr><td valign='middle' height='100%' align='center'><img class='ajaxLoading' src='../templates/images/framework/ajax/loading-icon.gif' /></td></tr></table>");

    // load thumbs
    $.getJSON(url, function(response) {
        thumbsDiv.html("");
        var allThumbs = response;
        thumbs = new Array(); // reset the array of displayable thumbs
        currentThumbId = 0;
        for( i=1; i<allThumbs.length; i++) { // i=1 because want to skip first which is current folder
            var file = allThumbs[i];
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
            }
        }
        if( thumbs.length == 0 ) {
            showNoThumbs();
        } else {
            loadCurrentThumbs();
            $("img").lazyload({
                container: $("#thumbs")
            });
            selectFirstThumb();
        }
    });
}

function loadCurrentThumbs() {
    var thumbsDiv = $("#thumbs");
    for( i=0; i<thumbs.length; i++) {
        var file = thumbs[i];
        buildThumbFrame(file, i, thumbsDiv);
    }
}

function highLightFolder(url) {
    $(".jstree-clicked").removeClass("jstree-clicked");
    var nodeId = toNodeId(url);
    $("#" + nodeId + " > a").addClass("jstree-clicked");
}

function showNoThumbs(thumbsDiv) {
    $("#facebookShare").hide();
//    showPreviewHref("https://www.thebackupbusiness.com/templates/getting_started_panel.gif");
//    var html = "<table width='100%' height='100%' border='0' cellspacing='0' cellpadding='0'><tr><td valign='middle' height='100%' align='center'><img src='../images/ml/fileManager/noFilesWarning.png' alt='No Files' /></td></tr></table>";
//    thumbsDiv.html(html);
}

function selectFirstThumb() {
    $("#facebookShare").show();
    for( i=0; i<thumbs.length; i++) {
        if( selectThumb(i)) {
            return;
        }
    }
    $("#preview").html("");
}

function clickThumb(i) {
    if( i < 0 || i >= thumbs.length) {
        $("#preview").html("");
        return false;
    } else {
        var file = thumbs[i];
        var type = file.getType();
        if( type == 'folder' ) {
            var path = file.href.replace(basePath(),"");
            openAndLoadFolder(path);
            return false;
        } else {
            selectThumb(i);
            return false;
        }
    }
}

function selectThumb(i) {
    if( i >= thumbs.length) {
        i = 0;
    } else if( i < 0 ) {
        i = thumbs.length-1;
    }
    var file = thumbs[i];
    currentThumbId = i;
    var type = file.getType();
    if( type == 'image' ) {
        var html = "<img src='" + file.getPreview() + "' alt='" + file.name +  "' class='image' />";
        $("#preview").html(html);
        return true;
    } else if( type == 'video' ) {
        if( file.streamingVideoHref ) {
            var flashFile = basePath() + currentFolderUrl + "_sys_flashs/" + file.name + ".flv";
            showVideo(file.name,  flashFile);
            return true;
        } else {
            return false;
        }
    } else if( type == 'flash' ) {
        showVideo(file.name, file.href)
        return true;
    } else {
        var fileUrl = basePath() + currentFolderUrl + file.name;
        var fileIconHtml = "<img src='../images/ml/fileManager/icons/" + file.getIcon() + "' />";
        var fileHtml = "<a target='new' href='" + fileUrl + "'>" + fileIconHtml + "</a>";
        fileHtml = fileHtml + "<a target='new' href='" + fileUrl + "'>" + file.name + "</a>";
        fileHtml = fileHtml + "<p>Size: " + toFileSize(file.contentLength) +"</p>";
        $("#preview").html("<div class='file'>" + fileHtml + "</div>");
        return false;
    }
}

// Display a video in the preview area
function showVideo(title, streamingVideoHref) {
    $("#preview").html("<div id='flowplayerholder'></div>");
    flowplayer("flowplayerholder", {
        src: "../templates/flowplayer-3.2.2.swf"
    }, {
        clip: {
            url: streamingVideoHref,
            autoPlay: true
        },
        play: {
            label: title,
            replayLabel: "click to play again"
        }
    } );
}


function buildThumbFrame(file, i, thumbsDiv) {
    var type = file.getType();

    var thumbFrame;
    var thumbHref;
    if( type == 'image' ) {
        thumbHref = ".." + basePath() + currentFolderUrl + "_sys_thumbs/" + file.name;
        thumbFrame = "<div class='thumbPhoto' onclick='clickThumb(" + i + ")'><img src='" + thumbHref + "' alt='" + file.name + "' /></div>";
    } else if( type == 'flash' ) {
        thumbHref = ".." + basePath() + currentFolderUrl + "_sys_thumbs/" + file.name + ".jpg";
        thumbFrame = "<div class='thumbPhoto' onclick='clickThumb(" + i + ")'><img src='" + thumbHref + "' alt='" + file.name + "' /></div>";
    } else if( type == 'video' ) {
        thumbHref = ".." + basePath() + currentFolderUrl + "_sys_thumbs/" + file.name + ".jpg";
        thumbFrame = "<div class='thumbPhoto' onclick='clickThumb(" + i + ")'><img src='" + thumbHref + "' alt='" + file.name + "' /></div>";
    } else {
        // audio, folder, or an office document, use the type variable as an icon
        thumbFrame = "<div class='thumbFile' onclick='clickThumb(" + i + ")'><div class='icon'><img src='../images/ml/fileManager/icons/" + file.getIcon() + "' /></div><span class='text'>" + file.name + "</span></div>";
    }
    if( thumbFrame ) {
        thumbsDiv.append(thumbFrame);
    }

}


// see clyde-modules-common/common.js
function endsWith(str, suffix) {
    return str.match(suffix+"$")==suffix;
}
function startsWith(str, prefix) {
    return str.indexOf(prefix) === 0;
}




$.extend({
    URLEncode:function(c){
        var o='';
        var x=0;
        c=c.toString();
        var r=/(^[a-zA-Z0-9_.]*)/;
        while(x<c.length){
            var m=r.exec(c.substr(x));
            if(m!=null && m.length>1 && m[1]!=''){
                o+=m[1];
                x+=m[1].length;
            }else{
                if(c[x]==' ')o+='+';
                else{
                    var d=c.charCodeAt(x);
                    var h=d.toString(16);
                    o+='%'+(h.length<2?'0':'')+h.toUpperCase();
                }
                x++;
            }
        }
        return o;
    },
    URLDecode:function(s){
        var o=s;
        var binVal,t;
        var r=/(%[^%]{2})/;
        while((m=r.exec(o))!=null && m.length>1 && m[1]!=''){
            b=parseInt(m[1].substr(1),16);
            t=String.fromCharCode(b);
            o=o.replace(m[1],t);
        }
        return o;
    }
});


var isFileView = true;

function showSlideshow() {
    if( isFileView ) {
        var flashvars = {
            feed: "../sites/" + accountName + "/files/" + currentFolderUrl +  "media.xml"
        };
        var params = {
            numRows : "2",
            allowFullScreen: "true",
            allowscriptaccess: "always"
        };
        swfobject.embedSWF("http://apps.cooliris.com/embed/cooliris.swf", "preview", "95%", "330", "9.0.0", "",flashvars, params);
        isFileView = false;
    } else {
        $("#preview").replaceWith("<div id='preview' class='preview' align='center'> </div>");
        clickThumb(0);
        isFileView = true;
    }

}

function fbLoad(file) {
    var loadHref = ".." + basePath() + currentFolderUrl + "_sys_regs/" + file.name;
    var url = loadHref + "/_sys_facebook";

    $("#whizzyWhirly").show();
    $.ajax({
        type: 'POST',
        url: url,
        data: {
            caption: "Loaded via http://shmego.com - Backup & Share your photos."
        },
        success: function() {
            $("#whizzyWhirly").hide();
        },
        error:  function() {
            alert('We were unable to share your photo with Facebook.  Please try again.');
            $("#whizzyWhirly").hide();
        },
        dataType: "text"
    });
}

function showShareForm() {
    $("inviteForm").show(); // make sure not hidden from previous
    $("#folderShareModal").modal({
        minHeight:400,
        minWidth: 600
    });
    var folder = $.URLDecode(toDisplayFolder(currentFolderUrl));
    $(".descriptionHighlight").html(folder);
}

function inviteSend() {
    var inviteMessage = $("#inviteMessage").val();

    var invites = $("#inviteeEmails").val();

    if( invites == "" ) {
        alert('please enter at least one email address');
        return;
    }

    $("#inviteForm").hide();
    $("#inviteWhizzyWhirly").show();

    var url = basePath() + currentFolderUrl + 'invite';
    
    $.ajax({
        type: 'POST',
        url: url,
        data: {
            invite: "invite",
            inviteeEmails: invites,
            inviteMessage: inviteMessage
        },
        dataType: "text",
        success: onDoneInvite,
        error: onErrorInvite
    });
}

function inviteCancel() {
    closeModal();
}

function onDoneInvite(resp) {
    $("#inviteWhizzyWhirly").hide();
    if( resp == "ok" ) {
        $("#inviteDoneOk").show();
    } else {
        $("#inviteDoneErr").show();
        $("#inviteForm").show();
    }
}

function onErrorInvite(resp) {
    $("#inviteWhizzyWhirly").hide();
    $("#inviteDoneErr").show();
    $("#inviteForm").show();
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

function toFileSize(num) {
    if( num > 1000000 ) {
      return Math.round(num/1000000) + 'Mb';
    } else {
      return Math.round(num/1000) + 'Kb';
    }
}