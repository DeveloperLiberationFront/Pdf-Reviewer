function getRepoSources() {
  $.get("/repoSource?access_token=" + accessToken)
    .done(function(data) {
      $("#repoSourceList").empty();
      for(var i=0; i<data.length; i++) {
        var source = data[i];
        var sourceBtn = $("<a />")
          .attr("class", "list-group-item")
          .text(source)
          .on("click", function(e) {
            $("#repoSourceList .list-group-item.active").removeClass("active");
            $(this).addClass("active");
            getRepos($(this).text());
            getPossibleReviewers($(this).text());
          })
          .appendTo($("#repoSourceList")); 
      }
    });
}


function showRepos(data, login) {
  if(data.length == 0) {
    $("<h5 />")
      .text("No Repositories")
      .appendTo($("#repoList"));

    return;
  }

  for(var i=0; i<data.length; i++) {
    var repo = data[i];

    var repoBtn = $("<a />")
      .attr("class", "list-group-item")
      .data("name", repo["name"])
      .html(repo["name"])
      .append($("<div />")
                .attr("id", repo["name"] + "-fileList")
                .attr("class", "fileList")
                .css("display", "none"))
      .attr("data-url", repo["url"])
      .on("click", function() {
        $("#repoList .list-group-item.active").removeClass("active");
        $(this).addClass("active");

        getFiles($(this).text(), login, "/");
      })
      .appendTo($("#repoList"));
  }

  $("#selectRepo").show();
}

function getRepos(login) {
  $.get("/repo?access_token=" + accessToken + "&login=" + escape(login))
    .done(function(data) {
      $("#repoList").empty();
      showRepos(data, login);
    })
    .fail(function(data) {
      console.log(data);
    })
}

function getSelectedInList(list) {
  var active = $(list + " .list-group-item.active");
  if(active.length > 0)
    return active.text();
  else
    return null;
}

function getSelectedLogin() {
  return getSelectedInList("#repoSourceList");
}

function getSelectedRepo() {
  var active = $("#repoList .list-group-item.active");
  if(active.length > 0)
    return active.data("name");
  else
    return null;
}

function getSelectedFile() {
  return getSelectedInList(".fileList");
}

function getFiles(repoName, login, path) {
  $.get("/files?access_token=" + accessToken + "&repo=" + escape(repoName) + "&login=" + escape(login) + "&path=" + escape(path))
    .done(function(data) {
      showFiles(repoName, login, path, data);
    })
    .fail(function(data) {
    });
}

function showFiles(repoName, login, path, files) {
  $(".fileList").empty();
  $(".fileList").slideUp();

  var repoNameId = getRepoId(repoName);

  if(path.trim() != "/" && path.trim() != "") {
    $("<a />")
      .attr("class", "list-group-item folder-list-group-item")
      .append($("<img />")
                .attr("class", "folder-icon")
                .attr("src", "images/octofolder.png"))
      .append("...")
      .on("click", function(e) {
        e.stopPropagation();
        backPath = path.substr(0, path.lastIndexOf("/"));
        getFiles(repoName, login, backPath);
      })
      .appendTo($("#" + repoNameId + "-fileList"));
  }

  // Show dirs first
  var dirs = [];
  var notDirs = [];
  for(var i=0; i<files.length; i++) {
    var file = files[i];
    if(file.type == "dir")
      dirs.push(file);
    else
      notDirs.push(file);
  }

  var sortFiles = dirs.concat(notDirs);

  for(var i=0; i<sortFiles.length; i++) {
    var file = sortFiles[i];

    var folderImg = $("<img />")
      .attr("class", "folder-icon")
      .attr("src", "images/octofolder.png");

    var fileA = $("<a />")
      .attr("class", "list-group-item")
      .text(file.name)
      .data("type", file.type)
      .data("name", file.name)
      .on("click", function(e) {
        e.stopPropagation();

        var name = $(this).data("name");
        var type = $(this).data("type");

        if(type == "dir") {
          getFiles(repoName, login, path + "/" + name);
        }
        else {
          $("#" + repoNameId + "-fileList .list-group-item.active").removeClass("active");
          $(this).addClass("active");
          onSelectStuff();
        }
      });

      if(file.type == "dir") {
        fileA.prepend(folderImg);
        fileA.addClass("folder-list-group-item");
      }
      else {
        fileA.addClass("file-list-group-item");
      }

      fileA.appendTo($("#" + repoNameId + "-fileList"));
      $("#" + repoNameId + "-fileList").slideDown({easing: "linear"});
  }
}

function getRepoId(myid) {
  return myid.replace( /(:|\.|\[|\])/g, "\\$1" );
}