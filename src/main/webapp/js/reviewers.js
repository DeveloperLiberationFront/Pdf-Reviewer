function getPossibleReviewers() {
  $.get("/reviewer?access_token=" + accessToken)
  .done(function(data) {
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
  var email = "email" in r ? " (" + r["email"] + ")" : "";
  var name = "name" in r ? r["name"] + " - " : "";

  var text = name + r["login"] + email;

  var btn = $("<a />")
    .attr("class", "list-group-item")
    .text(text)
    .attr("data-login", r["login"])
    .on("click", function() {
      btn.toggleClass("active");
    })
    .prependTo($("#reviewersList"));
}