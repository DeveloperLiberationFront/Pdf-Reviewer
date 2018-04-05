function readURL(input) {
  if(!input.files[0].name.endsWith(".pdf")){
    alert("Please upload pdf files only.")
  } else {
    enableUploadButton();
    if (input.files && input.files[0]) {

      var reader = new FileReader();

      reader.onload = function(e) {
        $('.pdf-upload-wrap').hide();

        $('.file-upload-content').show();

        $('.pdf-title').html(input.files[0].name);
      };

      reader.readAsDataURL(input.files[0]);

      reader.addEventListener("load", function () {
        let params = new URLSearchParams(location.search.slice(1));
        let access_token = params.getAll('access_token');

        // alert($(".mdl-tabs__tab.is-active").text());
        // alert($('#branchList').find(":selected").text());

        let t0 = performance.now();

        $.post(
          "http://localhost:9090/fileupload?access_token=" + access_token, 
          {
            "dataurl" : reader.result,
            "selectedRepository" : $(".mdl-tabs__tab.is-active").text(),
            "selectedBranch" : $('#branchList').find(":selected").text()
          }
        )
        .done(function( data ) {
          alert( "Time Taken (milliseconds):" + (performance.now() - t0) );
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

function enableUploadButton(){
  $('#upload-button').prop("disabled", false);
}
function dissableUploadButton(){
  $('#upload-button').prop("disabled", true);
}
function removeUpload() {
  $('.file-upload-input').replaceWith($('.file-upload-input').clone());
  $('.file-upload-content').hide();
  $('.pdf-upload-wrap').show();
  dissableUploadButton();
}
$('.pdf-upload-wrap').bind('dragover', function () {
		$('.pdf-upload-wrap').addClass('pdf-dropping');
	});
	$('.pdf-upload-wrap').bind('dragleave', function () {
		$('.pdf-upload-wrap').removeClass('pdf-dropping');
});
