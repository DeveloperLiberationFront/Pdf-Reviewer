let params = new URLSearchParams(location.search.slice(1));
let access_token = params.getAll('access_token');


window.onload = function() {
    //Grab the inline template
    $.get('http://localhost:9090/repositories?access_token=' + access_token).done((repositories)=>{
        let repoTemplateJSON = {repos:[]}
        for (let i = 1; i <= repositories.length; i++) {
            repoTemplateJSON.repos.push({id: 'repo'+i, name: repositories[i]});
        }

        console.log(JSON.stringify(repoTemplateJSON))
        let repoTemplate = document.getElementById('repoTemplate').innerHTML;
        Mustache.parse(repoTemplate);
        let repoTemplateRendered = Mustache.render(repoTemplate, repoTemplateJSON);
        document.getElementById('repoTemplateRendered').innerHTML = repoTemplateRendered;
    });
  }