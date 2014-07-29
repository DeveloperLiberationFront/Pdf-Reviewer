
function setupLogin() {
  var code = getQueryParams("code");
  var clientId = escape("afa90e71a06d85c5fcb5");

  if(code == null) {
    var loginBtn = $("#login");
    loginBtn.on("click", function(e) {
      e.preventDefault();
      window.location = "https://github.com/login/oauth/authorize?" + "client_id=" + clientId + "&scope=repo&state=totallyrandomstring";
    });
  }
  else {
    $.get("/login?code=" + code)
    .done(function(data) {
      showAlert("success", "You are now logged in.");
      console.log(data);
    })
    .fail(function(data) {
      showAlert("danger", "<strong>Failed</strong> to login.");
      console.log(data);
    })
  }

}