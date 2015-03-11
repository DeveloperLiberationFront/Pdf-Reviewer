package edu.ncsu.dlf.database;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mongodb.DB;
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

        DB db = mongoClient.getDB(DB_NAME);
        System.out.println("DB checked out: "+db.getName());
    }
    
    @Override
    public List<Review> getPendingReviews(User user, UserService userService) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public List<Review> getPendingReviewRequests(User user, UserService userService) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public void addReviewToDatastore(Review newReview) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeReviewFromDatastore(String reviewer, String writer, String repo) {
        // TODO Auto-generated method stub
        
    }

}
