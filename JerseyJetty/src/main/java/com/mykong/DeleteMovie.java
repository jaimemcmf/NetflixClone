package com.mykong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static com.mykong.DownloadFile.*;
import static com.mykong.UploadFile.LOGGER;

@Path("/delete")
public class DeleteMovie {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteFile(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map;
        try {
            map = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.info("Error reading Values: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        String pass = map.get("pass");
        String user = map.get("user");
        int movieId = Integer.parseInt(map.get("movieId"));
        LOGGER.info("user: {} pass: {} movieId: {}", user, pass, movieId);
        OpenDB();
        String storedPass = getUserPass(user);
        LOGGER.info("User pass: {}", storedPass);
        if(storedPass == null || validatePassword(pass, storedPass)){
            return Response.status(401).entity("Credentials not found").build();
        }
        String MoviePath;
        MoviePath = getMoviePath(movieId, user);
        if(MoviePath == null) return Response.status(500).build();
        LOGGER.info("Getting movies... Path: {}", MoviePath);
        File f = new File(MoviePath);
        boolean del = f.delete(); // Delete High Res File
        if(f.exists() || !del) return Response.status(401).entity("Failed to delete the file").build();
        LOGGER.info("Deleted High Res file: {}", MoviePath);
        String ThumbnailPath;
        try {
            ThumbnailPath = getThumbnail(movieId);
            LOGGER.info("Thumbnail: {}", ThumbnailPath);
        } catch (SQLException e) {
            LOGGER.info("Error Getting Thumbnail: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        if(ThumbnailPath == null) return Response.status(401).entity("Failed to delete the file").build();
        f = new File(ThumbnailPath);
        del = f.delete(); // Delete Thumbnail
        // Deleting Low Res version
        String lowPath = getLowPath(movieId);
        f = new File(lowPath);
        if(f.exists()) del = f.delete();
        if(del) LOGGER.info("Low Res Movie was deleted");
        else LOGGER.info("Was not deleted");
        String fileName = getMovieName(movieId);
        LOGGER.info("fileName: {}", fileName);
        if(fileName != null) {
            BucketThread bucketThread = new BucketThread("", true, fileName + "_" + movieId + ".mp4");
            bucketThread.start();
            BucketThread bucketThread1 = new BucketThread("", true, fileName + "_360_" + movieId + ".mp4");
            bucketThread1.start();
        }
        removeEntry(user, movieId);
        LOGGER.info("All information related to: {} has been deleted, exiting...", movieId);
        try {
            CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Error closing database: {}" , e.getMessage());
            return Response.status(500).entity("An error occured while completing your request").build();
        }
        LOGGER.info("Returning...");
        return Response.status(Response.Status.OK).entity("Movie Deleted").build();
    }

    public String getMovieName(int movieId){
        PreparedStatement stm;
        try {
            stm = connection.prepareStatement("""          
                        Select name from videos where id = ?
                        """);
        stm.setInt(1, movieId);
        ResultSet r = stm.executeQuery();
        if(r.next()) return r.getString(1);
        return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMoviePath(int id, String user){
        try {
            PreparedStatement stm = connection.prepareStatement("""          
                    select path from videos where id in (Select videoId from uploads where videoId = ? and userId in (Select id from users where name = ?))
                    """);
            stm.setInt(1, id);
            stm.setString(2, user);
            ResultSet r = stm.executeQuery();
            if (r.next()) return r.getString(1);

        }catch(SQLException e){
            LOGGER.info("Error in getMoviePath: {}", e.getMessage());
            return null;
        }
        return null;
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
            LOGGER.info("Error getting users: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void removeEntry(String user, int videoId){
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

    public static String getThumbnail(int id) throws SQLException {
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Select thumbnail from videos where id = ?
                    """);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if(!rs.next()) return null;

            return rs.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLowPath(int movieId){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Select lowPath from videos where id = ?
                    """);
            stm.setInt(1, movieId);
            ResultSet rs = stm.executeQuery();
            if(!rs.next()) return null;

            return rs.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
