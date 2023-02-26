package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class FragmentUser extends Fragment {
    final String deleteUrl = "http://{ip}:{port}/Connect/delete/";
    final String changePassUrl = "http://{ip}:{port}/Connect/changePass/";
    final String getInfoUrl = "http://{ip}:{port}/Connect/get/";
    Button btnDeleteAccount, btnChangePassword;
    EditText etNewPass;
    TextView usernameName, joinedDate, usernameId;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        etNewPass = view.findViewById(R.id.newPassEt);
        usernameName = view.findViewById(R.id.usernameName);
        joinedDate = view.findViewById(R.id.joinedDate);
        usernameId = view.findViewById(R.id.user_id);
        assert getArguments() != null;
        usernameName.setText("Hello, " + getArguments().getString("user") + "!");
        infoRequest();

        btnDeleteAccount.setOnClickListener(view1 -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext(), R.style.DeleteDialog);
            alert.setTitle("Delete");
            alert.setMessage("Are you sure you want to delete your account?");
            alert.setPositiveButton("Yes", (dialog, which) -> {
                deleteAccountRequest();
                dialog.dismiss();
            });
            alert.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            alert.show();
        });

        btnChangePassword.setOnClickListener(view1 -> {
            String newPass = etNewPass.getText().toString();
            if(!newPass.equals("")){
                changePasswordRequest(newPass);
            }
        });

        return view;
    }

    public void deleteAccountRequest() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());

        JSONObject postData = new JSONObject();
        try {
            assert getArguments() != null;
            postData.put("user", getArguments().getString("user"));
            postData.put("pass", getArguments().getString("pass"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, deleteUrl, postData, response -> {
            Intent intent = new Intent(getActivity(), StartActivity.class);
            startActivity(intent);
            Toast.makeText(getContext(), "Your account has been deleted.", Toast.LENGTH_SHORT).show();
        }, error -> System.out.println(error.toString()));
        requestQueue.add(jsonObjectRequest);
    }

    public void changePasswordRequest(String newPass) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());

        JSONObject postData = new JSONObject();
        try {
            assert getArguments() != null;
            postData.put("user", getArguments().getString("user"));
            postData.put("pass", getArguments().getString("pass"));
            postData.put("newPass", newPass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, changePassUrl, postData,
                response -> {
                    Intent intent = new Intent(getActivity(), StartActivity.class);
                    startActivity(intent);
                    Toast.makeText(getContext(), "Your password has been updated.", Toast.LENGTH_SHORT).show();
            }, error -> System.out.println(error.toString()));
        requestQueue.add(jsonObjectRequest);
    }

    public void infoRequest() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());

        JSONObject postData = new JSONObject();
        try {
            assert getArguments() != null;
            postData.put("user", getArguments().getString("user"));
            postData.put("pass", getArguments().getString("pass"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        @SuppressLint("SetTextI18n") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getInfoUrl, postData, response -> {
            try {
                if (response.getString("status").equals("Success")) {
                    String joinDate = response.getString("joinDate");
                    String id = Integer.toString(response.getInt("id"));
                    joinedDate.setText("Joined in: " + joinDate);
                    usernameId.setText("User ID: " + id);
                } else {
                    System.out.println("Invalid login");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> System.out.println(error.toString()));
        requestQueue.add(jsonObjectRequest);
    }
}