package edu.ncsu.dlf.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

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
    public List<Review> getPendingReviews(User user, UserService userService) {
        List<Review> retVal = new ArrayList<>();
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        DBCursor cursor = coll.find();
        try {
            while (cursor.hasNext()) {
                DBObject element = cursor.next();
                System.out.println(element);
                Object reviewer = element.get("Reviewer");
                System.out.println(reviewer);
                if (reviewer != null)
                    System.out.println(reviewer.getClass());
                if (reviewer instanceof Review && user.getLogin().equals(((DBObject) reviewer).get("Login"))) {
                    retVal.add((Review) reviewer);
                }
            }
        } finally {
           cursor.close();
        }
        return retVal;
    }

    @Override
    public List<Review> getPendingReviewRequests(User user, UserService userService) {
        List<Review> retVal = new ArrayList<>();
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        DBCursor cursor = coll.find();
        try {
            while (cursor.hasNext()) {
                DBObject element = cursor.next();
                System.out.println(element);
                Object requester = element.get("Requester");
                System.out.println(requester);
                if (requester != null)
                    System.out.println(requester.getClass());
                if (requester instanceof Review && user.getLogin().equals(((DBObject) requester).get("Login"))) {
                    retVal.add((Review) requester);
                }
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
    public void removeReviewFromDatastore(String reviewer, String writer, String repo) {
        // TODO Auto-generated method stub
        
    }

}
