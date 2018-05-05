/**
 * 
 * @param input the pdf file.  
 */
function readURL(input) {
    // Check if the branch is empty or is there something on branch selection.
    // This makes sure that repository is selected before user selects a pdf to upload. 
    if ($('#branchList').find(":selected").text().length == 0) {
        alert("Please select a repository first.")
    } else {
        // Checks to see if the uploaded file is pdf or not. If not then show an error message. 
        if (!input.files[0].name.endsWith(".pdf")) {
            alert("Please upload pdf files only.")
        } else {
            //A valid pdf file uploaded will enable the upload button on the page. 
            enableUploadButton();

            if (input.files[0]) {

                var reader = new FileReader();
                // gets the name of pdf for remove  pdf button.     
                reader.onload = function (e) {
                    $('.pdf-upload-wrap').hide();

                    $('.file-upload-content').show();

                    $('.pdf-title').html(input.files[0].name);
                };
                reader.readAsDataURL(input.files[0]);
                // Takes repository, branch and pdf and sends it to back end. Once done it displays a success message. 
                displayMessage(input.files[0]);
            } else {
                // Removes the remove pdf button and replace it with the drag and drop container. 
                removeUpload();
            }
        }
    }
}
/**
 * Enables the upload button on the page. 
 */
function enableUploadButton() {
    $('#upload-button').prop("disabled", false);
}
/**
 * Dissables the upload button on the page. 
 */
function dissableUploadButton() {
    $('#upload-button').prop("disabled", true);
}
/**
 * Removes the uploaded pdf button and replace it with the drag and drop container. 
 */
function removeUpload() {
    $('.file-upload-input').replaceWith($('.file-upload-input').clone());
    $('.file-upload-content').hide();
    $('.pdf-upload-wrap').show();
    $("#uploadDiv").load(" #uploadDiv > *");
    dissableUploadButton();
}
$('.pdf-upload-wrap').bind('dragover', function () {
    $('.pdf-upload-wrap').addClass('pdf-dropping');
});
$('.pdf-upload-wrap').bind('dragleave', function () {
    $('.pdf-upload-wrap').removeClass('pdf-dropping');
});

/**
 * Takes the repository name, branch name, and uploaded pdf file. Send those information to backend. 
 * Once everything is processed the function creates a pop up message card with success, number of issues,
 * issues classified by types, and link where the pdf is archived.  
 * @param inputFile the uploaded pdf. 
 */
function displayMessage(inputFile) {
    'use strict';
    var dialogButton = document.querySelector('#upload-button');

    dialogButton.addEventListener('click', function () {
        var repoName = "";
        if ($("#repoList").val() != '' && $("#repoList").val().length != 0) {
            repoName = $("#repoList").val();
        } else if ($(".mdl-tabs__tab.is-active").text().length != 0) {
            repoName = $(".mdl-tabs__tab.is-active").text();
        } else {
            alert("Please select a repository.");
            return;
        }
        showLoading();
        var formData = new FormData();
        formData.append("file", inputFile);

        let params = new URLSearchParams(location.search.slice(1));
        let access_token = params.getAll('access_token');

        var postURL = "/fileupload?access_token=" + access_token;

        postURL += "&selectedRepository=" + escape(repoName);
        postURL += "&selectedBranch=" + escape($('#branchList').find(":selected").text());

        let t0 = performance.now();

        $.ajax({
            type: "POST",
            url: postURL,
            processData: false,
            contentType: false,
            data: formData
        })
            .done(function (data) {
                hideLoading();
                showDialog({
                    positive: {
                        title: 'Close'
                    },
                    cancelable: false
                }, data);
            });
    });
};

/* library */
/* Source:https://github.com/oRRs/mdl-jquery-modal-dialog */
/**
 * Displays the loading screen. 
 */
function showLoading() {
    // remove existing loaders
    $('.loading-container').remove();
    $('<div id="orrsLoader" class="loading-container"><div><div class="mdl-spinner mdl-js-spinner is-active"></div></div></div>').appendTo("body");

    componentHandler.upgradeElements($('.mdl-spinner').get());
    setTimeout(function () {
        $('#orrsLoader').css({ opacity: 1 });
    }, 1);
}
/**
 * Removes the loading screen. 
 */
function hideLoading() {
    $('#orrsLoader').css({ opacity: 0 });
    setTimeout(function () {
        $('#orrsLoader').remove();
    }, 400);
}
/**
 * Creates and displays the pop up message card once the program is done processing pdf. 
 * @param options options for the pop up message card
 * @param data data from the backend. 
 */
function showDialog(options, data) {
    options = $.extend({
        id: 'dialog',
        positive: false,
        cancelable: true,
        contentStyle: null,
        onLoaded: false
    }, options);

    // remove existing dialogs
    $('.dialog-container').remove();
    $(document).unbind("keyup.dialog");

    $('<div id="' + options.id + '" class="dialog-container"><div class="mdl-card hello mdl-shadow--16dp"></div></div>').appendTo("body");
    var dialog = $('#dialog');
    var content = dialog.find('.mdl-card');
    if (options.contentStyle != null) content.css(options.contentStyle);
    $(content).html(data);
    if (options.positive) {
        var buttonBar = $('<div class="mdl-card__actions dialog-button-bar"></div>');
        if (options.positive) {
            options.positive = $.extend({
                id: 'positive',
                title: 'OK',
                onClick: function () {
                    return false;
                }
            }, options.positive);
            var posButton = $('<button class="mdl-button mdl-button--colored mdl-js-button mdl-js-ripple-effect" onClick="window.location.reload();" id="' + options.positive.id + '">' + options.positive.title + '</button>');
            posButton.click(function (e) {
                e.preventDefault();
                if (!options.positive.onClick(e))
                    hideDialog(dialog)
            });
            posButton.appendTo(buttonBar);
        }
        buttonBar.appendTo(content);
    }
    componentHandler.upgradeDom();
    if (options.cancelable) {
        dialog.click(function () {
            hideDialog(dialog);
        });
        $(document).bind("keyup.dialog", function (e) {
            if (e.which == 27)
                hideDialog(dialog);
        });
        content.click(function (e) {
            e.stopPropagation();
        });
    }
    setTimeout(function () {
        dialog.css({ opacity: 1 });
        if (options.onLoaded)
            options.onLoaded();
    }, 1);
}
/**
 * Removes the pop up message card. 
 */
function hideDialog(dialog) {
    $(document).unbind("keyup.dialog");
    dialog.css({ opacity: 0 });
    setTimeout(function () {
        dialog.remove();
    }, 400);
}
