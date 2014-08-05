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

function getPossibleReviewers() {
  $.get("/reviewer?access_token=" + accessToken)
  .done(function(data) {
    $("#reviewersList").empty();
    for(var i=0; i<data.length; i++) {
      showReviewer(data[i]);
    }

    $("#selectReviewers").show();
  })
  .fail(function(data) {
    console.log(data);
  });
}

function showReviewer(r) {
  var btn = $("<a />")
    .attr("class", "list-group-item")
    .text(getUserText(r))
    .attr("data-login", r["login"])
    .on("click", function() {
      btn.toggleClass("active");
    })
    .prependTo($("#reviewersList"));
}