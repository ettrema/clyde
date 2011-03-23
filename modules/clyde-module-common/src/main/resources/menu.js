
function _addPage(folder, template) {
    var name = prompt("Enter the name for the new file","");
    if( name.length > 0 ) {		
	    var url = folder + name + ".new?templateSelect=" + template;
	    window.location.href = url;
    }
    return false;
}

function addFolder(template) {
    var name = prompt("Enter the name for the new folder","new folder");
    doCreateCollection(name);
    return false;
}

function onMessage(result) {
    window.location.href = result;
}

function doCreateCollection(name) {
    Console.createCollection('$targetPage.parent.url',name,onMessage);
}



// Copyright 2006-2007 javascript-array.com

var timeout	= 1500;
var closetimer	= 0;
var ddmenuitem	= 0;

// open hidden layer
function mopen(id)
{	
    // cancel close timer
    mcancelclosetime();

    // close old layer
    if(ddmenuitem) ddmenuitem.style.visibility = 'hidden';

    // get new layer and show it
    ddmenuitem = document.getElementById(id);
    ddmenuitem.style.visibility = 'visible';

}
// close showed layer
function mclose()
{
    if(ddmenuitem) ddmenuitem.style.visibility = 'hidden';
}

// go close timer
function mclosetime()
{
    closetimer = window.setTimeout(mclose, timeout);
}

// cancel close timer
function mcancelclosetime()
{
    if(closetimer)
    {
        window.clearTimeout(closetimer);
        closetimer = null;
    }
}

// close layer when click-out
document.onclick = mclose; 
