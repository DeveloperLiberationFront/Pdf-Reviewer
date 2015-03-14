package edu.ncsu.dlf.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.media.MediaByteArraySource;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class ImageUtils {
    
    private PicasawebService photoService;
    private URL pdfAlbumUrl;
    private static ImageUtils singleton = new ImageUtils();
    
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    private ImageUtils() {
        photoService = new PicasawebService("ncsu-pdfreviewer-0.1");
        
        String username = System.getenv("PICASSA_USER");
        String password = System.getenv("PICASSA_PASSWORD");
        String pdfAlbumId = System.getenv("PICASSA_PDF_ALBUM_ID");
        
        try {
            pdfAlbumUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+pdfAlbumId);
            photoService.setUserCredentials(username, password);
        } catch (AuthenticationException | MalformedURLException e) {
            e.printStackTrace();
            photoService = null;
        } 
    }
    
    /**
     * Uploads a photo to a hosting service and returns the publicly-accessible URI to be used
     * in the markdown of a new issue.
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
            
            //scale it up to 800 px, which is the largest we can hotlink to
            publicLinkToPhoto = publicLinkToPhoto.replace("/s72/", "/s800/").replace("/s144/", "/s800/").replace("/s288/", "/s800/");
        }
        return publicLinkToPhoto;
    }
    
    
    @SuppressWarnings("unused")
    private static void main(String[] args) throws IOException, ServiceException {
        PicasawebService photoService = new PicasawebService("ncsu-pdfreviewer-0.1");
        
        String username = System.getenv("PICASSA_USER");
        String password = System.getenv("PICASSA_PASSWORD");
        String pdfAlbumId = System.getenv("PICASSA_PDF_ALBUM_ID");
        photoService.setUserCredentials(username, password);
        
        
        URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+pdfAlbumId);

        BufferedImage image = ImageIO.read(new File("test.jpg"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ImageIO.write(image, "png", baos);
        
        MediaByteArraySource myMedia = new MediaByteArraySource(baos.toByteArray(), "image/png");

        PhotoEntry returnedPhoto = photoService.insert(feedUrl, PhotoEntry.class, myMedia);
        
        System.out.println(returnedPhoto);
        
    }

}
