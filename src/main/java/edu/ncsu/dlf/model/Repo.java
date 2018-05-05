package edu.ncsu.dlf.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a GitHub repository
 */
public class Repo {
    public String repoOwner;
    public String repoName;
    
    /**
     * Construct a new Repo object given the owner of the repository and 
     * the name of the repository
     * @param repoOwner Owner (not collaborator) of the repository
     * @param repoName Name of the repository
     */
    public Repo(String repoOwner, String repoName) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
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

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("repoOwner", repoOwner);
        json.put("repoName", repoName);

        return json;
    }

}
