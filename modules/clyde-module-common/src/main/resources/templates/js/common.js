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
                var d=c.charCodeAt(x);
                var h=d.toString(16);
                o+='%'+(h.length<2?'0':'')+h.toUpperCase();

                //                if(c[x]==' ')o+='+';
                //                else{
                //                    var d=c.charCodeAt(x);
                //                    var h=d.toString(16);
                //                    o+='%'+(h.length<2?'0':'')+h.toUpperCase();
                //                }
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


/**
 * varargs function to output to console.log if console is available
 */
function log() {
    if( typeof(console) != "undefined" ) {
        console.log(arguments);
    }
}

function pad2(i) {
    var j = i - 0; // force to be a number
    if( j < 10 ) {
        return "0" + j;
    } else {
        return i;
    }
}


function toFileSize(num) {
    if( num > 1000000 ) {
      return Math.round(num/1000000) + 'Mb';
    } else {
      return Math.round(num/1000) + 'Kb';
    }
}

function toDisplayDateNoTime(dt) {
    return (dt.day+1) + "/" + (dt.month+1) + "/" + (dt.year+1900);
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


function now() {
    var dt = new Date();
    return {
        day: dt.getDay(),
        month: dt.getMonth(),
        year: dt.getYear()
    };
}



function reverseDateOrd(post1,post2){
    return dateOrd(post1,post2) * -1;
}

function dateOrd(post1,post2){
    var n = post1.date;
    var m = post2.date;

    if( n.year < m.year ) {
        return -1;
    } else if( n.year > m.year ) {
        return 1;
    }
    if( n.month < m.month ) {
        return -1;
    } else if( n.month > m.month ) {
        return 1;
    }
    if( n.day < m.day ) {
        return -1;
    } else if( n.day > m.day ) {
        return 1;
    }
    if( n.hours < m.hours ) {
        return -1;
    } else if( n.hours > m.hours ) {
        return 1;
    }
    if( n.minutes < m.minutes ) {
        return -1;
    } else if( n.minutes> m.minutes ) {
        return 1;
    }
    if( n.seconds < m.seconds ) {
        return -1;
    } else if( n.seconds> m.seconds ) {
        return 1;
    }
    return 0;
}

function isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}