package edu.ncsu.dlf.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.media.MediaByteArraySource;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.ServiceException;

public class ImageUtils {
    
    /**
     * Uploads a photo to a hosting service and returns the publicly-accessable URI to be used
     * in the markdown of a new issue.
     * @param img
     * @return
     */
    public URI uploadPhoto(BufferedImage img) {
        // TODO
        return null;
    }
    
    public static void main(String[] args) throws IOException, ServiceException {
        PicasawebService photoService = new PicasawebService("ncsu-pdfreviewer-0.1");
        
        String username = System.getenv("PICASSA_USER");
        String password = System.getenv("PICASSA_PASSWORD");
        String pdfAlbumId = System.getenv("PICASSA_PDF_ALBUM_ID");
        photoService.setUserCredentials(username, password);
        
        
        URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+pdfAlbumId);

        BufferedImage image = ImageIO.read(new File("test.png"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ImageIO.write(image, "png", baos);
        
        MediaByteArraySource myMedia = new MediaByteArraySource(baos.toByteArray(), "image/png");

        PhotoEntry returnedPhoto = photoService.insert(feedUrl, PhotoEntry.class, myMedia);
        
        System.out.println(returnedPhoto);
        System.out.println(returnedPhoto.getId());
        System.out.println(returnedPhoto.getHtmlLink());
        System.out.println(returnedPhoto.getHtmlLink().getHref());
        System.out.println(returnedPhoto.getMediaContents());
        System.out.println(returnedPhoto.getMediaContents().get(0));
        System.out.println(returnedPhoto.getMediaContents().get(0).getUrl());   //this one works
        
    }

}
