function readURL(input) {
  if($('#branchList').find(":selected").text().length == 0){
    alert("Please select a repository first.")
  } else {
    if(!input.files[0].name.endsWith(".pdf")){
      alert("Please upload pdf files only.")
    } else {
      //if($('#branchList').find(":selected").text().length != 0){
        enableUploadButton();
      //}
      
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
    showLoading();
    var formData = new FormData();
    formData.append("file", inputFile);

    let params = new URLSearchParams(location.search.slice(1));
    let access_token = params.getAll('access_token');

    var postURL = "/fileupload?access_token=" + access_token;
    var repoName = "";
    if($("#repoList").val() != '' && $("#repoList").val().length != 0){
     repoName = $("#repoList").val();
    } 
    if($(".mdl-tabs__tab.is-active").text().length != 0){
      repoName = $(".mdl-tabs__tab.is-active").text();
    }
    postURL += "&selectedRepository=" + escape(repoName);
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
      hideLoading();
      // var timeTakenString = "Time Taken (milliseconds):" + (performance.now() - t0) ;
      // alert(timeTakenString);

      $(".mdl-dialog__content").html(data);
      dialog.showModal();
    });
  });
  dialog.querySelector('button:not([disabled])')
  .addEventListener('click', function() {
    dialog.close();
  });
};

/* library */
function showLoading() {
  // remove existing loaders
  $('.loading-container').remove();
  $('<div id="orrsLoader" class="loading-container"><div><div class="mdl-spinner mdl-js-spinner is-active"></div></div></div>').appendTo("body");

  componentHandler.upgradeElements($('.mdl-spinner').get());
  setTimeout(function () {
      $('#orrsLoader').css({opacity: 1});
  }, 1);
}

function hideLoading() {
  $('#orrsLoader').css({opacity: 0});
  setTimeout(function () {
      $('#orrsLoader').remove();
  }, 400);
}
