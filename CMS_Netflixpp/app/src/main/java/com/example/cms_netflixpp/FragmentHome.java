package com.example.cms_netflixpp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentHome extends Fragment {
    View view;
    ArrayList<MovieModel> arrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        arrayList = new ArrayList<>();
        movieListRequest();
        return view;
    }

    protected void movieListRequest() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());
        JSONObject postData = new JSONObject();
        try {
            assert getArguments() != null;
            postData.put("user", getArguments().getString("user"));
            postData.put("pass", getArguments().getString("pass"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://{ip}:{port}/search/user", postData, response -> {
            try {
                final ListView list = view.findViewById(R.id.list);
                arrayList = new ArrayList<>();
                for (int i = 0; i < response.getJSONArray("movies").length(); i++) {
                    JSONObject jo = response.getJSONArray("movies").getJSONObject(i);
                    arrayList.add(new MovieModel(jo.getString("name"), jo.getInt("id"), "http://{ip}:{port}/download/thumbnail/" + jo.getInt("id")));
                }
                if(arrayList.isEmpty()){
                    arrayList.add(new MovieModel("No movies found", -1, null));
                }
                assert getArguments() != null;
                MovieAdapter movieAdapter = new MovieAdapter(this.getContext(), requireActivity().getApplicationContext(), arrayList, getArguments().getString("user"), getArguments().getString("pass"), FragmentHome.this);
                list.setAdapter(movieAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            final ListView list = view.findViewById(R.id.list);
            arrayList = new ArrayList<>();
            arrayList.add(new MovieModel("No movies found", -1, null));
            MovieAdapter movieAdapter = new MovieAdapter(this.getContext(), requireActivity().getApplicationContext(), arrayList, getArguments().getString("user"), getArguments().getString("pass"), FragmentHome.this);
            list.setAdapter(movieAdapter);
        });
        requestQueue.add(jsonObjectRequest);
    }

}
