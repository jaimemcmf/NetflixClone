package com.example.cms_netflixpp;

import static com.example.cms_netflixpp.FragmentUpload.thumb;
import static com.example.cms_netflixpp.FragmentUpload.uploadURL;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UploadThread extends Thread{
    String user;
    String pass;
    String title;
    String filePathv;
    Context context;
    UploadThread(String user, String pass, String title, String filePathv, Context context){
        this.user = user;
        this.pass = pass;
        this.title = title;
        this.filePathv = filePathv;
        this.context = context;
    }

    public void run(){
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, uploadURL, response -> {
            Toast.makeText(context, "Upload Completed", Toast.LENGTH_SHORT).show();
            thumb = null;
        }, Throwable::printStackTrace) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", user);
                params.put("pass", pass);
                params.put("fileName", title);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData(){
                Map<String, DataPart> params = new HashMap<>();
                try {
                    params.put("upload", new DataPart("ola.mp4", Files.readAllBytes(Paths.get(filePathv)), "multipart/form-data"));
                if (thumb != null) {
                    params.put("thumbnail", new DataPart("file_cover.png", Files.readAllBytes(Paths.get(thumb)), "image/png"));
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        VolleySingleton volleySingleton = new VolleySingleton(context);
        volleySingleton.addToRequestQueue(multipartRequest);
    }
}
