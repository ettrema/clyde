function findType(file) {
    if( isDisplayableFile(file)) {
        if( file.iscollection ) {
            return "folder";
        } else {
            if( file.contentType ) {
                if( file.contentType.indexOf("image") >= 0) {
                    return "image";
                } else if( file.contentType.indexOf("flv") >= 0 || file.contentType.indexOf("flash") >= 0 ) {
                    return "flash";
                } else if( file.contentType.indexOf("video") >= 0 ) {
                    return "video";
                } else if( file.contentType.indexOf("audio") >= 0) {
                    return "audio";
                } else if( file.contentType.indexOf("directory") >= 0) {
                    return "folder";
                } else {
                    return "file";
                }
            } else {
                var ex = getExt(file.name);
                if( ex == "jpg" || ex == "jpeg" || ex == "png" || ex == "png") {
                    return "image";
                } else if( ex == "flv") {
                    return "flv";
                } else if( ex == "mp3") {
                    return "audio";
                }
                return "file";
            }
        }
    } else {
        return "hidden";
    }
}


function findIcon(file) {
    if( isDisplayableFile(file)) {
        if( file.iscollection ) {
            return "folder.png";
        } else {
            return findIconByExt(file.href);
        }
    } else {
        return "hidden";
    }
}

function findIconByExt(filePath) {
    var ext = getExt(filePath);
    return ext + "_48x48-32.png";
}

function getExt(fileName) {
    var ext = /^.+\.([^.]+)$/.exec(fileName);
    ext = (ext == null) ? "" : ext[1];
    return ext.toLowerCase();
}

function getFileName(path) {
    var arr = path.split('/');
    var name = arr[arr.length-1];
    if( name.length == 0 ) { // might be empty if trailing slash
        return arr[arr.length-2];
    } else {
        return name;
    }
}

function isDisplayableFile(file) {
    if( file.href ) {
        return isDisplayableFileHref(file.href);
    } else {
        if( file.targetHref ) { // for recent files
            return isDisplayableFileHref(file.targetHref);
        } else {
            return false;
        }
    }
}

// These file names either have particular meaning to shmego, or they are common
// garbage files.
function isDisplayableFileHref(href) {
    if( href == 'Thumbs.db' ) return false;
    if( endsWith(href, '/regs/') ) return false;
    if( endsWith(href, '.MOI') ) return false;
    if( endsWith(href, '.THM') ) return false;
    var name = getFileName(href);
    if( startsWith(name, "_sys_")) return false;
    return true;
}

