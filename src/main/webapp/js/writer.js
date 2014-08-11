
function setupWriterBtns() {
  $("#submitReview").on("click", function(e) {
    e.preventDefault();

    var data = {
      login: getSelectedLogin(),
      repo: getSelectedRepo(),
      pathToPaper: pathToPaper,
      paper: getSelectedFile(),
      reviewers: getSelectedReviewers()
    };

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

    console.log(data);

    $.post("/reviewRequest?access_token=" + accessToken, JSON.stringify(data))
    .done(function(data) {
      showAlert("success", "Your review request has been submitted.");
    })
    .fail(function(data) {
      if(data.status == 417)
        showAlert("danger", "Some reviewers could not be added for review. Only members of the organization can review papers in an organization.")
      else
        showAlert("danger", "There has been a problem submitting your review.");
    });
  });

  setupAddOtherReviewer();
}

function setupWriter() {
  getRepoSources();
  $("#writerDiv").fadeIn();
  $("#submitReview").attr("disabled", true);
}

function getPossibleReviewers(login) {
  $.get("/reviewer?access_token=" + accessToken + "&login=" + escape(login))
  .done(function(data) {
    $("#reviewersList").empty();

    if(data.length == 0) {
      $("<h5 />")
        .text("No Reviewers")
        .appendTo($("#reviewersList"));
      return;
    }

    for(var i=0; i<data.length; i++) {
      showReviewer(data[i]);
    }
  })
  .fail(function(data) {
    console.log(data);
  });
}

function showReviewer(r, selected) {
  if(selected === undefined) selected = false;

  var btn = $("<a />")
    .attr("class", "list-group-item")
    .text(getUserText(r))
    .attr("data-login", r["login"])
    .on("click", function() {
      btn.toggleClass("active");
      onSelectStuff();
    })
    .appendTo($("#reviewersList"));

    if(selected) {
      btn.addClass("active");
      onSelectStuff();
    }
}

function setupAddOtherReviewer() {

  $("#addOtherReviewer").select2({
    minimumInputLength: 1,
    cache: true,
    ajax: {
      url: "/search",
      dataType: "json",
      quietMillis: 500,
      data: function(term, page) {
        return {
          search: term,
          access_token: accessToken
        };
      },
      results: function(data, page) {
        var results = [];
        for(var i=0; i<data.length; i++) {
          results.push({id: data[i], text: data[i]});
        }
        return {results: results};
      }
    }
  });

  $("#addOtherReviewerBtn").on("click", function(e) {
    e.preventDefault();
    addReviewer();
  });

  $("#s2id_autogen1_search").keydown(function(e) {
    if(e.keyCode == 13) {
      addReviewer();
    }
  });
}

function addReviewer() {
  var valA = $("#addOtherReviewer").val();
  var valB = $("#s2id_autogen1_search").val();
  var login = valA != "" ? valA : valB;

  $.get("/user?access_token=" + accessToken + "&user=" + escape(login))
  .done(function(data) {
    showReviewer(data, true);
    $("#select2-chosen-1").text("");
    $("#addOtherReviewer").val("");
  });
}

function getSelectedReviewers() {
  var selectedUsers = $("#reviewersList .list-group-item.active");
  var users = [];
  for(var i=0; i<selectedUsers.length; i++) {
    users[i] = $(selectedUsers[i]).data("login");
  }

  return users;
}

function onSelectStuff() {
  if(getSelectedLogin() != null && getSelectedFile() != null
     && getSelectedRepo() != null && getSelectedReviewers().length > 0) {
    $("#submitReview").attr("disabled", false);
  }
  else {
    $("#submitReview").attr("disabled", true);
  }
}