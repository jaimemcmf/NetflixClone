package com.example.cms_netflixpp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class VideosActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    Bundle bundle = new Bundle();
    FragmentHome fragmentHome = new FragmentHome();
    FragmentUser fragmentUser = new FragmentUser();
    FragmentUpload fragmentUpload = new FragmentUpload();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);
        setTheme(R.style.Theme_Videos);
        Intent intent = getIntent();
        bundle.putString("user", intent.getStringExtra("user"));
        bundle.putString("pass", intent.getStringExtra("pass"));
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.footer_home);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.footer_home:
                fragmentHome.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragmentHome).commit();
                return true;

            case R.id.footer_upload:
                fragmentUpload.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragmentUpload).commit();
                return true;

            case R.id.footer_user:
                fragmentUser.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, fragmentUser).commit();
                return true;
        }
        return false;
    }
}