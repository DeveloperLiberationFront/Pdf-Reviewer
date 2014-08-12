var accessToken = "";


function setupLogin(afterLogin) {
  accessToken = localStorage.githubAccessToken;

  var code = getQueryParams("code");
  var clientId = escape("b08a834d3b797794e83f");

  $("#login").on("click", function(e) {
    e.preventDefault();
    window.location = "https://github.com/login/oauth/authorize?" + "client_id=" + clientId + "&scope=repo&state=totallyrandomstring";
  });

  // If there is no code or accessToken, have the user login.
  if(code === null && accessToken === undefined) {
    $("#login").fadeIn();
  }
  // If there is an access token, the user is logged in.
  else if(accessToken !== undefined) {
    loggedIn(afterLogin);
  }
  // If we have a code, automatically log the user in.
  else {
    $.get("/login?code=" + code)
    .done(function(data) {
      accessToken = escape(data.access_token);
      
      if(data.access_token === undefined) {
        showAlert("danger", "<strong>Failed</strong> to login.");
        $("#login").fadeIn();
        return;
      }

      localStorage.githubAccessToken = accessToken;
      loggedIn(afterLogin);
    })
    .fail(function(data) {
      showAlert("danger", "<strong>Failed</strong> to login.");
      $("#login").fadeIn();
    })
  }
}

function getAvatar(fail, succeed) {
  $.get("/avatar?access_token=" + accessToken)
  .done(function(data) {
    $("#avatar").attr("src", data.avatar);
    $("#loginName").text(data.login);

    $("#loggedIn").fadeIn();
    succeed();
  })
  .fail(function(data) {
    $("#writerDiv").hide();
    localStorage.removeItem("githubAccessToken");
    fail();
  })
}

function loggedIn(afterLogin) {
  getAvatar(setupLogin, function() {
    setupLogout();
    afterLogin();
  });
}

function setupLogout() {
  $("#logout").on("click", function(e) {
    e.preventDefault();
    localStorage.removeItem("githubAccessToken");
    window.location = "/";
  });
}