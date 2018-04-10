package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;


import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.PdfComment;
import edu.ncsu.dlf.model.PdfComment.Tag;

import edu.ncsu.dlf.utils.ImgurUtils;


//TODO: Should I put this back in FileUpload Servlet and make it static?
final class UploadIssuesRunnable implements Runnable {
    private String accessToken;
    private List<PdfComment> comments;
    private int issueCount;
    private Repo repo;
    private List<String> customLabelStrings;

    private volatile int commentsToIssues = 0;

    public void setter(List<PdfComment> comments, String accessToken, Repo repo, int issueCount, List<String> customLabels) {
        this.comments = comments;
        this.accessToken = accessToken;
        this.repo = repo;
        this.issueCount = issueCount;
        this.customLabelStrings = customLabels;
    }

    @Override
    public void run() {
            try {
                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(accessToken);

                List<Label> customLabels = createCustomLabels(client);

                createIssues(client, customLabels);
            } catch(IOException e) {
                e.printStackTrace();
                System.err.println("Error processing Pdf.");
            }
    }

    public void createIssues(GitHubClient client, List<Label> customLabels) throws IOException {
        int count = 0;
        for(PdfComment comment : comments) {
            System.out.println(comment);
            if(count == 30) {
                try {
                    count = 0;
                    Thread.sleep(60000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            createOrUpdateIssue(client, repo, comment, customLabels);
            count++;
            commentsToIssues++;
        }
    }

    public void createOrUpdateIssue(GitHubClient client, Repo repo, PdfComment comment, List<Label> customLabels) throws IOException {
        IssueService issueService = new IssueService(client);

        // If the issue does not already exist
        if(comment.getIssueNumber() > issueCount) {
            createIssue(repo, comment, issueService, customLabels);
        }
        // If the issue already exists, update it
        else {
            updateIssue(repo, comment, issueService);
        }
    }

    private void createIssue(Repo repo, PdfComment comment, IssueService issueService, List<Label> customLabels) throws IOException {
        Issue issue = new Issue();
        issue.setTitle(comment.getTitle());

        String body = comment.getComment();
        try {
            //TODO: Implement image upload
            String imageURL = ImgurUtils.uploadImage(comment.getImage());
            body = String.format("![snippet](%s)%n%n%s", imageURL, body);
        } catch (Exception e) { //TODO: Should be IO Exception
            e.printStackTrace();
            // could not upload image, but carry on anyway
        }

        String pageReference = "Page Number: " + comment.getPageNumber();
        body += "\n\n" + pageReference;

        issue.setBody(body);
        List<Label> newLabels = new ArrayList<>(customLabels);
        //add tags to labels
        for(Tag tag : comment.getTags()) {
            Label label = new Label();
            label.setName(tag.name());     // these tags are, by default, the normal grey color.
            newLabels.add(label);             // User can change these to the severity whey want
        }

        issue.setLabels(newLabels);
        //creates an issue remotely
        issue = issueService.createIssue(repo.repoOwner, repo.repoName, issue);

        System.out.println("created issue #" + issue.getNumber());
        comment.setIssueNumber(issue.getNumber());
    }

    private void updateIssue(Repo repo, PdfComment comment, IssueService issueService)
            throws IOException {
        System.out.println("Looking for "+repo.repoOwner+'/'+repo.repoName);
        Issue issue = issueService.getIssue(repo.repoOwner, repo.repoName, comment.getIssueNumber());
        String issueText = comment.getComment();
        if(!issue.getBody().equals(issueText)) {
            // makes a comment if the text has changed
            issueService.createComment(repo.repoOwner, repo.repoName, comment.getIssueNumber(), issueText);
        }

        List<Label> existingLabels = issue.getLabels();
        List<Label> labels = new ArrayList<>();
        for(Tag tag : comment.getTags()) {
            Label l = new Label();
            l.setName(tag.name());
            labels.add(l);
        }

        boolean shouldUpdateLabels = labels.size() != existingLabels.size();
        if(!shouldUpdateLabels) {
            for(Label l1 : labels) {
                shouldUpdateLabels = true;
                for(Label l2 : existingLabels) {
                    if(l1.getName().equals(l2.getName())) {
                        shouldUpdateLabels = false;
                        break;
                    }
                }
                if(shouldUpdateLabels)
                    break;
            }
        }

        if(shouldUpdateLabels) {
            issue.setLabels(labels);
            issueService.editIssue(repo.repoOwner, repo.repoName, issue);
        }
    }

    private List<Label> createCustomLabels(GitHubClient client) {
        List<Label> labels = new ArrayList<>();
        LabelService labelService = new LabelService(client);
        for (String customLabel : customLabelStrings) {
            Label newLabel = new Label().setColor(randomColor()).setName(customLabel);
            try {
                Label alreadyExistingLabel = labelService.getLabel(repo.repoOwner, repo.repoName, customLabel);
                labels.add(alreadyExistingLabel);
            } catch(IOException e) {
                System.out.println("new label " +customLabel + " not found.  Going to create it");
                try {
                    newLabel = labelService.createLabel(repo.repoOwner, repo.repoName, newLabel);
                } catch (IOException e1) {
                    e1.printStackTrace();   //this is a bigger problem
                }
                labels.add(newLabel);
            }
        }
        return labels;
    }
    
    private String randomColor() {
        StringBuilder sb = new StringBuilder(6);
        Random r = new Random();
        for(int i = 0; i< 6; i++) {
            sb.append("0123456789abcdef".charAt(r.nextInt(16)));
        }
        return sb.toString();
    }

    public int getCommentsToIssues() {
        return this.commentsToIssues;
    }

}