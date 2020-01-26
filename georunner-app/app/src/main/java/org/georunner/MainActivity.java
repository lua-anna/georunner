package org.georunner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionMenuView;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;


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

    private SharedPreferences prefs;
    private Activity activity = MainActivity.this;
    private MapView map = null;

    private Toolbar toolbar;
    private Toolbar toolbarBot;
    private Menu botMenu;
    private ActionMode actionMode;
    //private ImageButton centerMap;
    private ImageButton followMap;
    //private Location currentLocation = null;
    private MyLocationNewOverlay locationOverlay;

    private Route menuRoute = new Route();
    private Route menuSimilRoute = new Route();
    private Route route1 = new Route();
    private Route route2 = new Route();
    private Route routeSimil1 = new Route();
    private Route routeSimil2 = new Route();

    private List<Integer> allRoutesCount = new ArrayList<>();
    private List<Integer> similRoutesCount = new ArrayList<>();

    private JSONArray routesArray;
    private JSONArray routesSimilarArray;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection deprecation
        Configuration.getInstance().load(activity, PreferenceManager.getDefaultSharedPreferences(activity));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarBot = findViewById(R.id.toolbarBot);

        botMenu = toolbarBot.getMenu();
        //toolbarBot.setVisibility(View.GONE);
        toolbarBot.setOnMenuItemClickListener(botOnClickListener);


        prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        map = findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setFlingEnabled(false);
        map.setMultiTouchControls(true);
        map.setTilesScaledToDpi(true);
//        myCompassOverlay mCompassOverlay = new myCompassOverlay(this, new InternalCompassOrientationProvider(this), map);
//        mCompassOverlay.enableCompass();
        //private CompassOverlay mCompassOverlay;
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setAlignBottom(true);
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(activity);
        locationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, map);
        locationOverlay.setDrawAccuracyEnabled(true);
        //mMyLocationOverlay.setEnableAutoStop(false);
        locationOverlay.enableMyLocation();
        locationOverlay.setOptionsMenuEnabled(true);
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map);
        rotationGestureOverlay.setEnabled(true);

        map.getOverlays().add(rotationGestureOverlay);
        map.getOverlays().add(locationOverlay);
//        map.getOverlays().add(mCompassOverlay);
        map.getOverlays().add(scaleBarOverlay);


        checkRoutes();


        final float zoomLevel = prefs.getFloat(PREFS_ZOOM_LEVEL_DOUBLE, 12f);
        map.getController().setZoom(zoomLevel);
        final float orientation = prefs.getFloat(PREFS_ORIENTATION, 0);
        map.setMapOrientation(orientation, false);
        final String latitudeString = prefs.getString(PREFS_LATITUDE_STRING, "50.65d");
        final String longitudeString = prefs.getString(PREFS_LONGITUDE_STRING, "17.91d");
        final double latitude = Double.valueOf(latitudeString);
        final double longitude = Double.valueOf(longitudeString);
        map.setExpectedCenter(new GeoPoint(latitude, longitude));

//        centerMap = findViewById(R.id.ic_center_map);
//
//        centerMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(activity, "asd", Toast.LENGTH_SHORT).show();
//                if (currentLocation != null) {
//                    Toast.makeText(activity, currentLocation.toString(), Toast.LENGTH_SHORT).show();
//                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
//                    map.getController().animateTo(myPosition);
//                }
//            }
//        });

        followMap = findViewById(R.id.ic_follow);
        followMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!locationOverlay.isFollowLocationEnabled()) {
                    locationOverlay.enableFollowLocation();
                    followMap.setImageResource(R.drawable.osm_ic_follow_me_on);
                } else {
                    locationOverlay.disableFollowLocation();
                    followMap.setImageResource(R.drawable.osm_ic_follow_me);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!locationOverlay.isFollowLocationEnabled()) {
            followMap.setImageResource(R.drawable.osm_ic_follow_me);
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        botMenu.clear();
        toolbar.inflateMenu(R.menu.menu);

        if (actionMode == null) {
            toolbarBot.setBackgroundColor(getColor(R.color.colorPrimaryDark));

            getMenuInflater().inflate(R.menu.bot_menu, botMenu);

            MenuItem item = botMenu.findItem(R.id.menu_route1);
            if (!route1.getName().equals("")) {
                item.setTitle(route1.getName());
            } else {
                item.setTitle("loading");
            }

            MenuItem item2 = botMenu.findItem(R.id.menu_route2);
            if (!route2.getName().equals("")) {
                item2.setTitle(route2.getName());
            } else {
                item2.setTitle("loading");
            }
        } else {
            toolbarBot.setBackgroundColor(Color.parseColor("#0272a8"));
            getMenuInflater().inflate(R.menu.bot_menu_similar, botMenu);

            MenuItem item = botMenu.findItem(R.id.bot_menu_route1);
            if (!routeSimil1.getName().equals("")) {
                item.setTitle(routeSimil1.getName());
            } else {
                item.setTitle("loading");
            }

            MenuItem item2 = botMenu.findItem(R.id.bot_menu_route2);
            if (!routeSimil2.getName().equals("")) {
                item2.setTitle(routeSimil2.getName());
            } else {
                item2.setTitle("loading");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }


    public Toolbar.OnMenuItemClickListener botOnClickListener = new Toolbar.OnMenuItemClickListener() {

        BoundingBox bb;

        @Override
        public boolean onMenuItemClick (MenuItem item) {
            switch (item.getItemId()) {


                case R.id.menu_route1:
                    clearOverlays("");
                    bb = drawRoute("baseRoute", route1, Color.RED);
                    menuRoute = route1;

                    return true;

                case R.id.menu_route2:
                    clearOverlays("");
                    bb = drawRoute("baseRoute", route2, Color.RED);
                    menuRoute = route2;

                    return true;

                case R.id.menu_accept:
                    if (!menuRoute.getName().equals("")) {
                        if (actionMode != null) {
                            return false;
                        }
                        actionMode = startSupportActionMode(mActionModeCallback);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.choose_route), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM,0,200);
                        toast.show();
                    }

                    return true;

                case R.id.menu_change:
                    clearOverlays("");
                    getRoutes(routesArray);
                    menuRoute = new Route();
                    return true;

                case R.id.bot_menu_rate:
                    Toast toast = Toast.makeText(activity, "submenu_rate: "+menuRoute.getName(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM,0,200);
                    toast.show();
                    return true;

                case R.id.bot_menu_find:
                    getSimilarRoutes(menuRoute.getName());
                    invalidateOptionsMenu();
                    menuSimilRoute = new Route();
                    return true;

                case R.id.bot_menu_route1:
                    clearOverlays("baseRoute");
                    drawRoute("similarRoute", routeSimil1, Color.BLUE);
                    menuSimilRoute = routeSimil1;

                    return true;

                case R.id.bot_menu_route2:
                    clearOverlays("baseRoute");
                    drawRoute("similarRoute", routeSimil2, Color.BLUE);
                    menuSimilRoute = routeSimil2;

                    return true;

                case R.id.bot_menu_change:
                    clearOverlays("baseRoute");
                    getRoutes(routesSimilarArray);
                    menuSimilRoute = new Route();
                    invalidateOptionsMenu();

                    return true;

                case R.id.bot_menu_accept:
                    if (!menuSimilRoute.getName().equals("")) {
                        clearOverlays("");
                        bb = drawRoute("baseRoute", menuSimilRoute, Color.RED);
                        menuRoute = menuSimilRoute;
                        actionMode.invalidate();
                    } else {
                        Toast toast2 = Toast.makeText(getApplicationContext(), getResources().getString(R.string.choose_route), Toast.LENGTH_SHORT);
                        toast2.setGravity(Gravity.BOTTOM,0,200);
                        toast2.show();
                    }

                    return true;

                case R.id.bot_menu_cancel:
                    clearOverlays("baseRoute");
                    actionMode.invalidate();
                    map.zoomToBoundingBox(bb, true);
                    return true;

                default:
                    return false;
            }
        }



    };


    private BoundingBox drawRoute (String routeName, Route route, int color){
        KmlDocument kmlDocument = new KmlDocument();
        InputStream jsonStream;
        String jsonString = null;
        FolderOverlay kmlOverlay;
        BoundingBox bb;
        BoundingBox bbInc;
        KmlFeature.Styler styler = new MyKmlStyler(color);

        try {
            jsonStream = route.getValues();
            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            jsonStream.read(buffer);
            jsonStream.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.d("Error", e.toString());
        }

        kmlDocument.parseGeoJSON(jsonString);
        kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, styler, kmlDocument);
        kmlOverlay.setName(routeName);
        map.getOverlays().add(kmlOverlay);
        map.invalidate();
        bb = kmlDocument.mKmlRoot.getBoundingBox();
        bbInc = bb.increaseByScale(1.3f);
        map.zoomToBoundingBox(bbInc, true);

        return bbInc;
    }


    private void clearOverlays(String routeName){
        Overlay routeOverlay;

        for(int i=0; i<map.getOverlays().size(); i++) {
            routeOverlay = map.getOverlays().get(i);

            if (routeOverlay instanceof FolderOverlay) {
                if (routeName.equals("") || (!((FolderOverlay) routeOverlay).getName().equals(routeName))) {
                    map.getOverlays().remove(i);
                    i = i - 1;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                clearOverlays("");
                menuRoute = new Route();
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
            mode.getMenuInflater().inflate(R.menu.action_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(menuRoute.getName());
            toolbarBot.setBackgroundColor(getColor(R.color.colorPrimaryDark));
            botMenu.clear();
            getMenuInflater().inflate(R.menu.bot_menu_find, botMenu);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){

                case R.id.action_menu_report:
                    Toast toast2 = Toast.makeText(activity, menuRoute.getName()+" reported as broken", Toast.LENGTH_SHORT);
                    toast2.setGravity(Gravity.BOTTOM,0,200);
                    toast2.show();

                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            clearOverlays("baseRoute");
            invalidateOptionsMenu();
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
        super.onPause();

        //save the current location
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(PREFS_ORIENTATION, map.getMapOrientation());
        edit.putString(PREFS_LATITUDE_STRING, String.valueOf(map.getMapCenter().getLatitude()));
        edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(map.getMapCenter().getLongitude()));
        edit.putFloat(PREFS_ZOOM_LEVEL_DOUBLE, (float) map.getZoomLevelDouble());
        edit.apply();

        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void getRoutes(JSONArray mArray) {
        if (actionMode == null) {
            if (allRoutesCount.size()<2){
                allRoutesCount = new ArrayList<>();
                for (int i = 0; i < mArray.length(); i++) {
                    allRoutesCount.add(i);
                }
                Collections.shuffle(allRoutesCount);
            }
            route1 = getRouteQuery("dict"+ allRoutesCount.get(0));
            route2 = getRouteQuery("dict"+ allRoutesCount.get(1));
            allRoutesCount.remove(0);
            allRoutesCount.remove(0);
        } else {
            if (similRoutesCount.size()<2){
                similRoutesCount = new ArrayList<>();
                for (int i = 0; i < mArray.length(); i++) {
                    similRoutesCount.add(i);
                }
                Collections.shuffle(similRoutesCount);
            }
            routeSimil1 = getRouteQuery("dict"+ similRoutesCount.get(0));
            routeSimil2 = getRouteQuery("dict"+ similRoutesCount.get(1));
            similRoutesCount.remove(0);
            similRoutesCount.remove(0);
        }
    }

    @SuppressWarnings("unused")
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
                                getRoutes(routesArray);
                            } catch (JSONException e){
                                Log.d("Error", e.toString());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.d("Error", e.toString());
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
                            //Toast.makeText(activity, response.toString(), Toast.LENGTH_SHORT).show();
                            if (response.optString("success").equals("true")) {
                                route.setName(response.optString("name"));
                                route.setValues(response.optString("values"));
                                route.setIds(response.optString("ids"));

                                invalidateOptionsMenu();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError) {
                                Toast.makeText(activity, getResources().getString(R.string.offline), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof TimeoutError) {
                                Toast.makeText(activity, getResources().getString(R.string.server_offline), Toast.LENGTH_LONG).show();
                            } else if (error instanceof AuthFailureError) {
                                Toast.makeText(activity, "AuthFailureError"+error.toString(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ServerError) {
                                NetworkResponse networkResponse = error.networkResponse;
                                if (networkResponse != null && networkResponse.data != null) {
                                    String jsonError = new String(networkResponse.data);
                                    try {
                                        JSONObject errorResponse = new JSONObject(jsonError);
                                            Toast.makeText(activity, errorResponse.toString(), Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        Log.d("Error", e.toString());
                                    }
                                }
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(activity, "NetworkError"+error.toString(), Toast.LENGTH_SHORT).show();
                            } else if (error instanceof ParseError) {
                                Toast.makeText(activity, "ParseError"+error.toString(), Toast.LENGTH_SHORT).show();
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

    private void getSimilarRoutes(String route) {
        final String routeName = "asd"; // route.replace("Route ","dict");!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        final JSONArray routeArray = new JSONArray();

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://vps510297.ovh.net:3123/api/routes/getSimilar";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("NAME", routeName);

            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response.optString("success").equals("true")) {
                        try {
                            JSONObject route = response.getJSONObject("route");
                            routesSimilarArray = route.optJSONArray("similars");
                            getRoutes(routesSimilarArray);
                        } catch (JSONException e) {
                            Log.d("Error", e.toString());
                        }
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            Log.d("Error", e.toString());
                        }
                    });
            queue.add(loginRequest);
        } catch (JSONException e) {
            Log.d("Error", e.toString());
        }
    }

    @SuppressWarnings("deprecation")
    class MyKmlStyler implements KmlFeature.Styler {

        private int sColor;

        private MyKmlStyler(int color) {
            sColor = color;
        }

        @Override
        public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
        }

        @Override
        public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
        }

        @Override
        public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString){
            polyline.setWidth(8.0f);
            polyline.setColor(sColor);
        }

        @Override
        public void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
        }

        @Override
        public void onTrack(Polyline polyline, KmlPlacemark kmlPlacemark, KmlTrack kmlTrack) {
        }
    }
}

