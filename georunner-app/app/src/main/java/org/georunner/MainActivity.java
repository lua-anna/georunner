package org.georunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.BuildConfig;
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
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "org.georunner.prefs";
    private static final String PREFS_LATITUDE_STRING = "latitudeString";
    private static final String PREFS_LONGITUDE_STRING = "longitudeString";
    private static final String PREFS_ORIENTATION = "orientation";
    private static final String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";

    private String menu_route = "";

    Activity activity = MainActivity.this;
    MapView map = null;
    Route route1 = new Route();
    Route route2 = new Route();
    List<Integer> numbers = new ArrayList<>();
    private SharedPreferences mPrefs;
    //protected ImageButton centerMap;
    protected ImageButton followMap;
    //private Location currentLocation = null;
    private MyLocationNewOverlay mLocationOverlay;
    //private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    JSONArray routesArray;

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        mPrefs = getApplicationContext().getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);
        map = findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setFlingEnabled(false);
        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);


//        myCompassOverlay mCompassOverlay = new myCompassOverlay(this, new InternalCompassOrientationProvider(this), map);
//        mCompassOverlay.enableCompass();



        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setAlignBottom(true);

        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(getApplicationContext());
        mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        mLocationOverlay.setDrawAccuracyEnabled(true);
        //mMyLocationOverlay.setEnableAutoStop(false);

        mLocationOverlay.enableMyLocation();
        mLocationOverlay.setOptionsMenuEnabled(true);

        mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);


        map.getOverlays().add(mRotationGestureOverlay);
        map.getOverlays().add(mLocationOverlay);
//        map.getOverlays().add(mCompassOverlay);
        map.getOverlays().add(mScaleBarOverlay);


        checkRoutes();





        final float zoomLevel = mPrefs.getFloat(PREFS_ZOOM_LEVEL_DOUBLE, 7f);
        map.getController().setZoom(zoomLevel);
        final float orientation = mPrefs.getFloat(PREFS_ORIENTATION, 0);
        map.setMapOrientation(orientation, false);
        final String latitudeString = mPrefs.getString(PREFS_LATITUDE_STRING, "52.18d");
        final String longitudeString = mPrefs.getString(PREFS_LONGITUDE_STRING, "19.35d");
        final double latitude = Double.valueOf(latitudeString);
        final double longitude = Double.valueOf(longitudeString);
        map.setExpectedCenter(new GeoPoint(latitude, longitude));

//        centerMap = findViewById(R.id.ic_center_map);
//
//        centerMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "asd", Toast.LENGTH_SHORT).show();
//                if (currentLocation != null) {
//                    Toast.makeText(getApplicationContext(), currentLocation.toString(), Toast.LENGTH_SHORT).show();
//                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
//                    map.getController().animateTo(myPosition);
//                }
//            }
//        });

        followMap = findViewById(R.id.ic_follow);

        followMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mLocationOverlay.isFollowLocationEnabled()) {
                    mLocationOverlay.enableFollowLocation();
                    followMap.setImageResource(R.drawable.osm_ic_follow_me_on);
                } else {
                    mLocationOverlay.disableFollowLocation();
                    followMap.setImageResource(R.drawable.osm_ic_follow_me);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (!mLocationOverlay.isFollowLocationEnabled()) {

            followMap.setImageResource(R.drawable.osm_ic_follow_me);

        }

        return super.dispatchTouchEvent(event);
    }





    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.menu_route1);

        if (!route1.getName().equals("")) {
            item.setTitle(route1.getName());
        } else {
            item.setTitle("Route download error");
        }

        MenuItem item2 = menu.findItem(R.id.menu_route2);

        if (!route2.getName().equals("")) {
            item2.setTitle(route2.getName());
        } else {
            item2.setTitle("Route download error");
        }



        return super.onPrepareOptionsMenu(menu);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        KmlDocument kmlDocument = new KmlDocument();
        InputStream jsonStream;
        String jsonString = null;
        FolderOverlay kmlOverlay;
        BoundingBox bb;
        KmlFeature.Styler styler = new MyKmlStyler();

        switch (item.getItemId()) {
            case R.id.menu_route1:

                for(int i=0; i<map.getOverlays().size(); i++) {
                    String className = map.getOverlays().get(i).getClass().getName();
                    if (className.equals("org.osmdroid.views.overlay.FolderOverlay")){
                        map.getOverlays().remove(i);
                        break;
                    }
                }

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

                if (mActionMode != null) {
                    return false;
                }
                menu_route = route1.getName();
                mActionMode = startSupportActionMode(mActionModeCallback);





                return true;




            case R.id.menu_route2:

                for(int i=0; i<map.getOverlays().size(); i++) {
                    String className = map.getOverlays().get(i).getClass().getName();
                    if (className.equals("org.osmdroid.views.overlay.FolderOverlay")){
                        map.getOverlays().remove(i);
                        break;
                    }
                }

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
                kmlOverlay.setName("asd2");
                map.getOverlays().add(kmlOverlay);
                map.invalidate();
                bb = kmlDocument.mKmlRoot.getBoundingBox();
                bb.increaseByScale(1.5f);
                map.zoomToBoundingBox(bb, true);

                if (mActionMode != null) {
                    return false;
                }
                menu_route = route2.getName();
                mActionMode = startSupportActionMode(mActionModeCallback);

                return true;


            case R.id.menu_change:

                getRoutes();
                return true;


            case R.id.menu_clear:
                for(int i=0; i<map.getOverlays().size(); i++) {
                    String className = map.getOverlays().get(i).getClass().getName();
                    if (className.equals("org.osmdroid.views.overlay.FolderOverlay")){
                        map.getOverlays().remove(i);
                        break;
                    }
                }

                map.invalidate();
                return true;

            case R.id.menu_logout:
                finish();

                return true;


            case R.id.menu_about:

                ImageView image = new ImageView(this);
                image.setImageResource(R.drawable.eovalue);

                new AlertDialog.Builder(activity)
                        .setView(image)
                        .setTitle("About")
                        .setMessage("This application has been developed within the EOVALUE project, which has received funding from the European Union’s Horizon 2020 research and innovation programme. The JRC, or as the case may be the European Commission, shall not be held liable for any direct or indirect, incidental, consequential or other damages, including but not limited to the loss of data, loss of profits, or any other financial loss arising from the use of this application, or inability to use it, even if the JRC is notified of the possibility of such\n" +
                                "damages.")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        //.setNegativeButton(android.R.string.no, null)
                        //.setIcon(R.drawable.eovalue)
                        .show();

                return true;


            default:

                return super.onOptionsItemSelected(item);

        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.submenu, menu);
            mode.setTitle(menu_route);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.submenu_find:
                    Toast.makeText(activity,"submenu_find", Toast.LENGTH_SHORT).show();

                    return true;
                case R.id.submenu_rate:
                    Toast.makeText(activity,"submenu_rate", Toast.LENGTH_SHORT).show();

                    return true;
                case R.id.submenu_report:
                    Toast.makeText(activity,"submenu_report", Toast.LENGTH_SHORT).show();

                    return true;
                default:
                    return false;

            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            menu_route = "";

        }
    };

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

    }

    public void onPause(){

        //save the current location
        final SharedPreferences.Editor edit = mPrefs.edit();
        edit.putFloat(PREFS_ORIENTATION, map.getMapOrientation());
        edit.putString(PREFS_LATITUDE_STRING, String.valueOf(map.getMapCenter().getLatitude()));
        edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(map.getMapCenter().getLongitude()));
        edit.putFloat(PREFS_ZOOM_LEVEL_DOUBLE, (float) map.getZoomLevelDouble());
        edit.apply();

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
            for (int i = 0; i < routesArray.length(); i++) {
                numbers.add(i);
            }

            Collections.shuffle(numbers);

        }

        route1 = getRouteQuery("dict"+numbers.get(0));
        route2 = getRouteQuery("dict"+numbers.get(1));



        numbers.remove(0);
        numbers.remove(0);


    }

    public class Route {
        public String name;
        private String values;
        private String ids;
        private int count;

        private Route() {
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
            this.name = name.replace("dict","Route ");
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        private InputStream getValues() {
            return new ByteArrayInputStream(values.getBytes(Charset.forName("UTF-8")));
        }

        private void setValues(String values) {
            this.values = values;
        }

        public String getIds() {
            return ids;
        }

        private void setIds(String ids) {
            this.ids = ids;


        }

    }

    private void checkRoutes() {




        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://vps510297.ovh.net:3123/api/routes/";



        JsonObjectRequest getRouteRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.optString("success").equals("true")) {

                            try{


                                routesArray = response.getJSONArray("route");
                                //Toast.makeText(getApplicationContext(), routesArray.toString(), Toast.LENGTH_SHORT).show();
                                for (int i = 0; i < routesArray.length(); i++) {
                                    numbers.add(i);
                                }

                                Collections.shuffle(numbers);

                                getRoutes();



                            }catch (JSONException e){
                                Log.d("Error", e.toString());
                            }
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();

                    }



                }
        );

        queue.add(getRouteRequest);

    }


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

