package org.georunner;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class RouteValues {
    private JSONArray jsonArray;
    private Route route;
    private Context context;
    private Activity activity;

    RouteValues(JSONArray jsonArray, Route route, Activity activity) {
        this.jsonArray = jsonArray;
        this.route = route;
        this.activity = activity;
        this.context = activity.getApplicationContext();

    }

    Route getRoute() {

        try {
            String name = jsonArray.getJSONObject(0).optString("name");


            RequestQueue queue = Volley.newRequestQueue(context);
            String url ="http://vps781559.ovh.net:3123/api/routes/getRoute";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);

            JsonObjectRequest getRouteRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (response.optString("success").equals("true")) {
                                route.setName(response.optString("name"));
                                route.setValues(response.optString("values"));
                                route.setIds(response.optString("ids"));

                                activity.invalidateOptionsMenu();

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError) {
                                Toast.makeText(context, context.getResources().getString(R.string.offline), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof TimeoutError) {
                                Toast.makeText(context, context.getResources().getString(R.string.server_offline), Toast.LENGTH_LONG).show();
                            } else if (error instanceof AuthFailureError) {
                                Toast.makeText(context, "AuthFailureError"+error.toString(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ServerError) {
                                NetworkResponse networkResponse = error.networkResponse;
                                if (networkResponse != null && networkResponse.data != null) {
                                    String jsonError = new String(networkResponse.data);
                                    try {
                                        JSONObject errorResponse = new JSONObject(jsonError);
                                        Toast.makeText(context, errorResponse.toString(), Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        Log.d("Error", e.getMessage(), e);
                                    }
                                }
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(context, "NetworkError"+error.toString(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ParseError) {
                                Toast.makeText(context, "ParseError"+error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            queue.add(getRouteRequest);

        } catch (JSONException e) {
            Log.d("Error", e.getMessage(), e);
        }
        jsonArray.remove(0);
        return route;
    }

}
