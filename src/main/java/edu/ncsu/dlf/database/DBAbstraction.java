package edu.ncsu.dlf.database;

import java.util.List;

import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

public interface DBAbstraction {
    
    public List<Review> getReviewsWhereUserIsRequester(User user);

    public List<Review> getReviewsWhereUserIsReviewer(User user);

    public void addReviewToDatastore(Review newReview);

    public void removeReviewFromDatastore(String reviewer, String writer, String repo);

}
