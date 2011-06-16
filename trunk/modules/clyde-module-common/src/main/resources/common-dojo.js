

function showAdd() {
    dijit.byId('addPageDialog').show();
}

function nameIsUnique(folderHref, name) {
    var isUnique = true;
    dojo.xhrGet ({
        url: folderHref + '_DAV/PROPFIND',
        sync: true,
        handleAs: 'json',
        load: function (data) {
            for( i=0; i<data.length; i++ ) {
                if( data[i].name == name ) {
                    isUnique = false;
                }
            }
        },
        error: function (data) {
            alert('error getting file list: ' + data);
        }
    });
    return isUnique;
}


function validate(folderHref) {
    var name = dijit.byId('newName').value;
    var b = name != null && name.length>0;
    if( !b ) {
        alert('please enter a name');
        return false;
    }
    if(!nameIsUnique(folderHref, name)) {
        alert('there is already a file with that name. Please choose another');
        return false;
    }
    var form = dojo.byId('addPageForm');
    var templateSelect = form.elements['templateSelect'];
    if( templateSelect.length ) {
        b = false;
        for( i=0; i<templateSelect.length; i++ ) {
            if( templateSelect[i].checked ) b = true;
        }
    } else {
        b = templateSelect.checked;
    }
    if( !b ) {
        alert('please select a template');
    }
    return b;    
}

function loadProps(href, fields, depth, callback, container) {
    dojo.xhrGet ({
        url: href + '_DAV/PROPFIND?fields=title,href&depth=' + depth,
        sync: false,
        handleAs: 'json',
        load: function (data) {
            data = data.children;
            while(container.hasChildNodes()){
                container.removeChild(container.lastChild);
            }
            callback(data,container);
        },
        error: function (data) {
            alert('error getting file list: ' + data);
        }
    });
}

function showReplies(data, container) {

    if(!data) return;
    var ul = document.createElement("ul");
    container.appendChild(ul);
    for( i=0; i<data.length; i++ ) {
        var li = document.createElement("li");
        var a = document.createElement("a");
        a.href = data[i].href;
        a.innerHTML = data[i].title;
        li.appendChild(a);
        ul.appendChild(li);

        if(data[i].children) {
            console.log('showReplies has children');
            showReplies(data[i].children, li);
        }
    }
}

function reply(folderHref, callback) {
    dojo.xhrPost ({
        url: folderHref + '_autoname.new',
        sync: true,
        handleAs: 'json',
        load: callback,
        error: function (data) {
            alert('error getting file list: ' + data);
        }
    });

}

