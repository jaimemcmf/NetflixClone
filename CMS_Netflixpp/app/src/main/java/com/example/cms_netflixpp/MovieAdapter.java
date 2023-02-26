package com.example.cms_netflixpp;

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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import android.app.AlertDialog;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MovieAdapter extends BaseAdapter implements ListAdapter {
    FragmentHome fragmentHome;
    String deleteUrl = "http://{ip}:{port}/delete/";
    Context context;
    Context c;
    ArrayList<MovieModel> arrayList;
    String user;
    String pass;
    public Intent intent;
    View newView;

    public MovieAdapter(Context c, Context context, ArrayList<MovieModel> list, String user, String pass, FragmentHome fragmentHome) {
        super();
        this.c = c;
        this.context = context;
        this.arrayList = list;
        this.user = user;
        this.pass = pass;
        this.fragmentHome = fragmentHome;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieModel movie = arrayList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.movie_row, null);
            convertView.setOnClickListener(v -> {
                if(movie.getId() != -1){
                    newView = v;
                    AlertDialog.Builder alert = new AlertDialog.Builder(c, R.style.DeleteDialog);
                    alert.setTitle("Delete Movie");
                    alert.setMessage("Are you sure you want to delete this movie?");
                    alert.setPositiveButton("Yes", (dialog, which) -> {
                        deleteRequest(this.user, this.pass, Integer.toString(movie.getId()));
                        dialog.dismiss();
                    });
                    alert.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                    alert.show();
                }
            });
            TextView tittle = convertView.findViewById(R.id.movie_title);
            ImageView imageView = convertView.findViewById(R.id.thumbnail);
            tittle.setText(movie.getName());
            Picasso.with(context).load(movie.thumbnail).into(imageView);
        }
        return convertView;
    }

    protected void deleteRequest(String user, String password, String movieId) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONObject postData = new JSONObject();
        try {
            postData.put("user", user);
            postData.put("pass", password);
            postData.put("movieId", movieId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, deleteUrl, postData,
                response -> System.out.println("deleted"),
                error -> {
                    fragmentHome.movieListRequest();
                    Toast.makeText(context, "The movie was successfully deleted.", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(jsonObjectRequest);
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
