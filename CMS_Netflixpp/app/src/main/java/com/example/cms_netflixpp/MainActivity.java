package com.example.cms_netflixpp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements CallbackFragmento {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CMS_Netflixpp);
        setContentView(R.layout.activity_main);
        addFragment();
    }

    public void addFragment() {
        FragmentLogin fragment = new FragmentLogin();
        fragment.setCallbackFragment(this);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragmentRegister() {
        FragmentRegister fragment = new FragmentRegister();
        fragment.setCallbackFragment(this);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragmentLogin() {
        FragmentLogin fragment = new FragmentLogin();
        fragment.setCallbackFragment(this);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void changeFragmentRegister() {
        replaceFragmentRegister();
    }

    @Override
    public void changeFragmentLogin() {
        replaceFragmentLogin();
    }

}