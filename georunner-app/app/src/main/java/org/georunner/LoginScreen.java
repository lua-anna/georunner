package org.georunner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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


public class LoginScreen extends AppCompatActivity {

    private SharedPreferences lPrefs;
    private static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    private static final String PREFS_NAME = "org.georunner.lgn";
    private static final String PREFS_MAIL = "mail";
    private static final String PREFS_PASSWORD = "password";
    private static final String PREFS_REMEMBER = "rememberPassword";
    CheckBox rememberPass;
    EditText userMail;
    EditText userPass;
    Button loginBtn;
    Button signupBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        rememberPass = findViewById(R.id.rememberPass);
        userMail = (EditText) findViewById(R.id.mail);
        userPass = (EditText) findViewById(R.id.pass);
        loginBtn = findViewById(R.id.login_btn);
        signupBtn = findViewById(R.id.signup_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginQuery();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSignup();
            }
        });

        lPrefs = getApplicationContext().getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);

        userMail.setText(lPrefs.getString(PREFS_MAIL, ""));
        userPass.setText(lPrefs.getString(PREFS_PASSWORD, ""));
        rememberPass.setChecked(lPrefs.getBoolean(PREFS_REMEMBER, false));


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }


    }



    protected void onStop() {
        super.onStop();

        userMail.setText(lPrefs.getString(PREFS_MAIL, ""));
        userPass.setText(lPrefs.getString(PREFS_PASSWORD, ""));
        rememberPass.setChecked(lPrefs.getBoolean(PREFS_REMEMBER, false));
    }

    private void gotoSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    public void gotoMap() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void loginQuery() {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://vps781559.ovh.net:3123/api/users/login";

            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", userMail.getText());
            jsonBody.put("password", userPass.getText());

            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response.optString("success").equals("true")) {

                        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.logged_in), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM,0,200);
                        toast.show();

                        if (rememberPass.isChecked())
                        {
                            lPrefs.edit().putString(PREFS_MAIL, userMail.getText().toString()).apply();
                            lPrefs.edit().putString(PREFS_PASSWORD, userPass.getText().toString()).apply();
                            lPrefs.edit().putBoolean(PREFS_REMEMBER, rememberPass.isChecked()).apply();
                        } else
                        {
                            lPrefs.edit().clear().apply();
                        }
                        gotoMap();
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

                                if (!errorResponse.optString("email").equals("")) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_req), Toast.LENGTH_SHORT).show();
                                } else if (!errorResponse.optString("password").equals("")) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_req), Toast.LENGTH_SHORT).show();
                                } else if (!errorResponse.optString("emailnotfound").equals("") || !errorResponse.optString("passwordincorrect").equals("")){
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_pass_wrong), Toast.LENGTH_SHORT).show();
                                } else if (!errorResponse.optString("not_verified").equals("")){
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_verified), Toast.LENGTH_SHORT).show();
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

            queue.add(loginRequest);
        } catch (JSONException e) {
            Log.d("Error", e.toString());
        }
    }

}


