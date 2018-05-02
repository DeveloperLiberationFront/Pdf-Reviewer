let params = new URLSearchParams(location.search.slice(1));
let access_token = params.getAll('access_token');

// this will run on load
window.onload = function() {
    // Grab the inline template and populate using the result of this get request (list of repositories)
    $.get('/repositories?access_token=' + access_token).done((repositories)=>{
        // all of repos
        let repoTemplateJSON = {repos:[]}

        // top 10 repos
        let repoTemplateJSON10 = {repos:[]}

        for (let i = 0; i < repositories.length; i++) {
            repoTemplateJSON.repos.push({id: 'repo'+i, name: repositories[i]});            
            if(i < 10)
                repoTemplateJSON10.repos.push({id: 'repo'+i, name: repositories[i]});
        }

        console.log(JSON.stringify(repoTemplateJSON))
        let repoTemplate = document.getElementById('repoTemplate').innerHTML;
        Mustache.parse(repoTemplate);
        let repoTemplateRendered = Mustache.render(repoTemplate, repoTemplateJSON10);
        document.getElementById('repoTemplateRendered').innerHTML = repoTemplateRendered;

        // make the repository active in the list of top 10 repos
        $('.mdl-tabs__tab').on('click', function() {
            $(this).addClass('is-active');
            var otherRepos = $(this).siblings().removeClass('is-active');
            //if there is something written on the search bar than remove it. 
            if($("#repoList").val().length != 0){
                $("#repoList").val('');
            }
            populateBranches(repoTemplateJSON, $(this).text());
        })

        // Populates the searchbar with repository names so that user can type and  the tool will autocomplete
        // console.log(repoTemplateJSON.repos.map(repo=>repo.name.repoName))
        var availableRepo = repoTemplateJSON.repos.map(repo=>repo.name.repoName);
        $( "#repoList" ).autocomplete({
            source: availableRepo,
            autoFocus: true,
            change: function(event, ui){
                event.preventDefault();
                //if the item on the search repository box is not a valid repository then clear the box and show message
                if(!ui.item && $("#repoList").val() != ''){
                    $("#repoList").val('');
                    alert("Please select a valid repository.");
                } else {
                    $('.mdl-tabs__tab').removeClass('is-active');
                    $('#branchList')[0].options.length = 0;
                    populateBranches(repoTemplateJSON, ui.item.value);
                }
                   
            }
        });
    });
  }

/**
 * Populates the branch dropdown based on the selected repo
 * @param {JSON} repoTemplateJSON JSON object that contains all repos and their branches
 * @param {String} repoName name of selected repo
 */
function populateBranches(repoTemplateJSON, repoName){
    let repoTemplate = document.getElementById('branchTemplate').innerHTML;
    let selectedRepo = repoTemplateJSON.repos.find((repos)=>repos.name.repoName === repoName)
    selectedRepo.name.branches.sort((a, b)=> {
        if(a == 'master') 
            return -1;
        else if(b=='master'){
            return 1;
        }})
    Mustache.parse(repoTemplate);
    let branchTemplateRendered = Mustache.render(repoTemplate, selectedRepo.name);
    document.getElementById('branchList').innerHTML = branchTemplateRendered;
}
