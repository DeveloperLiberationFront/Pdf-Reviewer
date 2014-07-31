
$(document).ready(main);

function main() {
  $("#login").hide();
  $("#uploadingDiv").hide();
  $("#loggedIn").hide();

  setupLogin();
  setupPdfUpload();
  setupReviewRequest();
}

function showWriterPage() {
  getRepos();
  getPossibleReviewers();

  $("#writerDiv").fadeIn();
}

function setupReviewRequest() {
  $("#submitReview").on("click", function(e) {
    e.preventDefault();

    var data = {
      repo: getRepoUrl(),
      reviewers: []
    };

    var selectedUsers = $("#reviewersList .list-group-item.active");
    for(var i=0; i<selectedUsers.length; i++) {
      data.reviewers[i] = $(selectedUsers[i]).data("login");
    }

    if(data.repo == null) {
      showAlert("warning", "Please select a repository.");
      return;
    }

    if(data.reviewers.length == 0) {
      showAlert("warning", "Please select at least one reviewer.");
      return;
    }

    $.post("/review?access_token=" + accessToken, JSON.stringify(data))
    .done(function(data) {
      showAlert("success", "Your review request has been submitted.");
    })
    .fail(function(data) {
      showAlert("danger", "There has been a problem submitting your review.");
    });
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

function getQueryParams(key) {
    key = key.replace(/[*+?^$.\[\]{}()|\\\/]/g, "\\$&"); // escape RegEx meta chars
    var match = location.search.match(new RegExp("[?&]"+key+"=([^&]+)(&|$)"));
    return match && decodeURIComponent(match[1].replace(/\+/g, " "));
}