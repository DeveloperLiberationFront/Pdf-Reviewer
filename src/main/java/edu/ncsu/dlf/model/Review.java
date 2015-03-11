package edu.ncsu.dlf.model;

import java.io.IOException;

import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class Review {
    
    public final PDFUser requester;
    public final PDFUser writer;
    public final PDFUser reviewer;
    public final String repo;
    public final String paper;
    public final String link;
    
    
    
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

}
