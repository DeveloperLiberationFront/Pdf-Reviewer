
$(document).ready(main);

function main() {
  $("#login").hide();
  $("#reviewerDiv").hide();
  $("#writerDiv").hide();
  $("#loggedIn").hide();
  $("#statusDiv").hide();

  var repoName = getQueryParams("repoName");
  var writer = getQueryParams("writer");

  if(repoName != null && writer != null) {
    setupLogin(setupReviewer);
    $("#reviewerInstruction").show();
  }
  else {
    setupLogin(setupStatus);
  }

  $("#showWriterBtn").on("click", function() {
    setupWriter();
    $("#statusDiv").hide();
    $("#writerDiv").fadeIn();
  });

  $("#showStatusBtn").on("click", function() {
    $("#writerDiv").hide();
    $("#reviewerDiv").hide();
    $("#statusDiv").fadeIn();
  });

}

function setupWriter() {
  getRepos();
  getPossibleReviewers();
  $("#writerDiv").fadeIn();

  $("#submitReview").on("click", function(e) {
    e.preventDefault();

    var data = {
      repo: getSelectedRepo(),
      paper: getSelectedFile(),
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

    if(data.paper == null) {
      showAlert("warning", "Please select a paper to be reviewed.");
      return;
    }

    $.post("/reviewRequest?access_token=" + accessToken, JSON.stringify(data))
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

function getUserText(r) {
  var email = "email" in r ? " (" + r["email"] + ")" : "";
  var name = "name" in r ? r["name"] + " - " : "";

  return name + r["login"] + email;
}