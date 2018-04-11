let params = new URLSearchParams(location.search.slice(1));
let access_token = params.getAll('access_token');


window.onload = function() {
    //Grab the inline template
    $.get('/repositories?access_token=' + access_token).done((repositories)=>{
        let repoTemplateJSON = {repos:[]}
        for (let i = 0; i < repositories.length; i++) {
            repoTemplateJSON.repos.push({id: 'repo'+i, name: repositories[i]});
        }

        console.log(JSON.stringify(repoTemplateJSON))
        let repoTemplate = document.getElementById('repoTemplate').innerHTML;
        Mustache.parse(repoTemplate);
        let repoTemplateRendered = Mustache.render(repoTemplate, repoTemplateJSON);
        document.getElementById('repoTemplateRendered').innerHTML = repoTemplateRendered;

        $('.mdl-tabs__tab').on('click', function() {
            $(this).addClass('is-active');
            var otherRepos = $(this).siblings().removeClass('is-active');
            populateBranches(repoTemplateJSON, $(this).text());
          })

        //Populates the searchbar with repository names so that user can type and  the tool will autocomplete
        console.log(repoTemplateJSON.repos.map(repo=>repo.name.repoName))
        var availableRepo = repoTemplateJSON.repos.map(repo=>repo.name.repoName);
        $( "#repoList" ).autocomplete({
            source: availableRepo
        });
    });
  }

function populateBranches(repoTemplateJSON, repoName){
    let repoTemplate = document.getElementById('branchTemplate').innerHTML;
    let selectedRepo = repoTemplateJSON.repos.find((repos)=>repos.name.repoName === repoName)
    Mustache.parse(repoTemplate);
    let branchTemplateRendered = Mustache.render(repoTemplate, selectedRepo.name);
    document.getElementById('branchList').innerHTML = branchTemplateRendered;
}