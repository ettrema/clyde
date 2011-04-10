
var userUrl = null;
var accountName;
var jsonDev;

/**
 * returns true if there is a valid user
 */
function initUser() {
    if( userUrl ) {
        return; // already done
    }
    initUserCookie();
    if( isEmpty(userUrl) || isEmpty(accountName) ) {
        // no cookie, so authentication hasnt been performed.
        return false;
    } else {
        $("#userUrl").html(userUrl);
        $("#accountName").html(accountName);
        return true;
    }
}

function initUserCookie() {
    userUrl = $.cookie('_clydeUser');
    accountName = $.cookie('accountName');
    jsonDev = $.cookie('jsonDev');
}

function isEmpty(s) {
    return s == null || s.length == 0;
}

function doLogout() {
    $.ajax({
        type: 'POST',
        url: "/index.html",
        data: "_clydelogout=true",
        dataType: "text",
        success: function() {
            window.location = "/index.html";
        },
        error: function() {
            alert('There was a problem logging you out');
        }
    });    
}

function doLogin(form) {
    $.ajax({
        type: 'POST',
        url: window.location.href + ".ajax",
        data: {
            _loginUserName: $("input[type=text]", form).val(),
            _loginPassword: $("input[type=password]", form).val()
        },
        dataType: "text",
        success: function() {
            alert('logged in ok');
        },
        error: function() {
            alert('There was a problem logging you in');
        }
    });
}

function initUsage() {
    initUser();

    var url;
    if( jsonDev ) {
        url = "/sites/" + accountName + "/files/DAV/PROPFIND.txt?fields=quota-available-bytes>available,quota-used-bytes>used&depth=0";
    } else {
        url = "/sites/" + accountName + "/files/_DAV/PROPFIND?fields=quota-available-bytes>available,quota-used-bytes>used&depth=0";
    }

    $.getJSON(url, function(response) {
        var root = response[0];
        var total = root.available + root.used;
        if( total > 0 ) {
            var perc = Math.round(root.used * 100 / total);
            var totalGigs = total / 1000000000
            $(".labelTypeData").html(totalGigs + "GB");
            $(".labelDataAmount").html(perc + "%");
        } else {
            $(".labelDataAmount").html("Unknown");
        }
    });
}

function getParameter( name ) {
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}