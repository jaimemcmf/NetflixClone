package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import android.app.AlertDialog;

import java.util.ArrayList;

public class MovieAdapter extends BaseAdapter implements ListAdapter {
    String selected = null;
    Context context;
    Context c;
    ArrayList<MovieModel> arrayList;
    String user;
    String pass;
    public View newView;
    public Intent intent;

    public MovieAdapter(Context c, Context context, ArrayList<MovieModel> list, String user, String pass) {
        super();
        this.c = c;
        this.context = context;
        this.arrayList = list;
        this.user = user;
        this.pass = pass;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieModel movie = arrayList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.movie_row, null);
            convertView.setOnClickListener(v -> {
                newView = v;
                intent = new Intent(context, PlayActivity.class);
                intent.putExtra("user", this.user);
                intent.putExtra("pass", this.pass);
                intent.putExtra("movieId", Integer.toString(movie.getId()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(movie.getId() != -1) setResolution();
            });
            TextView tittle = convertView.findViewById(R.id.movie_title);
            ImageView imageView = convertView.findViewById(R.id.thumbnail);
            tittle.setText(movie.getName());
            Picasso.with(context).load(movie.thumbnail).into(imageView);
        }
        return convertView;
    }

    public void setResolution() {
        String[] resolutions = {"360p", "1080p"};
        AlertDialog.Builder alert = new AlertDialog.Builder(c, R.style.DeleteDialog);
        alert.setTitle("Resolution");
        alert.setSingleChoiceItems(resolutions, -1, (dialog, which) -> selected = resolutions[which]);
        alert.setPositiveButton("Continue", (dialog, which) -> {
            if (selected != null) {
                intent.putExtra("resolution", selected);
                newView.getContext().startActivity(intent);
            }
        });
        alert.setNegativeButton("Back", (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
