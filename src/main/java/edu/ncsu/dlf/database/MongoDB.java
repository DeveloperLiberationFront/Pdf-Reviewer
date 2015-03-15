package edu.ncsu.dlf.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;

public class MongoDB implements DBAbstraction {

    private static final String DB_NAME = "pdfreview";
    private MongoClient mongoClient;

    public MongoDB() throws IOException {
        String portNumber = System.getenv("OPENSHIFT_MONGODB_DB_PORT");
        ServerAddress address = new ServerAddress(System.getenv("OPENSHIFT_MONGODB_DB_HOST"),
                Integer.parseInt(portNumber));
        String user = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
        String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
        
        MongoCredential credential = MongoCredential.createCredential(user, DB_NAME, password.toCharArray());
        this.mongoClient = new MongoClient(address, Arrays.asList(credential));
    }
    
    @Override
    public List<Review> getReviewsWhereUserIsRequester(User user) {
        return findRequests(user, "Requester");
    }

    @Override
    public List<Review> getReviewsWhereUserIsReviewer(User user) {
        return findRequests(user, "Reviewer");
    }

    private List<Review> findRequests(User userToLookFor, String roleOfUser) {
        List<Review> retVal = new ArrayList<>();
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        coll.setObjectClass(Review.class);
        BasicDBObject query = new BasicDBObject(roleOfUser +".Login", userToLookFor.getLogin());
        DBCursor cursor = coll.find(query);
        try {
            while (cursor.hasNext()) {
                DBObject element = cursor.next();
                retVal.add((Review) element);
            }
        } finally {
           cursor.close();
        }
        return retVal;
    }

    @Override
    public void addReviewToDatastore(Review newReview) {
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        coll.save(newReview);
    }

    @Override
    public void removeReviewFromDatastore(String reviewer, Repo repo) {
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        BasicDBObject query = new BasicDBObject("Reviewer.Login", reviewer).
                append("Repo.RepoOwner", repo.repoOwner).
                append("Repo.RepoName", repo.repoName);
        WriteResult result = coll.remove(query);
        System.out.println(result.getN() +" documents removed");
    }

    @Override
    public Review findReview(String reviewerLogin, Repo repo) {
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        coll.setObjectClass(Review.class);
        BasicDBObject query = new BasicDBObject("Reviewer.Login", reviewerLogin).
                append("Repo.RepoOwner", repo.repoOwner).
                append("Repo.RepoName", repo.repoName);
        return (Review) coll.findOne(query);
    }

}
