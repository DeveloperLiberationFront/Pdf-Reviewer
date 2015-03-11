package edu.ncsu.dlf.model;

import java.io.IOException;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class PDFUser {
    
    public final String login;
    public final String email;
    public final String name;
    
    

    public PDFUser(String login, String email, String name) {
        this.login = login;
        this.email = email;
        this.name = name;
    }

    public static PDFUser userFromLogin(String login, UserService userService) throws IOException {
        User user = userService.getUser(login);
        return new PDFUser(login, user.getEmail(), user.getName());
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("login", login);
        json.put("email", email);
        json.put("name", name);

        return json;
    }

}
