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
    
    dialogButton.addEventListener('click', function() {
      var repoName = "";
      if($("#repoList").val() != '' && $("#repoList").val().length != 0){
        repoName = $("#repoList").val();
      } else if($(".mdl-tabs__tab.is-active").text().length != 0){
        repoName = $(".mdl-tabs__tab.is-active").text();
      } else {
        alert("Please select a repository.");
        return;
      }
      showLoading();
      var formData = new FormData();
      formData.append("file", inputFile);

      let params = new URLSearchParams(location.search.slice(1));
      let access_token = params.getAll('access_token');

      var postURL = "/fileupload?access_token=" + access_token;
      
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
        showDialog({
          title: 'Success',
          positive: {
              title: 'Close'
          },
          cancelable: false
          }, data);
    //       var dialog = document.querySelector('#dialog');
    // if (! dialog.showModal) {
    //   dialogPolyfill.registerDialog(dialog);
    // }
        dialog.showModal();
      });
    });
    dialog.querySelector('button:not([disabled])')
    .addEventListener('click', function() {
      dialog.close();
    });
  
};

/* library */
/* Source:https://github.com/oRRs/mdl-jquery-modal-dialog */
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

function showDialog(options, data) {
  options = $.extend({
      id: 'dialog',
      title: null,
      negative: false,
      positive: false,
      cancelable: true,
      contentStyle: null,
      onLoaded: false
  }, options);

  // remove existing dialogs
  $('.dialog-container').remove();
  $(document).unbind("keyup.dialog");

  $('<div id="' + options.id + '" class="dialog-container"><div class="mdl-card hello mdl-shadow--16dp"></div></div>').appendTo("body");
  var dialog = $('#dialog');
  var content = dialog.find('.mdl-card');
  if (options.contentStyle != null) content.css(options.contentStyle);
  if (options.title != null) {
      $('<h5>' + options.title + '</h5>').appendTo(content);
  }
  $(content).html(data);
  if (options.positive) {
      var buttonBar = $('<div class="mdl-card__actions dialog-button-bar"></div>');
      if (options.positive) {
          options.positive = $.extend({
              id: 'positive',
              title: 'OK',
              onClick: function () {
                  return false;
              }
          }, options.positive);
          var posButton = $('<button class="mdl-button mdl-button--colored mdl-js-button mdl-js-ripple-effect" onClick="window.location.reload();" id="' + options.positive.id + '">' + options.positive.title + '</button>');
          posButton.click(function (e) {
              e.preventDefault();
              if (!options.positive.onClick(e))
                  hideDialog(dialog)
          });
          posButton.appendTo(buttonBar);
      }
      buttonBar.appendTo(content);
  }
  componentHandler.upgradeDom();
  if (options.cancelable) {
      dialog.click(function () {
          hideDialog(dialog);
      });
      $(document).bind("keyup.dialog", function (e) {
          if (e.which == 27)
              hideDialog(dialog);
      });
      content.click(function (e) {
          e.stopPropagation();
      });
  }
  setTimeout(function () {
      dialog.css({opacity: 1});
      if (options.onLoaded)
          options.onLoaded();
  }, 1);
}

function hideDialog(dialog) {
  $(document).unbind("keyup.dialog");
  dialog.css({opacity: 0});
  setTimeout(function () {
      dialog.remove();
  }, 400);
}
