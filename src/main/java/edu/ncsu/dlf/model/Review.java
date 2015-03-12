package edu.ncsu.dlf.model;

import java.io.IOException;

import com.mongodb.ReflectionDBObject;

import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class Review extends ReflectionDBObject {
    
    public PDFUser requester;
    public PDFUser writer;
    public PDFUser reviewer;
    public String repo;
    public String paper;
    public String link;
    
    public Review() {
        // For Mongo
    }
    
    public Review(PDFUser requester, PDFUser writer, PDFUser reviewer, String repo, String paper, String link) {
        this.requester = requester;
        this.writer = writer;
        this.reviewer = reviewer;
        this.repo = repo;
        this.paper = paper;
        this.link = link;
    }

    public Review(String requesterLogin, String writerLogin, String reviewerLogin, String repo, String paper, String link, UserService userService) throws IOException {
        this(PDFUser.userFromLogin(requesterLogin, userService),
                PDFUser.userFromLogin(requesterLogin, userService),
                PDFUser.userFromLogin(requesterLogin, userService),
                repo, paper, link);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("requester", requester.toJSON());
        request.put("writer", writer.toJSON());
        request.put("reviewer", reviewer.toJSON());
        request.put("repo", repo);
        request.put("paper", paper);
        request.put("link", link);
        return request;
    }

    public PDFUser getRequester() {
        return requester;
    }

    public void setRequester(PDFUser requester) {
        this.requester = requester;
    }

    public PDFUser getWriter() {
        return writer;
    }

    public void setWriter(PDFUser writer) {
        this.writer = writer;
    }

    public PDFUser getReviewer() {
        return reviewer;
    }

    public void setReviewer(PDFUser reviewer) {
        this.reviewer = reviewer;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
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
