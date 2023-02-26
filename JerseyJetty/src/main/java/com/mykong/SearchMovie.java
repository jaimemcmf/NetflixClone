package com.mykong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mykong.DownloadFile.*;
import static com.mykong.UploadFile.LOGGER;


@Path("/search")
public class SearchMovie {
    @GET // Testing Search Rout
    public String ping(){
        return "Testing search rout";
    }


    @Path("/name/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchMovies(@PathParam("name") String movieName){
        OpenDB();
        List<Movie> movies =  getMovies(movieName);
        try {
            CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Failed to close DB");
            throw new RuntimeException(e);
        }
        if(movies == null) return Response.status(204).entity("{ 'empty':true' }").build(); // In Case no movies are found, return empty


        return Response.status(200).entity("{ 'movies': "+ movies + "}").build();
    }

    @Path("/all") // Return every movie
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchMovies(){
        OpenDB();
        List<Movie> movies =  getMovies("");
        try {
            CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Failed to close DB");
            throw new RuntimeException(e);
        }
        if(movies == null) return Response.status(204).entity("{ 'movies':[] }").build();


        return Response.status(200).entity("{ 'movies': "+ movies + "}").build();
    }

    @Path("/user") // Return every movie belonging to the user
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchUserUploads(String json){
        OpenDB();
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
        LOGGER.info("user: {} pass: {}", user, pass);
        String storedPass = getUserPass(user);
        LOGGER.info("User pass: {}", storedPass);
        if(storedPass == null || validatePassword(pass, storedPass)){
            return Response.status(401).entity("Credentials not found").build();
        }
        List<Movie> movies =  getUserUploads(user);
        LOGGER.info("Number of movies: {}", movies.size());
        try {
            CloseDB();
        } catch (SQLException e) {
            LOGGER.info("Failed to close DB");
            throw new RuntimeException(e);
        }
        if(movies == null || movies.size() == 0) {
            return Response.status(204).entity("{ 'movies':[{'id':-1, 'name':'No Movies Found'}] }").build();
        }
        return Response.status(200).entity("{ 'movies': "+ movies + "}").build();

    }






    public List<Movie> getMovies(String movieName){
        try {
            LOGGER.info("Movie name: {}", movieName);

            movieName = movieName
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_")
                    .replace("[", "![");
            PreparedStatement stm = connection.prepareStatement("""          
                    Select id, name from videos where name like ? ESCAPE '!'
                    order by name, id
                    """);
            stm.setString(1, "%" + movieName + "%");
            ResultSet r = stm.executeQuery();
            List<Movie> movies = new ArrayList<>();
            while(r.next()){
                Movie temp = new Movie();
                temp.setId(r.getInt(1));
                temp.setName(r.getString(2));
                movies.add(temp);
            }


            return movies;
        } catch (SQLException e) {
            LOGGER.info("An error has occurred while getting the movie list: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Movie> getUserUploads(String user){
        try {
            PreparedStatement stm = connection.prepareStatement("""          
                    Select id, name from videos where id in (Select videoId from uploads where userId in (Select id from users where name = ?))
                    order by name, id
                    """);
            stm.setString(1, user);
            ResultSet r = stm.executeQuery();
            List<Movie> movies = new ArrayList<>();
            while (r.next()) {
                Movie temp = new Movie();
                temp.setId(r.getInt(1));
                temp.setName(r.getString(2));
                movies.add(temp);
            }
            return movies;
        }  catch(SQLException e){
            LOGGER.info("Error Getting user uploaded movies: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
