package com.mykong;

import ch.qos.logback.classic.Logger;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

@Path("/download")
public class DownloadFile {

    public static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DownloadFile.class);
    protected static Connection connection;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping(){
        LOGGER.info("Received GET request for download");
        return Response.status(200).entity("{'status':'success'}").build();
    }

    @GET
    @Path("/thumbnail/{id}")
    @Produces("image/png")
    public Response getFile(@PathParam("id") int id) throws SQLException{
        LOGGER.info("Received an request to download Thumbnail");
        LOGGER.info("Id passed: {}", id);
        String path = null;
        OpenDB();
        path = getThumbnail(id);
        LOGGER.info("Returned path: {}", path);
        CloseDB();
        if (path == null) {
            LOGGER.info("Request Denied. No file with id {} was found", id);
            return Response.status(404).build();
        }
        File file = new File(path);

        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=image_from_server.png");
        return response.build();
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

    public static String getUserPass(String user){
        try{
            PreparedStatement stm = connection.prepareStatement("""
                    Select pass from users where name = ?
                    """);
            stm.setString(1, user);
            ResultSet rs = stm.executeQuery();
            if(!rs.next()) return null;
            else return rs.getString(1);
        } catch (SQLException e) {
            LOGGER.info("Exception thrown: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static boolean validatePassword(String originalPassword, String storedPassword)
    {

        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = new byte[0];
        try {
            salt = fromHex(parts[1]);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info("An error has occured while validating the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        byte[] hash = new byte[0];
        try {
            hash = fromHex(parts[2]);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info("An error has occured while validating the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(),
                salt, iterations, hash.length * 8);
        SecretKeyFactory skf = null;
        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info("An error has occured while validating the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        byte[] testHash = new byte[0];
        try {
            testHash = skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            LOGGER.info("An error has occured while validating the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
        }
        return diff != 0;
    }
    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i < bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    protected static String passHash(String password)

    {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = new byte[0];
        salt = getSalt();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 4);
        SecretKeyFactory skf = null;
        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info("An error has occured while hashing the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        byte[] hash = new byte[0];
        try {
            hash = skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            LOGGER.info("An error has occured while hashing the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    private static byte[] getSalt()
    {
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info("An error has occured while salting the password: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);

        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }




    public static void OpenDB() {
        //System.out.println("Opening Database");
        String user = "root";
        String password = "rootPassword";
        String url = "jdbc:mysql://localhost:3306/{databaseName}";
        String driver = "com.mysql.cj.jdbc.Driver";
        try {
            Class.forName(driver); // Directly import Mysql Driver in order to access the dataBase
        } catch (Exception ex) {
            System.out.println("Error  " +  ex);
        }
        try { // Establish Connection to the dataBase
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error " + e);
        }

    }

    public static void CloseDB() throws SQLException{
        connection.close();
    }
}
