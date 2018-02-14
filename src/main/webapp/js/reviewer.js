/*global showAlert, getQueryParams, accessToken */
/*exported setupReviewerBtns*/
function setupReviewerBtns() {
  var uploadBtn = $("#upload");
  var fileSelecters = $("#pdf-file");
  var fileSelecter = $("#pdf-file")[0];

  uploadBtn.on("click", function(e) {
    e.preventDefault();

    // Get data and be sure everything is there before it is sent.
    var file = fileSelecter.files[0];
    
    if(!file) {
      showAlert("danger", "Be sure to select a file.");
      return;
    }

    var repoName = getQueryParams("repoName");
    if(!repoName) {
      showAlert("danger", "No repository specified, be sure your link is correct.");
      return;
    }

    var writer = getQueryParams("writer");
    if(!writer) {
      showAlert("danger", "No writer specified, be sure your link is correct.");
    }

    // Show that data is being sent.
    uploadBtn.val("Uploading...");
    uploadBtn.attr("disabled", true);

    // Attach the file to the request data.
    var formData = new FormData();
    formData.append("file", file);

    // Send data
    $.ajax("/review?access_token=" + accessToken + "&repoName=" + escape(repoName) + "&writer=" + escape(writer), {
      type: "POST",
      processData: false,
      contentType: false,
      data: formData 
    })
    .done(function(data) {
      var pdfUrl = "https://github.com/" + writer + "/" + repoName + "/blob/master/" + data;
      showAlert("success", "<strong>Success!</strong> Your PDF has been processed. Click <a href=" + pdfUrl + ">here</a> view it.");
    })
    .fail(function(data) {
      if (data.status === 409) {
        showAlert("danger", "It appears you have already submitted a review for this paper...");
      } else {
        showAlert("danger", "<strong>Uh oh!</strong> There has been an error submitting your review.");
      }
      
    })
    .always(function() {
      uploadBtn.val("Upload");
      uploadBtn.attr("disabled", false);
    });

  });

  // Show that the file has been changed.
  fileSelecters.on("change", function() {
    if($(this)[0].files[0]) {
      var selected = $(this)[0].files[0].name;
      showAlert("info", selected + " has been selected to upload.");
      uploadBtn.attr("disabled", false);
    }
    else {
      showAlert("warning", "The file to upload has been deselected.");
      uploadBtn.attr("disabled", true);
    }
  });
}

function setupReviewer() {
  $("#reviewerDiv").fadeIn();
  $("#upload").attr("disabled", true);
  setDownloadBtnLink(getQueryParams("writer"), getQueryParams("repoName"), getQueryParams("paper"));
}

function setDownloadBtnLink(writer, repoName, paper) {
  if(!writer || !repoName || !paper) {
    $("#downloadPaper").hide();
    return;
  }

  var url = "https://github.com/" + writer + "/" + repoName + "/raw/master/" + paper;
  $("#downloadPaper").attr("href", url);
}