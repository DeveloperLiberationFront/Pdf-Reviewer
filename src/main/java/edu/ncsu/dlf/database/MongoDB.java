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

import edu.ncsu.dlf.model.PDFUser;
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
        return findRequests(user, "Reviewer");
    }

    @Override
    public List<Review> getPendingReviewRequests(User user, UserService userService) {
        return findRequests(user, "Requester");
    }

    private List<Review> findRequests(User userToLookFor, String whichUser) {
        List<Review> retVal = new ArrayList<>();
        DB db = mongoClient.getDB(DB_NAME);
        DBCollection coll = db.getCollection(DB_NAME);
        coll.setObjectClass(Review.class);
        DBCursor cursor = coll.find();
        try {
            while (cursor.hasNext()) {
                DBObject element = cursor.next();
                Object user = element.get(whichUser);
                if (element instanceof Review && user instanceof PDFUser &&
                        userToLookFor.getLogin().equals(((PDFUser) user).getLogin())) {
                    retVal.add((Review) element);
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
