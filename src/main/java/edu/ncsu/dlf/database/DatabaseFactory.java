package edu.ncsu.dlf.database;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

public class DatabaseFactory {
    
    private DatabaseFactory() { }

    public static DBAbstraction getDatabase() {
        try {
            return new MongoDB();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DummyDatabase();
    }

    private static class DummyDatabase implements DBAbstraction {

        @Override
        public List<Review> getPendingReviews(User user, UserService userService) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> getPendingReviewRequests(User user, UserService userService) {
            return Collections.emptyList();
        }

        @Override
        public void addReviewToDatastore(Review newReview) {
            //ignores it
        }

        @Override
        public void removeReviewFromDatastore(String reviewer, String writer, String repo) {
            //ignores it
        }
        
    }
}
