package edu.ncsu.dlf.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.mongodb.ReflectionDBObject;

import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class Review extends ReflectionDBObject {
    
    public PDFUser requester;
    public PDFUser reviewer;
    public Repo repo;
    public String pathToPaperInRepo;
    public String linkToReviewPaper;
    public List<String> customLabels = Collections.emptyList(); 
    
    public Review() {
        // For Mongo
    }
    
    public Review(PDFUser requester, PDFUser reviewer, Repo repo, String pathToPaperInRepo, String downloadPaperLink) {
        this.requester = requester;
        this.reviewer = reviewer;
        this.pathToPaperInRepo = pathToPaperInRepo;
        this.linkToReviewPaper = downloadPaperLink;
        this.repo = repo;
    }
    
    public Review(PDFUser requester, PDFUser repoOwner, PDFUser reviewer, String repoName, String pathToPaperInRepo, String downloadPaperLink) {
        this.requester = requester;
        this.reviewer = reviewer;
        this.pathToPaperInRepo = pathToPaperInRepo;
        this.linkToReviewPaper = downloadPaperLink;
        this.repo = new Repo(repoOwner.login, repoName);
    }

    public Review(String requesterLogin, String writerLogin, String reviewerLogin, String repo, String pathToPaperInRepo, String downloadPaperLink, UserService userService) throws IOException {
        this(PDFUser.userFromLogin(requesterLogin, userService),
                PDFUser.userFromLogin(writerLogin, userService),
                PDFUser.userFromLogin(reviewerLogin, userService),
                repo, pathToPaperInRepo, downloadPaperLink);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("requester", requester.toJSON());
        request.put("reviewer", reviewer.toJSON());
        request.put("repo", repo.toJSON());
        request.put("paper", pathToPaperInRepo);
        request.put("link", linkToReviewPaper);
        return request;
    }

    public PDFUser getRequester() {
        return requester;
    }

    @Override
    public String toString() {
        return "Review [requester=" + requester + ", reviewer=" + reviewer + ", repo=" + repo + ", paper=" + pathToPaperInRepo + ", link="
                + linkToReviewPaper + "]";
    }

    public void setRequester(PDFUser requester) {
        this.requester = requester;
    }

    public PDFUser getReviewer() {
        return reviewer;
    }

    public void setReviewer(PDFUser reviewer) {
        this.reviewer = reviewer;
    }

    public Repo getRepo() {
        return repo;
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }
    
    public String getPathToPaperInRepo() {
        return pathToPaperInRepo;
    }

    public void setPathToPaperInRepo(String pathToPaperInRepo) {
        this.pathToPaperInRepo = pathToPaperInRepo;
    }

    public String getLinkToReviewPaper() {
        return linkToReviewPaper;
    }

    public void setLinkToReviewPaper(String linkToReviewPaper) {
        this.linkToReviewPaper = linkToReviewPaper;
    }

    public void setCustomLabels(List<String> parsedCustomLabels) {
        this.customLabels = parsedCustomLabels;
    }

    public List<String> getCustomLabels() {
        return customLabels;
    }
    
}
