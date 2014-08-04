
function setupStatus() {
  $("#statusDiv").fadeIn();
  getReviews();
}

function getReviews() {
  $.get("/status?access_token=" + accessToken)
    .done(function(data) {
      showPendingRequests(data.requests);
      showPendingReviews(data.reviews);
    });
}

function showPendingRequests(requests) {
  showReviews("reviewer", requests);
}

function showPendingReviews(reviews) {
  showReviews("writer", reviews);
}

function showReviews(writerOrReviewer, reviews) {
  if(reviews.length == 0) {
    showEmptyReviews(writerOrReviewer);
  }

  var isReviewer = writerOrReviewer == "reviewer";
  var wrText = isReviewer ? "Writer" : "Reviewer";
  var div = isReviewer ? "#reviewRequests" : "#pendingReviews"


  function getOtherUser(r) {
    if(isReviewer) {
      return r.writer;
    }
    else
      return r.reviewer
  }

  for(var i=0; i<reviews.length; i++) {
    var review = reviews[i];
    console.log(review);
    var otherUser = getUserText(getOtherUser(review));

    var html = "<table>"
             + "<tr><td><label>" + wrText + "</label></td><td><a href='https://github.com/" + getOtherUser(review).login + "'>" + otherUser + "</a></td></tr>"
             + "<tr><td><label>Repository:</label></td><td><a href='https://github.com/" + getOtherUser(review).login + "/" + review.repo + "'>" + review.repo + "</a></td></tr>"
             + "<tr><td><label>Paper:</label></td><td><a href='https://github.com/'" + getOtherUser(review).login + "/" + review.repo + "/blob/master/" + review.paper + "'>" + review.paper + "</a></td></tr>"
             + "</table>";

    var reviewDiv = $("<div />")
      .attr("class", "panel panel-default")
      .append($("<div />")
        .attr("class", "panel-heading")
        .text(otherUser))
      .append($("<div />")
        .attr("class", "panel-body")
        .html(html));

      var cancelReviewBtn = $("<button>")
        .attr("class", "btn btn-danger")
        .data("writer", review.writer.login)
        .data("reviewer", review.reviewer.login)
        .data("repo", review.repo)
        .on("click", function(e) {
            e.preventDefault();
            var data = $(this).data();
            cancelReview(data.writer, data.reviewer, data.repo);
        });

    if(isReviewer) {
      reviewDiv.find(".panel-body")
        .append($("<a />")
          .attr("href", review.link)
          .attr("class", "btn btn-primary")
          .text("Review Now"));

      cancelReviewBtn.text("Decline Request");
    }
    else {
      cancelReviewBtn.text("Remove Request")
    }
    
    reviewDiv.find(".panel-body").append(cancelReviewBtn);

    reviewDiv.appendTo($(div));
  }

}

function showEmptyReviews(writerOrReviewer) {
  var isReviewer = writerOrReviewer == "reviewer";
  var div = isReviewer ? "#reviewRequests" : "#pendingReviews"
  $(div).append("<h5>No Reviews</h5>")
}

function cancelReview(writer, reviewer, repo) {
  $.ajax("/reviewRequest?access_token=" + accessToken + "&writer=" + escape(writer) + "&reviewer=" + escape(reviewer) + "&repo=" + repo, 
  {
    type: "DELETE"
  })
  .done(function(data) {
    $.each($("#reviewRequests div.panel, #pendingReviews div.panel"), function(i, elem) {
      var btn = $(elem).find("button.btn-danger");
      if(btn.data("writer") == writer && btn.data("reviewer") == reviewer && btn.data("repo") == repo) {
        $(elem).fadeOut();
      }
    });
  })
  .fail(function(data) {
    console.log(data);
  });
}