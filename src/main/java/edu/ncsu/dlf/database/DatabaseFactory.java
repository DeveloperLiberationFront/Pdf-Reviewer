package edu.ncsu.dlf.database;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;

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
        public List<Review> getReviewsWhereUserIsRequester(User user) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> getReviewsWhereUserIsReviewer(User user) {
            return Collections.emptyList();
        }

        @Override
        public void addReviewToDatastore(Review newReview) {
            //ignores it
        }

        @Override
        public void removeReviewFromDatastore(String reviewer, Repo repo) {
            //ignores it
        }

        @Override
        public Review findReview(String reviewerLogin, Repo repo) {
            return new Review();        //do nothing
        }
        
    }
}
