
function resetValidation() {
    $(".validationError").remove();
    $(".pageMessage").hide();
    $(".error > *").unwrap();
    $(".errorField").removeClass("errorField");
}

function checkRequiredFields(container) {
    log('checkRequiredFields', container);
    var isOk = true;

    // Check mandatory
    $(".required", container).each( function(index, node) {
        var val = $(node).val();

        var title = $(node).attr("title");
        if( !val || (val == title) ) { // note that the watermark can make the value == title
            log('error field', node);
            showErrorField($(node));
            isOk = false;
        }

        return
    });
    if( isOk ) {
        isOk = checkValidEmailAddress(container);
        if( !checkDates(container)) {
            isOk = false;
        }
        if( !checkValidPasswords(container) ) {
            isOk = false;
        }
        log('will check length');
        if( !checkValueLength($("#firstName", container), 1, 15, "First name" ) ) {
            isOk = false;
        }
    } else {
        showMessage("Please enter all required values", container);
    }
    return isOk;
}

function checkRadio(radioName, container) {
    log('checkRadio', radioName, container);
    if( $("input:radio[name=" + radioName + "]:checked", container).length == 0 ) {
        var node = $("input:radio[name=" + radioName + "]", container)[0];
        node = $(node);
        node = $("label[for=" + node.attr("id") + "]");
        log('apply error to label', node);
        showValidation(node, "Please select a value for " + radioName, container );
        return false;
    } else {
        return true;
    }
}

// depends on common.js
function checkDates(container) {
    isOk = true;
    $("input", container).each( function(index, node) {
        var id = $(node).attr("id");
        if( id && id.contains("Date")) {
            var val = $(node).val();
            log('val');
            if( val && val.length > 0) {
                if( !isDate(val)) {
                    showValidation($(node), "Please enter a valid date", container );
                    isOk = false;
                }
            }
        }
    });
    return isOk;
}
/**
 *  If password is present, checks for validity
 */
function checkValidPasswords(container) {
    var target = $("#password",container);
    var p1 = target.val();
    if( p1 ) {
        if( p1.length < 6 ) {
            showValidation(target, "Your password must be at least 6 characters and must contain numbers and letters", container);
            return false;
        } else {
            return checkPasswordsMatch(container);
        }
    }
    return true;
}

function checkPasswordsMatch(container) {
    var p1 = $("#password",container).val();
    var p2 = $("#confirmPassword",container).val();
    if( p1 != p2 ) {
        showValidation("password", "Your password's don't match. Please try again",container);
        return false;
    }
    return true;
}

/**
 * We assume the field to validate has an id of "email"
 */
function checkValidEmailAddress(container) {
    var target = $("#email, input.email", container); // either with id of email, or with class email
    var emailAddress = target.val();
    if( emailAddress ) {
        var pattern = new RegExp(/^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i);
        if( !pattern.test(emailAddress) ) {
            showValidation(target, "Please check the format of your email address, it should read like ben@somewhere.com", container);
            return false;
        }
    }
    return true;
}

function checkValueLength(target, minLength, maxLength, lbl) {
    log('checkValueLength', target, minLength, maxLength, lbl);
    target = ensureObject(target);
    if( target.length == 0 ) {
        return true;
    }
    var value = target.val();
    log('length', value.length);
    if( minLength ) {
        if( value.length < minLength ) {
            showValidation(target, lbl + " must be at least " + minLength + " characters");
            return false;
        }
    }
    if( maxLength ) {
        log('check max length: ' + (value.length > maxLength));
        if( value.length > maxLength ) {
            showValidation(target, lbl + " must be no more then " + maxLength + " characters");
            return false;
        } else {
            log('check max length ok: ' + (value.length > maxLength));
        }

    }
    log('length ok');
    return true;
}

function checkExactLength(target, length) {
    target = ensureObject(target);
    var value = target.val();
    if( value.length != length ) {
        showValidation(target, "Must be at " + length + " characters");
        return false;
    }
    return true;
}



// Passes if one of the targets has a non-empty value
function checkOneOf(target1, target2, message) {
    target1 = ensureObject(target1);
    target2 = ensureObject(target2);
    if( target1.val() || target2.val() ) {
        return true;
    } else {
        showValidation(target1, message);
        return false
    }
}


// Passes if target's value is either empty or a number. Spaces etc are not allowed
function checkNumeric(target) {
    if( typeof target == "string") {
        target = $("#" + target);
    }
    var n = target.val();
    if( n ) {
        if( !isNumber(n)) {
            showValidation(target, "Please enter only numeric digits");
            return false;
        } else {
            return true;
        }
    } else {
        return true;
    }
}

function checkTrue(target, message, container) {
    var n = $("#" + target + ":checked").val();
    if( n ) {
        return true;
    } else {
        showValidation($("label[for='" + target + "']"), message, container);
        return false;
    }
}



/**
* target can be id or a jquery object
* text is the text to display
*/
function showValidation(target, text, container) {
    if( text ) {
        log("showValidation", target, text);
        showMessage(text, container);
        if( target ) {
            var t = ensureObject(target);
            showErrorField(t);
        }
    }
}

function showMessage(text, container) {
    $(".pageMessage",container).append("<p class='validationError'>" + text + "</p>");
    $(".pageMessage",container).show();
}

function showErrorField(target) {
    target.addClass("errorField");
}
