package com.mykong;

import jakarta.servlet.http.HttpServlet;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.mykong.DownloadFile.*;


@Path("/upload")
public class UploadFile extends HttpServlet {
    public static final Logger LOGGER = (Logger) LoggerFactory.getLogger(UploadFile.class);
    @GET
    public String ping(){
        LOGGER.info("Received GET request for upload");
        return "Testing upload rout.";
    }

    @POST
    @Path("/movie")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("upload") InputStream is,
                               @FormDataParam("upload") FormDataContentDisposition formData,
                               @FormDataParam("user") String user,
                               @FormDataParam("pass") String pass,
                               @FormDataParam("thumbnail") InputStream isImg,
                               @FormDataParam("fileName") String fileName){
        LOGGER.info("Received POST Request on upload/file");
        if(is == null) return Response.status(400).build();
        String ext = getExt(formData.getFileName());
        if(ext.equals("")) return Response.status(415).entity("Extension not found/permitted, please rename your file as name.extension, and check if extensio is valid").build();
        LOGGER.info("Info:\nFile Name: {}\nFile Type{}", formData.getFileName(), ext);
        OpenDB();
        String storedPass = getUserPass(user);
        // Grabbing the stored password from user
        if(storedPass == null || validatePassword(pass, storedPass)){                                  // Check if the user exists, if yes, compare the passwords
            LOGGER.info("No user with the credentials name: {} ; pass: {} ; was found", user, pass);
            return Response.status(401).entity("No user was found, please check the provided credentials").build();
        }
        String ProjPath = System.getProperty("user.dir");
        int videoId = getNextId();
        String fileLocation, thumbnailPath, lowPath;
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            fileLocation = ProjPath + "\\src\\main\\resources\\videos\\High\\" + fileName + "_" + videoId + ext;
            thumbnailPath = ProjPath + "\\src\\main\\resources\\videos\\Thumbnail\\" + videoId + ".png";
            lowPath = ProjPath + "\\src\\main\\resources\\videos\\Low\\" + fileName + "_360_" + videoId + ext;
        } else{
            fileLocation = ProjPath + "/src/main/resources/videos/High/" + fileName + "_" + videoId + ext;
            thumbnailPath = ProjPath + "/src/main/resources/videos/Thumbnail/" + videoId + ".png";
            lowPath = ProjPath + "/src/main/resources/videos/Low/" + fileName + "_360_" + videoId + ext;
        }

        try {
            LOGGER.info("Trying to save file info into database...");
            // Adding Video to database, requesting userid and creating entry into the upload table
            String bucketHigh = "{link to google bucket}/"+ fileName + "_"+ videoId + ".mp4";
            String bucketLow = "{link to google bucket}/"+ fileName + "_360_"+ videoId + ".mp4";

            boolean insert = insertFile(fileLocation, user, fileName, thumbnailPath, lowPath, bucketHigh, bucketLow);
            if(!insert){
                LOGGER.info("An error has occurred when trying to add the file to the database");
                removeEntry(fileLocation, user);
                return Response.status(500).entity("An error has occurred when trying to add the file to the database").build();
            }
            saveFile(is, fileLocation, user);
            ////////////////// SAVING FILE TO BUCKET ///////////////////////////////
            BucketThread bucketThread = new BucketThread(fileLocation, false, fileName);
            bucketThread.start();
            File check = new File(fileLocation);
            String result;
            if(check.exists()) {
                LOGGER.info("Successfully File Uploaded on the path : {}", fileLocation);
            }else{
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to Save the file in the hard disk").build();
            }
            is.close(); // Fechar buffer de input para o ficheiro original
            if(isImg != null){
                LOGGER.info("An Thumbnail was provided");
                saveFile(isImg, thumbnailPath, user);
                isImg.close();
            } else{
               LOGGER.info("No thumbnail detected. Generating one...");
               GenerateThumb generateThumb = new GenerateThumb(fileLocation, thumbnailPath);
               generateThumb.start();
            }
            check = new File(thumbnailPath);
            if(check.exists()) {
                 LOGGER.info("Successfully File Uploaded on the path: {} ",  fileLocation);
            }else{
                LOGGER.info("The movie was uploaded but there was an error saving the thumbnail or the thumbnail is being generated right now");
            }
            LowRes lowRes = new LowRes(fileName, ext, ProjPath, videoId);
            lowRes.start();
            CloseDB();
            return Response.status(Response.Status.OK).entity("Everything went ok").build();
        } catch (IOException | SQLException e) {
            LOGGER.info("Error occured on upload: {}", e.getMessage());
            e.printStackTrace();
            OpenDB();
            removeEntry(fileLocation, user);
            try {
                CloseDB();
            } catch (SQLException ex) {
                LOGGER.info("Failed to close DB");
                throw new RuntimeException(ex);
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    private int getNextId(){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    SELECT AUTO_INCREMENT from information_schema.TABLES where TABLE_SCHEMA = "netflixpp" and TABLE_NAME= "videos";
                    """);
            ResultSet rs = stm.executeQuery();
            if(!rs.next()) return -1;                                      // Return -1 if no user was found

            return rs.getInt(1);                                // Else return User Id
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private String getExt(String fileName){
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf);
    }


    private void saveFile(InputStream is, String fileLocation, String user) throws FileNotFoundException {                     // Function to save the file into the hard disk
        LOGGER.info("Physically storing the video on {}", fileLocation);
        try {
            OutputStream os = new FileOutputStream(new File(fileLocation));
            byte[] buffer = new byte[256];
            int bytes = 0;
            while ((bytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytes);
            }
            LOGGER.info("Done");
            os.close();
            is.close();
        }catch (IOException e){
            removeEntry(fileLocation, user); // In case of error, remove entrys from Database
        }
    }

    private boolean insertFile(String fileLocation, String user, String fileName, String thumbnail, String lowPath, String bucketHigh, String bucketLow) throws SQLException {
        OpenDB();
        int userId = getUserId(user);                                       // Get UserId. (-1) if no user found
        int videoId = insertVideo(fileLocation, fileName, thumbnail, lowPath, bucketHigh, bucketLow);       // insert videos. (-1) if failed to do so
        CloseDB();
        if(userId != -1 && videoId != -1) {
            OpenDB();
            boolean upload = insertUpload(userId, videoId);                 // Insert upload into the uploads tables (false) if failed
            CloseDB();
            return upload;
        }
        return false;
    }

    private int getUserId(String user){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Select id from users where name = ?
                    """);
            stm.setString(1, user);
            ResultSet rs = stm.executeQuery();
            if(!rs.next()) return -1;                                      // Return -1 if no user was found

            return rs.getInt(1);                                // Else return User Id
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getVideoId(String path){
        LOGGER.info("Getting videoId");
        try {
            PreparedStatement stm = connection.prepareStatement("""          
                    Select id from videos where path = ?
                    """);
            stm.setString(1, path);
            ResultSet r = stm.executeQuery();
            if(!r.next()) return -1;                            // Return -1 if no video was found

            return r.getInt(1);                      // Return videoId
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while getting videoId: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean insertUpload(int userId, int videoId){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Insert into uploads(videoId, userId) values(?, ?)
                    """);
            stm.setInt(2, userId);
            stm.setInt(1, videoId);
            int rs = stm.executeUpdate();
            if(rs > 0) return true;                                       // Check if Insert was successful
            else return false;
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while inserting Upload: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private int insertVideo(String path, String name, String thumbnail, String lowPath, String bucketHigh, String bucketLow){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Insert into videos(name, path, thumbnail, lowPath, bucketPath, bucketLowPath) values(?, ?, ?, ?, ?, ?)
                    """);
            stm.setString(1, name);
            stm.setString(2, path);
            stm.setString(3, thumbnail);
            stm.setString(4, lowPath);
            stm.setString(5, bucketHigh);
            stm.setString(6, bucketLow);
            int rs = stm.executeUpdate();                               // Insert Video in database
            if(rs > 0) {
                return getVideoId(path);                                // GetVideoId
            }
            else return -1;                                             // Return -1 if insert operation fails
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to insert video: {}" ,e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void removeEntry(String fileLocation, String user){
        int videoId = getVideoId(fileLocation);
        int userId = getUserId(user);
        if(videoId != -1 && userId != -1){
            removeUpload(videoId, userId);
            removeVideo(videoId);
        }
    }

    private void removeVideo(int videoId){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Delete from videos
                    where id = ?
                    """);
            stm.setInt(1, videoId);
            int rs = stm.executeUpdate();
            if(rs < 0) {
                LOGGER.info("No video was removed, most likely was never added in the first place");
            }
            else LOGGER.info("Video was successfully removed from the database");
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to delete the video: {}" ,e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void removeUpload(int videoId, int userId){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Delete from uploads
                    where videoId = ? and userId = ?
                    """);
            stm.setInt(1, videoId);
            stm.setInt(2, userId);
            int rs = stm.executeUpdate();
            if(rs < 0) {
                LOGGER.info("No upload was removed, most likely was never added in the first place");
            }
            else LOGGER.info("Upload was successfully removed from the database");
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to delete the upload: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }



}
