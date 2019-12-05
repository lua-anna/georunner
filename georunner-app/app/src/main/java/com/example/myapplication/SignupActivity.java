package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        Button signupBtn = findViewById(R.id.reg_signup_btn);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                registerQuery();
            }
        });


    }

    private void registerQuery() {

        EditText username = findViewById(R.id.reg_username);
        EditText mail = findViewById(R.id.reg_mail);
        EditText password = findViewById(R.id.reg_pass);
        EditText password2 = findViewById(R.id.reg_pass_repeat);


        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://vps510297.ovh.net:3123/api/users/register";

            JSONObject jsonBody = new JSONObject();

            jsonBody.put("name", username.getText());
            jsonBody.put("email", mail.getText());
            jsonBody.put("password", password.getText());
            jsonBody.put("password2", password2.getText());


            // Request a string response from the provided URL.
            JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                    if (!response.optString("_id").equals("")) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.signed_up), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error instanceof NoConnectionError) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.offline), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof TimeoutError) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_offline), Toast.LENGTH_LONG).show();
                            } else if (error instanceof AuthFailureError) {
                                Toast.makeText(getApplicationContext(), "AuthFailureError"+error.toString(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ServerError) {
                                NetworkResponse networkResponse = error.networkResponse;
                                if (networkResponse != null && networkResponse.data != null) {
                                    String jsonError = new String(networkResponse.data);

                                    try {
                                        JSONObject errorResponse = new JSONObject(jsonError);

                                        if (!errorResponse.optString("name").equals("")) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.username_req), Toast.LENGTH_SHORT).show();
                                        } else if (!errorResponse.optString("email").equals("")) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_req), Toast.LENGTH_SHORT).show();
                                        } else if (!errorResponse.optString("password").equals("")) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.reg_password_req), Toast.LENGTH_SHORT).show();
                                        } else if (!errorResponse.optString("password2").equals("")) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.reg_password2_req), Toast.LENGTH_SHORT).show();
                                        }

                                        else if (!errorResponse.optString("emailalreadyexists").equals("")) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_exists), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), errorResponse.toString(), Toast.LENGTH_SHORT).show();
                                        }


                                    } catch (JSONException err) {
                                        Toast.makeText(getApplicationContext(), err.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(getApplicationContext(), "NetworkError"+error.toString(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ParseError) {
                                Toast.makeText(getApplicationContext(), "ParseError"+error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }



                    });

            // Add the request to the RequestQueue.
            queue.add(registerRequest);
        } catch (JSONException e) {
            Log.d("Error", e.toString());

        }

    }
}
