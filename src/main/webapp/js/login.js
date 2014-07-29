
function setupLogin() {
  var loginBtn = $("#login");

  loginBtn.on("click", function(e) {
    e.preventDefault();
    var user = $("#user").val();
    var pass = $("#pass").val();

    $.ajax("/login", {
      type: "POST",
      data: {"login": user, "password": pass}
    })
    .done(function(data) {
      showAlert("success", "<strong>Yay!</strong> You are now logged in as " + user + ".")
      showRepos(data);
      $("#loginForm").hide();
      $("#uploadingDiv").show();
    })
    .fail(function() {
      showAlert("danger", "<strong>Failed!</strong> Could not login, check your password and try again.");
    });

  });
}