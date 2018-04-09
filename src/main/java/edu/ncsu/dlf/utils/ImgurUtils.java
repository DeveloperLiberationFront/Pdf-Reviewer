package edu.ncsu.dlf.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import edu.ncsu.dlf.utils.HttpUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;




public class ImgurUtils {
    private static ImgurUtils singleton = new ImgurUtils();

    private final static String clientID = System.getenv("IMGUR_ID");
    private final static String clientSecret = System.getenv("IMGUR_SECRET");
    private final static String albumID = System.getenv("IMGUR_ALBUM_ID");

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private ImgurUtils() {

    }

    public static String uploadImage(BufferedImage image) {
        String imageURL = "";
        synchronized (singleton) {
            HttpPost request = null;
            try {
                URIBuilder builder = new URIBuilder("https://api.imgur.com/3/image");

                singleton.baos.reset();
                ImageIO.write(image, "png", singleton.baos);
                builder.addParameter("image", Base64.getEncoder().encodeToString(singleton.baos.toByteArray()));
                builder.addParameter("type", "base64");
                builder.addParameter("album", albumID);
                
                request = new HttpPost(builder.build());
                request.setHeader("accept", "application/json");
                request.setHeader("Authorization", "Client-ID " + clientID);
    
                HttpClient client = HttpClients.createDefault();
                HttpResponse authResponse = client.execute(request);

                JSONObject responseJSON = new JSONObject(HttpUtils.getResponseBody(authResponse));
                JSONObject dataJSON = new JSONObject(responseJSON.get("data").toString());

                imageURL = dataJSON.get("link").toString();
            } catch (URISyntaxException | JSONException | IOException e) {
                e.printStackTrace();
            }  finally{
                if (request != null) {
                    request.releaseConnection();
                }
            }
        }
        return imageURL;
    }
}