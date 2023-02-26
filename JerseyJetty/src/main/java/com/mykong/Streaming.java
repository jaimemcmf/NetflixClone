package com.mykong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static com.mykong.DownloadFile.*;

@Path("/stream")
public class Streaming {
    @GET
    public String ping(){
        return "Testing streaming rout";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response streamMovie(String json) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = null;
        try {
            map = mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.info("Error read");
            throw new RuntimeException(e);
        }
        int movieId = Integer.parseInt(map.get("videoId"));
        String user = map.get("user");
        String pass = map.get("pass");
        String res = map.get("res");
        String source = map.get("source");
        OpenDB();
        String storedPass = getUserPass(user);
        // Grabbing the stored password from user
        if(storedPass == null || validatePassword(pass, storedPass)){                                  // Check if the user exists, if yes, compare the passwords
            LOGGER.info("No user with the credentials name: {} ; pass: {} ; was found", user, pass);
            return Response.status(401).entity("No user was found, please check the provided credentials").build();
        }

        int userId = getUserId(user);
        if(userId == -1) return Response.status(401).entity("An error has occurred while getting the user").build();
        String URL = null;
        if(source.equals("NGINX")) {
            File f1;
            String MoviePath;
            if(res.equals("1080p")) {
                MoviePath = getHighMoviePath(movieId);
                if(MoviePath == null) return Response.status(401).entity("Movie not found").build();
                f1 = new File("/tmp/hls/" + movieId + "-0.ts");
                URL = "http://{ip}:{port}/" + movieId + ".m3u8";
            }else{
                MoviePath = getLowMoviePath(movieId);
                if(MoviePath == null) return Response.status(401).entity("Movie not found").build();
                f1 = new File("/tmp/hls/" + movieId + "_360-0.ts");
                URL = "http://{ip}:{port}/" + movieId + "_360.m3u8";
            }
            if (f1.exists()) {
                LOGGER.info("Stream already exists");
                return Response.status(200).entity("{'URL':'" + URL + "'}").build();
            }
            LOGGER.info("Stream does not exist, creating new one");
            StreamThread streamThread = new StreamThread(movieId, MoviePath, res.equals("1080p"));
            streamThread.start();
            //MovieURL mu = new MovieURL(URL);
        }else if(source.equals("Bucket")){
            URL = getBucket(movieId, res.equals("360p"));
        }
        try {
            CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Error closing: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return Response.status(200).entity("{'URL':'" + URL + "'}").build();
    }

    public String getHighMoviePath(int id) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("""          
                    Select path from videos where id = ?
                    """);
        stm.setInt(1, id);
        ResultSet r = stm.executeQuery();
        if(r.next()) return r.getString(1);
        return null;
    }

    public String getLowMoviePath(int id) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("""          
                    Select lowPath from videos where id = ?
                    """);
        stm.setInt(1, id);
        ResultSet r = stm.executeQuery();
        if(r.next()) return r.getString(1);
        return null;
    }

    public int getUserId(String user) throws SQLException {
        PreparedStatement stm = connection.prepareStatement("""          
                    Select id from users where name = ?
                    """);
        stm.setString(1, user);
        ResultSet r = stm.executeQuery();
        if(r.next()) return r.getInt(1);
        return -1;
    }

    public String getBucket(int movieId, Boolean High){
        try{
            PreparedStatement stm = connection.prepareStatement("""          
                    Select bucketPath, bucketLowPath from videos where id = ?
                    """);
            stm.setInt(1, movieId);
            ResultSet r = stm.executeQuery();
            if(r.next()) {
                if(!High) return r.getString(1);
                return r.getString(2);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
