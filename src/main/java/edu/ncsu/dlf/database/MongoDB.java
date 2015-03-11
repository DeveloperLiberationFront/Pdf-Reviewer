package edu.ncsu.dlf.database;

import java.util.Collections;
import java.util.List;

import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

public class MongoDB implements DBAbstraction {

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
