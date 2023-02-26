package com.example.cms_netflixpp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;


public class FragmentRegister extends Fragment {
    final static String signupUrl = "http://{ip}:{port}/Connect/signup";
    Button btnLogin, btnRegister;
    EditText etUserName, etPassword, etRepeatPassword;
    TextView errorTextView;
    CallbackFragmento callbackFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        etUserName = view.findViewById(R.id.etUserName);
        etPassword = view.findViewById(R.id.etPassword);
        etRepeatPassword = view.findViewById(R.id.etRepeatPassword);
        errorTextView = view.findViewById(R.id.errorTxtView);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(view1 -> {
            if (callbackFragment != null) {
                callbackFragment.changeFragmentLogin();
            }
        });

        btnRegister.setOnClickListener(view1 -> {
            String usr = etUserName.getText().toString();
            String p1 = etPassword.getText().toString();
            String p2 = etRepeatPassword.getText().toString();
            if (p1.equals(p2) && !p1.equals("") && !usr.equals("")) {
                errorTextView.setText("");
                signupRequest(usr, p1, p2);
            } else if (!p1.equals(p2)) {
                errorTextView.setText("Passwords do not match.");
            } else {
                errorTextView.setText("Neither the username or password can be empty");
            }
        });

        return view;
    }

    public void setCallbackFragment(CallbackFragmento callbackFragment) {
        this.callbackFragment = callbackFragment;
    }

    public void signupRequest(String user, String password, String passwordConfirm) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());

        JSONObject postData = new JSONObject();
        try {
            postData.put("user", user);
            postData.put("pass", password);
            postData.put("confirmPass", passwordConfirm);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, signupUrl, postData, response -> {
            try {
                if (response.getString("status").equals("Sucesso")) {
                    errorTextView.setText("");
                    Intent intent = new Intent(getActivity(), VideosActivity.class);
                    intent.putExtra("user", user);
                    intent.putExtra("pass", password);
                    startActivity(intent);
                } else {
                    System.out.println("fail");
                    errorTextView.setText(response.getString("error"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> System.out.println(error.toString()));
        requestQueue.add(jsonObjectRequest);
    }

}
