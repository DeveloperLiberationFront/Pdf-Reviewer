function setupPdfUpload() {
  var fileSelecters = $("#pdf-file");
  var fileSelecter = $("#pdf-file")[0];
  var uploadBtn = $("#upload");

  uploadBtn.attr("disabled", true);

  fileSelecters.on("change", function() {
    if($(this)[0].files[0] != null) {
      var selected = $(this)[0].files[0].name;
      showAlert("info", selected + " has been selected to upload.");
      uploadBtn.attr("disabled", false);
    }
    else {
      showAlert("warning", "The file to upload has been deselected.");
      uploadBtn.attr("disabled", true);
    }
  });

  uploadBtn.on("click", function(e) {
    e.preventDefault();

    var file = fileSelecter.files[0];
    
    if(file == null) {
      showAlert("danger", "<strong>Oh no!</strong> You forgot to select a file.");
      return;
    }

    uploadBtn.val("Uploading...")
    uploadBtn.attr("disabled", true)

    var formData = new FormData();
    formData.append("file", file);
    
    var repoUrl = getRepoUrl();

    $.ajax("/pdf?repoUrl=" + escape(repoUrl), {
      type: "POST",
      processData: false,
      contentType: false,
      data: formData 
    })
    .done(function() {
      showAlert("success", "<strong>Success!</strong> Your PDF has been processed.");
    })
    .fail(function() {
      showAlert("danger", "<strong>Uh oh!</strong> There has been an error processing your PDF file.");
    })
    .always(function() {
      uploadBtn.val("Upload");
      uploadBtn.attr("disabled", false);
    })

  });
}