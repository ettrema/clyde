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
            $("#ajaxLoading").show();
            button.text('Uploading...');
            this.disable();
        },
        onComplete: function(file, response){
            $("#ajaxLoading").hide();
            button.text('Upload photos');
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