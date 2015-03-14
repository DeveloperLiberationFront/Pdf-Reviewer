package edu.ncsu.dlf.model;

import java.io.IOException;

import com.mongodb.ReflectionDBObject;

import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class Review extends ReflectionDBObject {
    
    public PDFUser requester;
    public PDFUser reviewer;
    public Repo repo;
    public String paper;
    public String link;     //don't know what this is for
    
    public Review() {
        // For Mongo
    }
    
    public Review(PDFUser requester, PDFUser reviewer, Repo repo, String paper, String link) {
        this.requester = requester;
        this.reviewer = reviewer;
        this.paper = paper;
        this.link = link;
        this.repo = repo;
    }
    
    public Review(PDFUser requester, PDFUser repoOwner, PDFUser reviewer, String repoName, String paper, String link) {
        this.requester = requester;
        this.reviewer = reviewer;
        this.paper = paper;
        this.link = link;
        this.repo = new Repo(repoOwner.login, repoName);
    }

    public Review(String requesterLogin, String writerLogin, String reviewerLogin, String repo, String paper, String link, UserService userService) throws IOException {
        this(PDFUser.userFromLogin(requesterLogin, userService),
                PDFUser.userFromLogin(writerLogin, userService),
                PDFUser.userFromLogin(reviewerLogin, userService),
                repo, paper, link);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("requester", requester.toJSON());
        request.put("reviewer", reviewer.toJSON());
        request.put("repo", repo.toJSON());
        request.put("paper", paper);
        request.put("link", link);
        return request;
    }

    public PDFUser getRequester() {
        return requester;
    }

    @Override
    public String toString() {
        return "Review [requester=" + requester + ", reviewer=" + reviewer + ", repo=" + repo + ", paper=" + paper + ", link="
                + link + "]";
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

    public String getPaper() {
        return paper;
    }

    public void setPaper(String paper) {
        this.paper = paper;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    
}
