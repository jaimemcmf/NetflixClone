package com.mykong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mykong.DownloadFile.*;
import static com.mykong.UploadFile.LOGGER;

@Path("/Connect")
public class UserCon {

    @GET // Testing UserCon Rout
    public static String ping(){
        LOGGER.info("Get request to Connection rout");
        return "Testing Connection rout";
    }

    @Path("/signup")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static RespUser SignUp(String json) throws JsonProcessingException {
        RespUser resposta = new RespUser();
        resposta.setStatus("Failed");
        resposta.setError("none");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(json, Map.class);
        String pass = map.get("pass");
        String user = map.get("user");
        String confirmPass = map.get("confirmPass");
        if(!Objects.equals(pass, confirmPass) || Objects.equals(user, "") || Objects.equals(pass, "")){ // Comparing passwords to check if they are equal and not empty
            resposta.setError("Passwords do not match");
            return resposta;
        }
        DownloadFile.OpenDB();
        if(!checkUserName(user)) { // Check if the username is already being used
            try {
                DownloadFile.CloseDB();
            } catch (SQLException e) {
                LOGGER.info("An Error has occurred while closing the database: {}", e.getMessage());
                throw new RuntimeException(e);
            }
            resposta.setError("Username already in use");
            return resposta;
        }
        if(CreateUser(user, DownloadFile.passHash(pass))){
            resposta.setStatus("Sucesso");
            LOGGER.info("User created with info: user: {}, pass: {}", user, pass);
        }
        try {
            DownloadFile.CloseDB();
        } catch (SQLException e) {
            LOGGER.info("An Error has occurred while closing the database: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return resposta;
    }
    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static RespUser LogIn(String json) throws JsonProcessingException {
        LOGGER.info("Just received a Login request. Processig...");
        RespUser resposta = new RespUser();
        resposta.setStatus("Failed");
        resposta.setError("none");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        resposta.setJoinDate(new Date(2022, 12, 31));
        System.out.println(json);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(json, Map.class);
        String pass = map.get("pass");
        String user = map.get("user");
        DownloadFile.OpenDB();
        String storedPass = DownloadFile.getUserPass(user);
        try {
            DownloadFile.CloseDB();
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to close the database: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        if(storedPass == null){
            try {
                DownloadFile.CloseDB();
            } catch (SQLException e) {
                LOGGER.info("An error has occurred while closing the database: {}", e.getMessage());
                throw new RuntimeException(e);
            }
            resposta.setError("Invalid Credentials");
            return resposta;
        }
        if(DownloadFile.validatePassword(pass, storedPass)) {
            resposta.setError("Invalid Credentials");
            return resposta;
        }
        resposta.setStatus("Success");
        return resposta;
    }
    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(json, Map.class);
        String pass = map.get("pass");
        String user = map.get("user");
        DownloadFile.OpenDB();
        String storedPass = DownloadFile.getUserPass(user);
        if(storedPass == null) return Response.status(401).entity("{'error':'User not found'}").build(); // Check if user exists
        if(DownloadFile.validatePassword(pass, storedPass)) return Response.status(401).entity("{'error':'User not found'}").build(); // Check if password is correct
        try {
            int userId = getUserId(user);
            // Get Video Paths to remove from disk
            List<Movie> movies = getUserUploads(userId, user); // Get The list of movies uploaded by that user
            File f;
            for(Movie movie : movies){
                LOGGER.info("Movie Info:\n##################\nid: {}, name: {}, path: {}, thumbnail: {}, lowPath: {}, bucketPath: {}, bucketLow: {}", movie.getId(), movie.getName(),movie.getPath(),movie.getThumbnail(),movie.getLowPath(),movie.getBucketPath(),movie.getBucketLowPath());
                f = new File(movie.getPath());
                boolean del = f.delete(); // Remove 1080p version of Movie
                if(f.exists() || !del) LOGGER.info("One of the uploads was not deleted: {}", movie.getPath());
                f = new File(movie.getLowPath()); // Remove 360p version of Movie
                del = f.delete();
                if(!del) LOGGER.info("Failed to delete Low Res of video: {}", movie.getLowPath());
                f = new File(movie.getThumbnail()); // Remove Thumbnail
                f.delete();
            }
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while getting user ID: {}", e.getMessage());
            return Response.status(500).entity("{'error':'An error has occurred while processing the request'}").build();
        }
        boolean del = DeleteUser(user,storedPass); // Delete the user entry on the database


        try {
            DownloadFile.CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Failed to close DataBase {}", e.getMessage());
            throw new RuntimeException(e);
        }
        if(!del) return Response.status(401).entity("{'result':'User not deleted'}").build();
        return Response.status(200).entity("{'result':'User deleted'}").build();

    }

    @POST
    @Path("/get")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static Response getUserInfo(String json){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = null;
        RespUser userInfo = new RespUser();
        userInfo.setStatus("Failure");
        Date date = new Date(2022, 12, 3);

        userInfo.setJoinDate(date);
        try {
            map = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.info("Error reading value: {}", e.getMessage());
            return Response.status(401).entity(userInfo).build();
        }
        String user = map.get("user");
        OpenDB();
        Date joinDate = getJoinDate(user);
        int id;
        try {
            id = getUserId(user);
        } catch (SQLException e) {
            LOGGER.info("Error reading value: {}", e.getMessage());
            return Response.status(401).entity(userInfo).build();
        }
        userInfo.setStatus("Success");
        userInfo.setJoinDate(joinDate);
        userInfo.setId(id);
        try {
            CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Error closing database: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return Response.status(200).entity(userInfo).build();

    }

    @POST
    @Path("/changePass")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(String json){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map;
        try {
            map = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.info("Error reading value: {}", e.getMessage());
            return Response.status(401).entity("Error").build();
        }
        String pass = map.get("pass");
        String user = map.get("user");
        String newPass = map.get("newPass");
        OpenDB();
        String storedPass = DownloadFile.getUserPass(user);
        if(storedPass == null) return Response.status(401).entity("{'error':'User not found'}").build();
        if(DownloadFile.validatePassword(pass, storedPass)) return Response.status(401).entity("{'error':'User not found'}").build();
        boolean change = changePass(user, DownloadFile.passHash(newPass));
        try {
            CloseDB();
            if(change)
            return Response.status(200).entity("{'result':'Password changed'}").build();
            else return Response.status(401).entity("{'result':'Password not changed'}").build();
        } catch (SQLException e) {
            LOGGER.info("Error Closing database: {}", e.getMessage());
            return Response.status(401).build();
        }
    }

    public static boolean checkUserName(String user){
        try {
            PreparedStatement stm = DownloadFile.connection.prepareStatement("""          
                    Select id from users where name = ?
                    """);
            stm.setString(1, user);
            ResultSet r = stm.executeQuery();
            return !r.next();
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while checking if user exists: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static boolean CreateUser(String user, String pass){
        try {
            PreparedStatement stm = DownloadFile.connection.prepareStatement("""
                    Insert into users(name, pass) values(?, ?)
                    """);
            stm.setString(1,user);
            stm.setString(2, pass);
            int rs = stm.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while inserting User: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean DeleteUser(String user, String pass){
        try {
            PreparedStatement stm = DownloadFile.connection.prepareStatement("""
                    Delete from users where name = ? and pass = ?
                    """);
            stm.setString(1,user);
            stm.setString(2, pass);
            int rs = stm.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while Delete User: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Movie> getUserUploads(int userId, String user){
        try{
        PreparedStatement stm = DownloadFile.connection.prepareStatement("""          
                    Select videoId from uploads where userId = ?
                    """);
        stm.setInt(1, userId);
        ResultSet r = stm.executeQuery();
        List<Integer> videos = new ArrayList<>();
        while(r.next()){
            int temp;
            temp = r.getInt(1);
            if(temp == 0) break;
            videos.add(temp);
            LOGGER.info("User: {} -> Video: {}", userId, temp);
            String fileName = getMovieName(temp);
            LOGGER.info("FileName: {} <-> MovieID: {}", fileName, temp);
            if(fileName != null && temp != -1) {
                BucketThread bucketThread = new BucketThread("", true, fileName + "_" + temp + ".mp4");
                bucketThread.start();
                BucketThread bucketThread1 = new BucketThread("", true, fileName + "_360_" + temp + ".mp4");
                bucketThread1.start();
            }
        }
        LOGGER.info("Debbug");
        if(videos.isEmpty()) return new ArrayList<>();
        List<Movie> paths = new ArrayList<>();
        for (Integer video : videos) {
            PreparedStatement stm2 = DownloadFile.connection.prepareStatement("""          
                    Select id, name, path, lowPath, thumbnail, bucketPath, bucketLowPath from videos where id = ?
                    """);
            stm2.setInt(1, video);
            ResultSet r2 = stm2.executeQuery();
            if(r2.next()){
                Movie temp = new Movie();
                temp.setId(r2.getInt(1));
                temp.setName(r2.getString(2));
                temp.setPath(r2.getString(3));
                temp.setLowPath(r2.getString(4));
                temp.setThumbnail(r2.getString(5));
                temp.setBucketPath(r2.getString(6));
                temp.setBucketLowPath(r2.getString(7));
                paths.add(temp);
            }
            removeEntry(user, video);
        }
        return paths;
        } catch (SQLException e) {
            LOGGER.info("Error while getting user uploads: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static int getUserId(String user) throws SQLException {
        PreparedStatement stm = DownloadFile.connection.prepareStatement("""          
                    Select id from users where name = ?
                    """);
        stm.setString(1, user);
        ResultSet r = stm.executeQuery();
        if(r.next()) return r.getInt(1);
        return -1;
    }

    private void removeEntry(String user, int videoId) throws SQLException {
        int userId = getUserId(user);
        if(videoId != -1 && userId != -1){
            removeUploadUser(userId);
            removeVideo(videoId);
        }
    }

    private void removeVideo(int videoId){
        try {
            PreparedStatement stm = DownloadFile.connection.prepareStatement("""
                    Delete from videos
                    where id = ?
                    """);
            stm.setInt(1, videoId);
            int rs = stm.executeUpdate();
            if(rs < 0) {
                LOGGER.info("No video was removed, most likely was never added in the first place");
            }
            else LOGGER.info("Video: {} was successfully removed from the database", videoId);
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to delete the video: {}" ,e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void removeUploadUser(int userId){
        try {
            PreparedStatement stm = DownloadFile.connection.prepareStatement("""
                    Delete from uploads
                    where userId = ?
                    """);
            stm.setInt(1, userId);
            int rs = stm.executeUpdate();
            if(rs < 0) {
                LOGGER.info("No upload was removed, most likely was never added in the first place");
            }
            else LOGGER.info("Upload from : {} was successfully removed from the database", userId);
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to delete the upload: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Date getJoinDate(String user){
        try {
            PreparedStatement stm = connection.prepareStatement("""
                    Select joinDate from users where name = ?
                    """);
            stm.setString(1, user);
            ResultSet rs = stm.executeQuery();
            if(!rs.next()) return null;

            return rs.getDate(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean changePass(String user, String newPass){
        try {
            PreparedStatement stm = DownloadFile.connection.prepareStatement("""
                    Update users
                    set pass = ?
                    where name = ?
                    """);
            stm.setString(1, newPass);
            stm.setString(2, user);
            int rs = stm.executeUpdate();
            if(rs < 0) {
                LOGGER.info("Não foi possivel alterar a palavra passe");
                return false;
            }
            else LOGGER.info("Password was changed for user: {}", user);
            return true;
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while trying to delete the upload: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getMovieName(int id){
        PreparedStatement stm;
        try {
            stm = connection.prepareStatement("""          
                        Select name from videos where id = ?
                        """);
            stm.setInt(1, id);
            ResultSet r = stm.executeQuery();
            if(r.next()) return r.getString(1);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
