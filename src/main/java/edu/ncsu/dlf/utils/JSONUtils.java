package edu.ncsu.dlf.utils;

import java.util.List;

import edu.ncsu.dlf.model.Review;

import org.json.JSONArray;
import org.json.JSONException;

public final class JSONUtils {

    private JSONUtils() {}

    public static JSONArray toJSON(List<Review> list) throws JSONException {
        JSONArray jarr = new JSONArray();
        for(Review r: list) {
            jarr.put(r.toJSON());
        }
        return jarr;
    }
    
}
