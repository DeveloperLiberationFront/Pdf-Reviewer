package edu.ncsu.dlf.model;

import com.mongodb.ReflectionDBObject;

import org.json.JSONException;
import org.json.JSONObject;

public class Repo extends ReflectionDBObject{
    public String repoOwner;
    public String repoName;
    
    public Repo() {
        // For mongodb
    }
    
    public String getRepoOwner() {
        return repoOwner;
    }

    public void setRepoOwner(String repoOwner) {
        this.repoOwner = repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public Repo(String repoOwner, String repoName) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("repoOwner", repoOwner);
        json.put("repoName", repoName);

        return json;
    }

}
