package edu.ncsu.dlf.utils;

import edu.ncsu.dlf.model.Repo;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.CannedAccessControlList;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;




public class S3Utils {
    private static S3Utils singleton = new S3Utils();

    private final static String bucketName = System.getenv("S3_BUCKET_NAME");;
    private final static String region = System.getenv("S3_BUCKET_REGION");

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonS3/S3Sample.java
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html
     */
	
	public static String uploadImage(BufferedImage image, Repo repo, int commentIndex) {
        String imageURL = "";
        synchronized(singleton) {
            try {
                AWSCredentials credentials = null;
                try {
                    credentials = new ProfileCredentialsProvider().getCredentials();
                } catch (Exception e) {
                    throw new AmazonClientException(
                            "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                            e);
                }

                AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();

                singleton.baos.reset();
                ImageIO.write(image, "png", singleton.baos);

                byte [] imageData = singleton.baos.toByteArray();
                InputStream imageStream = new ByteArrayInputStream(imageData);

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(imageData.length);
                metadata.setContentType("image/png");

                String systemTime = Long.toString(System.currentTimeMillis());
                String fileName = String.format("%s-%d-%s.png", 
                                                repo.getRepoOwner(), commentIndex, systemTime);

                String filePath = repo.getRepoName() + "/" + fileName;

                s3client.putObject(
                    new PutObjectRequest(bucketName, filePath, imageStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead)
                );

                //https://mybucket.s3.amazonaws.com/myfolder/afile.jpg
                imageURL = "https://" + bucketName + ".s3.amazonaws.com/" + filePath;
            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageURL;
    }

}