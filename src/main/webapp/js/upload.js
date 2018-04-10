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
      displayMessage(input.files[0]);
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

function displayMessage(inputFile) {
  'use strict';
  var dialogButton = document.querySelector('#upload-button');
  var dialog = document.querySelector('#dialog');
  if (! dialog.showModal) {
    dialogPolyfill.registerDialog(dialog);
  }
  
  dialogButton.addEventListener('click', function() {
    var formData = new FormData();
    formData.append("file", inputFile);

    let params = new URLSearchParams(location.search.slice(1));
    let access_token = params.getAll('access_token');

    var postURL = "http://localhost:9090/fileupload?access_token=" + access_token;
    postURL += "&selectedRepository=" + escape($(".mdl-tabs__tab.is-active").text());
    postURL += "&selectedBranch=" + escape($('#branchList').find(":selected").text());

    let t0 = performance.now();

    $.ajax({
      type: "POST",
      url: postURL,
      processData: false,
      contentType: false,
      data: formData
    })
    .done(function( data ) {
      var timeTakenString = "Time Taken (milliseconds):" + (performance.now() - t0) ;
      alert(timeTakenString);

      $(".mdl-dialog__content").html(data);
      dialog.showModal();
    });
  });
  dialog.querySelector('button:not([disabled])')
  .addEventListener('click', function() {
    dialog.close();
  });
};
