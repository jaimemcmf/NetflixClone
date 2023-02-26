package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayActivity extends AppCompatActivity {
    PlayerView playerView;
    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        hideSystemUI(getWindow());
        Intent intent = getIntent();
        String user = intent.getStringExtra("user");
        String pass = intent.getStringExtra("pass");
        String movie_id = intent.getStringExtra("movieId");
        String resolution = intent.getStringExtra("resolution");
        playerView = findViewById(R.id.video_view);
        streamRequest(user, pass, movie_id, resolution, "Bucket");
    }

    protected void streamRequest(String user, String pass, String movieID, String resolution, String source) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject postData = new JSONObject();
        try {
            postData.put("user", user);
            postData.put("pass", pass);
            postData.put("videoId", movieID);
            postData.put("res", resolution);
            postData.put("source", source);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://{ip}:{port}/stream/", postData, response -> {
            try {
                String videoURL = response.getString("URL");
                System.out.println(videoURL);
                player = new ExoPlayer.Builder(getApplicationContext()).build();
                player.addListener(new Player.Listener() {
                                       @Override
                                       public void onPlayerError(@NonNull PlaybackException error) {
                                           Player.Listener.super.onPlayerError(error);
                                           if(source.equals("Bucket")) streamRequest(user, pass, movieID, resolution, "NGINX");
                                       }
                                   }
                );
                playerView.setPlayer(player);
                MediaItem mediaItem = MediaItem.fromUri(videoURL);
                player.setMediaItem(mediaItem);
                player.seekTo(0);
                player.prepare();
                if(source.equals("NGINX")) Toast.makeText(this, "You may have to click play more than once to begin.", Toast.LENGTH_SHORT).show();
                player.play();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> System.out.println(error.toString()));
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
        }
        player = null;
    }

    public void hideSystemUI(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getInsetsController().hide(WindowInsets.Type.systemBars());
        } else {
            View decorView = window.getDecorView();
            int uiVisibility = decorView.getSystemUiVisibility();
            uiVisibility |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            uiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            uiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiVisibility);
        }
    }

}