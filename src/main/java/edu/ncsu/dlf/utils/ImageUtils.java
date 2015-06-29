package edu.ncsu.dlf.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.media.MediaByteArraySource;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.ServiceException;

public class ImageUtils {

    private PicasawebService photoService;

    private URL pdfAlbumUrl;

    private static ImageUtils singleton = new ImageUtils();

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private ImageUtils() {
        photoService = new PicasawebService("ncsu-pdfreviewer-0.1");

        String clientID = System.getenv("PICASSA_CLIENT_ID");
        String clientSecret = System.getenv("PICASSA_CLIENT_SECRET");
        String refreshToken = System.getenv("PICASSA_REFRESH_TOKEN");

        String pdfAlbumId = System.getenv("PICASSA_PDF_ALBUM_ID");

        try {
            pdfAlbumUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/" + pdfAlbumId);

            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            //get a current token
            GoogleRefreshTokenRequest req = new GoogleRefreshTokenRequest(transport, jsonFactory, refreshToken, clientID, clientSecret);
            GoogleTokenResponse res = req.execute();
            String accessToken = res.getAccessToken();
            
            //build a refreshable credential
            Credential credential = new GoogleCredential.Builder()
            .setClientSecrets(clientID, clientSecret)
            .setJsonFactory(jsonFactory)
            .setTransport(transport)
            .build();
            
            //Load OAuth2 credential
            credential.setAccessToken(accessToken);
            credential.setRefreshToken(refreshToken);
            photoService.setOAuth2Credentials(credential);
        } catch (IOException e) {
            e.printStackTrace();
            photoService = null;
        }
    }

    /**
     * Uploads a photo to a hosting service and returns the publicly-accessible URI to be used in the markdown of a new issue.
     * 
     * @param img
     * @return
     */
    public static String uploadPhoto(BufferedImage image) throws IOException {
        if (image == null) {
            throw new IOException("image was null", new NullPointerException());
        }
        String publicLinkToPhoto = null;
        synchronized (singleton) {
            if (singleton.photoService == null) {
                throw new IOException("Could not authenticate with Picassa");
            }

            singleton.baos.reset();
            ImageIO.write(image, "png", singleton.baos);
            MediaByteArraySource myMedia = new MediaByteArraySource(singleton.baos.toByteArray(), "image/png");

            PhotoEntry returnedPhoto;
            try {
                returnedPhoto = singleton.photoService.insert(singleton.pdfAlbumUrl, PhotoEntry.class, myMedia);
            } catch (ServiceException e) {
                e.printStackTrace();
                throw new IOException("Problem uploading photo", e);
            }

            publicLinkToPhoto = returnedPhoto.getMediaThumbnails().get(0).getUrl();

            // scale it up to 800 px, which is the largest we can hotlink to
            publicLinkToPhoto = publicLinkToPhoto.replace("/s72/", "/s800/").replace("/s144/", "/s800/").replace("/s288/", "/s800/");
        }
        return publicLinkToPhoto;
    }

    //Can be used to regenerate a refresh token
    private static String getRefreshToken() throws IOException {
        String client_id = System.getenv("PICASSA_CLIENT_ID");
        String client_secret = System.getenv("PICASSA_CLIENT_SECRET");
        
        // Adapted from http://stackoverflow.com/a/14499390/1447621
        String redirect_uri = "http://localhost";
        String scope = "http://picasaweb.google.com/data/";
        List<String> scopes;
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        scopes = new LinkedList<String>();
        scopes.add(scope);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(transport, jsonFactory, client_id, client_secret, scopes).build();
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        url.setRedirectUri(redirect_uri);
        url.setApprovalPrompt("force");
        url.setAccessType("offline");
        String authorize_url = url.build();
       
        // paste into browser to get code
        System.out.println("Put this url into your browser and paste in the access token:");
        System.out.println(authorize_url);
        
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        scanner.close();

        flow = new GoogleAuthorizationCodeFlow.Builder(transport, jsonFactory, client_id, client_secret, scopes).build();
        GoogleTokenResponse res = flow.newTokenRequest(code).setRedirectUri(redirect_uri).execute();
        String refreshToken = res.getRefreshToken();
        String accessToken = res.getAccessToken();

        System.out.println("refresh:");
        System.out.println(refreshToken);
        System.out.println("access:");
        System.out.println(accessToken);
        return refreshToken;
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException, ServiceException {
      //  getRefreshToken();
        BufferedImage image = ImageIO.read(new File("test.jpg"));

        System.out.println(uploadPhoto(image));

    }

}
