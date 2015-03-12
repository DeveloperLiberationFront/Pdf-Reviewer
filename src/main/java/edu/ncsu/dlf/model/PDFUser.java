package edu.ncsu.dlf.model;

import java.io.IOException;

import com.mongodb.ReflectionDBObject;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class PDFUser extends ReflectionDBObject {
    
    public String login;
    public String email;
    public String name;
    
    public PDFUser() {
        // For MongoDB
    }

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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PDFUser [login=" + login + ", email=" + email + ", name=" + name + "]";
    }

    
    
}
