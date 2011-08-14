$(document).ready(function(){
	initUploads();
});

function initUploads() {
	var button = $('#doUpload');
	
	new AjaxUpload(button,{
		action: '_DAV/PUT',
		name: 'picd',
		autoSubmit: true,
		responseType: 'json',
		onSubmit : function(file, ext){
			if( !userUrl ) {
				alert('Please login to upload photos');
				return;
			}
			$("span", button).text('Upload...');
			this.disable();
		},
		onComplete: function(file, response){
			$("span", button).text('Upload again');
			this.enable();
			for( i=0; i<response.length; i++ ) {
				var file = response[i];
			//      	     $("#cvHidden").attr("value",file.href);
			//      	     $("#cvLink").html(file.originalName + "(" + file.length + ")");
			//      	     $("#cvLink").attr("href",file.href);
			}
		}
          
	});
}

function showUploadModal() {
	$("#uploads").dialog({
		modal: true,
		width: 500,
		title: "Upload"
	});
}

(function($){
	$.fn.dragUploadable = function(postURL, fieldName, options) {

		var defaults = {
			dragenterClass: "",
			dragleaveClass: "",
			dropListing: "#dropListing",
			loaderIndicator: "#progress"
		};
		var options = $.extend(defaults, options);

		return this.each(function() {
			obj = $(this);
			obj.bind("dragenter", function(event){
				obj.removeClass(options.dragleaveClass);
				obj.addClass(options.dragenterClass);
				event.stopPropagation();
				event.preventDefault();
			}, false);
			obj.bind("dragover", function(event){
				event.stopPropagation();
				event.preventDefault();
			}, false);
			obj.bind("dragleave", function(event){
				obj.removeClass(options.dragenterClass);
				obj.addClass(options.dragleaveClass);
				event.stopPropagation();
				event.preventDefault();
			}, false);
			obj.bind("drop", function(event){
				var data = event.originalEvent.dataTransfer;
				event.stopPropagation();
				event.preventDefault();
				addToList(event.originalEvent.dataTransfer, options.dropListing, options.loaderIndicator);
				upload(postURL, fieldName, data);
			}, false);
		});
	};
})(jQuery);

function dropSetup() {
	var dropContainer = document.getElementById("output");

	dropContainer.addEventListener("dragenter", function(event){
		dropContainer.innerHTML = 'DROP';
		event.stopPropagation();
		event.preventDefault();
	}, false);
	dropContainer.addEventListener("dragover", function(event){
		event.stopPropagation();
		event.preventDefault();
	}, false);
	dropContainer.addEventListener("drop", upload, false);
};

function upload(postURL, fieldName, dataTransfer, loaderIndicator) {
	log("upload", postURL, fieldName, dataTransfer.files.length);
	$.each(dataTransfer.files, function ( i, file ) {
		log("send file", file.fileName);
		var xhr    = new XMLHttpRequest();
		var fileUpload = xhr.upload;
		fileUpload.addEventListener("progress", function(event) {
			if (event.lengthComputable) {
				var percentage = Math.round((event.loaded * 100) / event.total);				
				if (percentage < 100 && loaderIndicator) {
					log("percent complete", percentage);
					$(loaderIndicator).css("width", percentage + "%");
					$(loaderIndicator).text(percentage + "%");
				}
			}
		}, false);
				
		fileUpload.addEventListener("load", function(event) {
			$(loaderIndicator).text("Finished");
		}, false);
				
		fileUpload.addEventListener("error", function(event) {
			$(loaderIndicator).text("Error");
		}
		, false);
		xhr.open('PUT', postURL + "/" + file.fileName, true);
		xhr.setRequestHeader('X-Filename', file.fileName);
 
		xhr.send(file);
	});
}

function addToList(dataTransfer, dropListing) {	
	log("addToList");
	var files = dataTransfer.files;
	for (var i = 0, f; f = files[i]; i++) {
		var reader = new FileReader();
		reader.onload = (function(theFile) {
			return function(e) {
				var li = $("<li>");
				var img = $("<img>");
				$(dropListing).append(li);
				li.append(img);
	
				var data = e.target.result;
				img.attr("src", data); // base64 encoded string of local file(s)
				img.attr("width", 150);
				img.attr("height", 150);		
				log("done addToList");	
			};
		})(f);
		reader.readAsDataURL(f);
	}	
}

function upload1(postURL, fieldName, data) {
	log("upload", postURL, fieldName, data.files.length);
	var boundary = '------multipartformboundary' + (new Date).getTime();
	var dashdash = '--';
	var crlf     = '\r\n';

	/* Build RFC2388 string. */
	var builder = '';

	builder += dashdash;
	builder += boundary;
	builder += crlf;

	var xhr = new XMLHttpRequest();

	/* For each dropped file. */
	for (var i = 0; i < data.files.length; i++) {
		log("build upload for file", i);
		var file = data.files[i];

		/* Generate headers. */
		builder += 'Content-Disposition: form-data; name="' + fieldName + '"';
		if (file.fileName) {
			builder += '; filename="' + file.fileName + '"';
		}
		builder += crlf;

		builder += 'Content-Type: application/octet-stream';
		builder += crlf;
		builder += crlf; 

		/* Append binary data. */
		builder += file.getAsBinary();
		builder += crlf;

		/* Write boundary. */
		builder += dashdash;
		builder += boundary;
		builder += crlf;
		log("done..");
	}

	/* Mark end of the request. */
	builder += dashdash;
	builder += boundary;
	builder += dashdash;
	builder += crlf;
	log("begin upload", builder.length);
	xhr.open("POST", postURL, true);
	xhr.setRequestHeader('content-type', 'multipart/form-data; boundary=' + boundary);
	xhr.sendAsBinary(builder);        

	xhr.onload = function(event) {
		/* Response from server */
		log("response", event);
		if (xhr.responseText) {
			log(xhr.responseText);
		}

	};

}









