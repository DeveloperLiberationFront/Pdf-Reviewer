function readURL(input) {
  if(!input.files[0].name.endsWith(".pdf")){
    alert("Please upload pdf files only.")
  } else {
    if (input.files && input.files[0]) {

      var reader = new FileReader();

      reader.onload = function(e) {
        $('.pdf-upload-wrap').hide();

        $('.file-upload-content').show();

        $('.pdf-title').html(input.files[0].name);
      };

      reader.readAsDataURL(input.files[0]);

      reader.addEventListener("load", function () {
        $.post( "http://localhost:9090/fileupload", {"dataurl" : reader.result})
        .done(function( data ) {
          alert( "Data Loaded: " + data );
        });
      }, false);


      // $.ajax({
      //   type: "POST",
      //   url: "http://localhost:9090/fileupload",
      //   data: reader.result,
      //   success: success,
      //   dataType: 'json'
      // });

    } else {
      removeUpload();
    }
  }
}

function removeUpload() {
  $('.file-upload-input').replaceWith($('.file-upload-input').clone());
  $('.file-upload-content').hide();
  $('.pdf-upload-wrap').show();
}
$('.pdf-upload-wrap').bind('dragover', function () {
		$('.pdf-upload-wrap').addClass('pdf-dropping');
	});
	$('.pdf-upload-wrap').bind('dragleave', function () {
		$('.pdf-upload-wrap').removeClass('pdf-dropping');
});
