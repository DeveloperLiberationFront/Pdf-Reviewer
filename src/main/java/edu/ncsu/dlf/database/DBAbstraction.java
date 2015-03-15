package edu.ncsu.dlf.database;

import java.util.List;

import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.Review;

import org.eclipse.egit.github.core.User;

public interface DBAbstraction {
    
    public List<Review> getReviewsWhereUserIsRequester(User user);

    public List<Review> getReviewsWhereUserIsReviewer(User user);

    public void addReviewToDatastore(Review newReview);

    public void removeReviewFromDatastore(String reviewerLogin, Repo repo);

    public Review findReview(String reviewerLogin, Repo repo);

}
