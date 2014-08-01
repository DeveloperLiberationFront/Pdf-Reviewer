
function showRepos(data) {
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

        getFiles($(this).text(), "/");
      })
      .prependTo($("#repoList"));
  }

  $("#selectRepo").show();
}

function getRepos() {
  $.get("/repo?access_token=" + accessToken)
  .done(function(data) {
    showRepos(data);
  })
  .fail(function(data) {
    console.log(data);
  })
}

function getSelectedRepo() {
  var active = $("#repoList .list-group-item.active");
  if(active.length > 0)
    return active.data("name");
  else
    return null;
}

function getSelectedFile() {
  var active = $(".fileList .list-group-item.active");
  if(active.length > 0)
    return active.text();
  else
    return null;
}

function getFiles(repoName, path) {
  $.get("/files?access_token=" + accessToken + "&repo=" + escape(repoName) + "&path=" + escape(path))
    .done(function(data) {
      showFiles(repoName, path, data);
    })
    .fail(function(data) {
    });
}

function showFiles(repoName, path, files) {
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
        getFiles(repoName, backPath);
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
          getFiles(repoName, path + name);
        }
        else {
          $("#" + repoNameId + "-fileList .list-group-item.active").removeClass("active");
          $(this).addClass("active");
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