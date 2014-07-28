$(document).ready(main);

function main() {
  setupEvents();

  var fileSelect = $("#pdf-file")[0];
  var uploadBtn = $("#upload");

  uploadBtn.on("click", function(e) {
    e.preventDefault();

    var file = fileSelect.files[0];
    
    if(file == null) {
      showAlert("danger", "<strong>Oh no!</strong> You forgot to select a file.");
      return;
    }

    uploadBtn.val("Uploading...")
    uploadBtn.attr("disabled", true)

    //var reader = new FileReader();
    //reader.readAsArrayBuffer(file);
    
    var formData = new FormData();
    formData.append("file", file);

    //reader.onloadend = function(event) {
      $.ajax("/pdf", {
        type: "POST",
        processData: false,
        contentType: false,
        data: formData //event.target.result
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
    //}

  })
}

function setupEvents() {
  var fileSelect = $("#pdf-file");

  fileSelect.on("change", function() {
    if($(this)[0].files[0] != null) {
      var selected = $(this)[0].files[0].name;
      showAlert("info", selected + " has been selected to upload.");
    }
    else {
      showAlert("warning", "The file to upload has been deselected.");
    }
  });
}

function showAlert(type, text) {
  $(".alert").remove();

  var alert = $("<div/>")
    .attr("class", "alert alert-dismissible fade in alert-" + type)
    .attr("role", "alert")
    .html("<button type='button' class='close' data-dismiss='alert'><span aria-hidden='true'>&times;</span><span class='sr-only'>Close</span></button>" + text);
  
  alert.hide().appendTo($("#alerts")).fadeTo("medium", 1);
}

/*
<button type="button" 
        class="close" 
        data-dismiss="alert">
  <span aria-hidden="true">
    &times;
  </span>
  <span class="sr-only">
    Close</span>
</button>
*/