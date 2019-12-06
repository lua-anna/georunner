package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    MapView map = null;
    Route route1 = new Route();
    Route route2 = new Route();
    //JSONArray routesArray = new JSONArray();
    List<Integer> numbers = new ArrayList<>();

    private static final int MENU_ROUTE1 = Menu.FIRST;
    private static final int MENU_ROUTE2 = Menu.FIRST + 1;
    private static final int MENU_CHANGE = Menu.FIRST + 2;
    private static final int MENU_LOGOUT = Menu.FIRST + 3;
    private static final int MENU_CLEAR = Menu.FIRST + 4;
    private static final int MENU_ABOUT = Menu.FIRST + 5;
    private static final int MENU_ERROR = Menu.FIRST + 6;
    private static final int MENU_REPORT = Menu.FIRST + 7;
    Activity activity = MainActivity.this;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(7d);
        GeoPoint pl_center = new GeoPoint(52.18d, 19.35d);
        map.getController().setCenter(pl_center);

        //routesArray = checkRoutes();

        for (int i = 0; i < 100; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers);

        getRoutes();






    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (route1.getName() != "") {
            menu.add(0, MENU_ROUTE1, Menu.NONE, route1.getName());
        } else {
            menu.add(0, MENU_ERROR, Menu.NONE, "Route download error");
        }

        if (route2.getName() != "") {
            menu.add(0, MENU_ROUTE2, Menu.NONE, route2.getName());
        } else {
            menu.add(0, MENU_ERROR, Menu.NONE, "Route download error");
        }
        menu.add(0, MENU_CHANGE, Menu.NONE, R.string.changeRoute);
        menu.add(0, MENU_CLEAR, Menu.NONE, R.string.clear);
        menu.add(0, MENU_LOGOUT, Menu.NONE, R.string.logout);
        menu.add(0, MENU_ABOUT, Menu.NONE, R.string.about);
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        KmlDocument kmlDocument = new KmlDocument();
        InputStream jsonStream;
        InputStream kmlStream;
        String jsonString = null;
        FolderOverlay kmlOverlay;
        BoundingBox bb;
        KmlFeature.Styler styler = new MyKmlStyler();

        switch (item.getItemId()) {
            case MENU_ROUTE1:

                map.getOverlays().clear();
                try {
                    jsonStream = route1.getValues();
                    int size = jsonStream.available();
                    byte[] buffer = new byte[size];
                    jsonStream.read(buffer);
                    jsonStream.close();
                    jsonString = new String(buffer, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    ex.printStackTrace();

                }

                kmlDocument.parseGeoJSON(jsonString);
                kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);

                map.getOverlays().add(kmlOverlay);
                map.invalidate();
                bb = kmlDocument.mKmlRoot.getBoundingBox();
                bb.increaseByScale(1.5f);
                map.zoomToBoundingBox(bb, true);
                return true;




            case MENU_ROUTE2:

                map.getOverlays().clear();
                try {
                    jsonStream = route2.getValues();
                    int size = jsonStream.available();
                    byte[] buffer = new byte[size];
                    jsonStream.read(buffer);
                    jsonStream.close();
                    jsonString = new String(buffer, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    ex.printStackTrace();

                }

                kmlDocument.parseGeoJSON(jsonString);
                kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);

                map.getOverlays().add(kmlOverlay);
                map.invalidate();
                bb = kmlDocument.mKmlRoot.getBoundingBox();
                bb.increaseByScale(1.5f);
                map.zoomToBoundingBox(bb, true);
                return true;


            case MENU_CHANGE:

                getRoutes();




            case MENU_CLEAR:
                map.getOverlays().clear();
                map.invalidate();
                return true;

            case MENU_LOGOUT:
                finish();

                return true;

            case MENU_ERROR:
                getRoutes();

                return true;

            case MENU_ABOUT:
                new AlertDialog.Builder(activity)
                        .setTitle("About")
                        .setMessage("Georunner")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        //.setNegativeButton(android.R.string.no, null)
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;


            default:

                return super.onOptionsItemSelected(item);

        }
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void getRoutes() {

        if (numbers.size()<2){
            numbers = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                numbers.add(i);
            }

            Collections.shuffle(numbers);

        }

        route1 = getRouteQuery("route "+numbers.get(0));
        route2 = getRouteQuery("route "+numbers.get(1));

        numbers.remove(0);
        numbers.remove(0);


    }

    public class Route {
        public String name;
        public String values;
        public String ids;
        public int count;

        public Route() {
            name="";
        }

        public Route(String name, String values, String ids, int count) {
            this.name = name;
            this.values = values;
            this.ids = ids;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public InputStream getValues() {
            InputStream inputStream = new ByteArrayInputStream(values.getBytes(Charset.forName("UTF-8")));
            return inputStream;
        }

        public void setValues(String values) {
            this.values = values;
        }

        public String getIds() {
            return ids;
        }

        public void setIds(String ids) {
            this.ids = ids;


        }

    }

//    private JSONArray checkRoutes() {
//
//
//
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url ="http://vps510297.ovh.net:3123/api/routes/";
//
//
//
//        JsonObjectRequest getRouteRequest = new JsonObjectRequest(Request.Method.POST, url, null,
//                new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//
//                        if (response.optString("success").equals("true")) {
//
//                            try{
//
//
//                                routesArray = response.getJSONArray("route");
//
//
//                            }catch (JSONException e){
//                                Log.d("Error", e.toString());
//                            }
//                        }
//                    }
//                },
//
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                        if (error instanceof NoConnectionError) {
//                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.offline), Toast.LENGTH_SHORT).show();
//                        } else if (error instanceof TimeoutError) {
//                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_offline), Toast.LENGTH_LONG).show();
//                        } else if (error instanceof AuthFailureError) {
//                            Toast.makeText(getApplicationContext(), "AuthFailureError"+error.toString(), Toast.LENGTH_SHORT).show();
//                        } else if (error instanceof ServerError) {
//                            NetworkResponse networkResponse = error.networkResponse;
//                            if (networkResponse != null && networkResponse.data != null) {
//                                String jsonError = new String(networkResponse.data);
//
//                                try {
//                                    JSONObject errorResponse = new JSONObject(jsonError);
//
//                                    if (!errorResponse.optString("name").equals("")) {
//                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_req), Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(getApplicationContext(), errorResponse.toString(), Toast.LENGTH_SHORT).show();
//                                    }
//
//
//                                } catch (JSONException err) {
//                                    Toast.makeText(getApplicationContext(), err.toString(), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        } else if (error instanceof NetworkError) {
//                            Toast.makeText(getApplicationContext(), "NetworkError"+error.toString(), Toast.LENGTH_SHORT).show();
//                        } else if (error instanceof ParseError) {
//                            Toast.makeText(getApplicationContext(), "ParseError"+error.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//
//
//                }
//        );
//
//        queue.add(getRouteRequest);
//
//    return routesArray;
//
//
//    }


    private Route getRouteQuery(String name) {

        final Route route = new Route();

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://vps510297.ovh.net:3123/api/routes/getRoute";

            JSONObject jsonBody = new JSONObject();

            jsonBody.put("NAME", name);

            JsonObjectRequest getRouteRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                            if (response.optString("success").equals("true")) {

                                route.setName(response.optString("name"));
                                route.setValues(response.optString("values"));
                                route.setIds(response.optString("ids"));

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
                                            Toast.makeText(getApplicationContext(), errorResponse.toString(), Toast.LENGTH_SHORT).show();

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
                    }
            );

            queue.add(getRouteRequest);
        } catch (JSONException e) {
            Log.d("Error", e.toString());
        }

        return route;

    }





    class MyKmlStyler implements KmlFeature.Styler {

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString){
            polyline.setWidth(3.0f);
            polyline.setColor(Color.RED);
        }

        @Override
        public void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
        }


    }
}