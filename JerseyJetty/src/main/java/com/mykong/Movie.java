package com.mykong;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Movie {
    String name;
    int id;
    String lowPath;
    String path;
    String thumbnail;
    String bucketLowPath;
    String bucketPath;

    public void setId(int id){
        this.id = id;
    }
    public void setPath (String path){
        this.path = path;
    }
    public void setLowPath(String path){
        this.lowPath = path;
    }
    public void setThumbnail(String thumbnail){
        this.thumbnail = thumbnail;
    }
    public void setBucketLowPath(String bucketLowPath){
        this.bucketLowPath = bucketLowPath;
    }
    public void setBucketPath(String bucketPath){
        this.bucketPath = bucketPath;
    }

    public String getPath(){
        return path;
    }
    public String getLowPath(){
        return lowPath;
    }
    public String getThumbnail(){
        return thumbnail;
    }
    public String getBucketLowPath(){
        return bucketLowPath;
    }
    public String getBucketPath(){
        return bucketPath;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getId(){ return this.id; }
    public String getName(){ return this.name; }

    @Override
    public String toString(){
        try {
            // takes advantage of toString() implementation to format {"a":"b"}
            return new JSONObject().put("id", id).put("name", name).toString();
        } catch (JSONException e) {
            return null;
        }
    }
}

