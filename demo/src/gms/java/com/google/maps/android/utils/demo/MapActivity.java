package com.google.maps.android.utils.demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    int count;

    TextView textView;
    String coordinates, curr_id, cpns, journey_name;

    FloatingActionButton fbtn_chat, fbtn_recording;
    Animation fromBottom, toBottom, rotateOpen, rotateClose;
    Boolean add_clicked = false;
    Boolean start_clicked = false;

    // ?????? ????????? ??????
    private GoogleMap mMap;
    private boolean mIsRestore;

    private static final LatLng KAIST = new LatLng(36.372151, 127.360594);

    // ????????? ????????? ????????? ????????? ?????? ???????????? ???????????? ???????????? ?????????
    private GroundOverlay groundOverlay;

    // Heat Map ??? ?????? ?????? ??????
    private static final int ALT_HEATMAP_RADIUS = 50;
    private static final double ALT_HEATMAP_OPACITY = 0.2;
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.rgb(116, 0, 184),
            Color.rgb(105, 48, 195),
            Color.rgb(94, 96, 206),
            Color.rgb(78, 168, 222),
            Color.rgb(86, 207, 225),
            Color.rgb(100, 223, 223),
            Color.rgb(114, 239, 221),
            Color.rgb(128, 255, 219),
            Color.rgb(255, 255, 255),
    };
    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
//            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
            0.2f, 0.3f, 0.45f, 0.6f, 0.7f, 0.8f, 0.85f, 0.9f, 0.95f
    };
    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;

    ArrayList<LatLng> heatMap = new ArrayList<>();

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private HashMap<String, MapActivity.DataSet> mLists = new HashMap<>();

    // ?????? ????????? overlay ?????? ?????? ?????? ??????
    private static final String TAG = MainActivity.class.getSimpleName();
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Location location;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // ???????????? ???????????? ????????? ?????? ?????? ??????
    private static final int UPDATE_INTERVAL_MS = 500;  // 1???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 250; // 0.5???
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private Marker currentMarker = null;
    private View mLayout;  // Snackbar ???????????? ???????????? View??? ???????????????.
    // (????????? Toast????????? Context??? ??????????????????.)


    // onRequestPermissionsResult?????? ????????? ???????????? ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ???????????????.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    String[] REQUIRED_PERMISSIONS  = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ?????????

    private ArrayList<String> getLat;
    private ArrayList<String> getLng;


    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    // [START maps_current_place_state_keys]
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // [END maps_current_place_state_keys]

    String shared;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Intent intent = getIntent();
        shared = intent.getExtras().getString("shared");
        coordinates = intent.getExtras().getString("coordinates");
        curr_id = intent.getExtras().getString("curr_id");
        journey_name = intent.getExtras().getString("journey_name");
        cpns = intent.getExtras().getString("cpns");


        fromBottom = AnimationUtils.loadAnimation(MapActivity.this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(MapActivity.this, R.anim.to_bottom_anim);
        rotateOpen = AnimationUtils.loadAnimation(MapActivity.this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(MapActivity.this, R.anim.rotate_close_anim);

        fbtn_chat = findViewById(R.id.fbtn_chat);
        fbtn_recording = findViewById(R.id.fbtn_start);

        if(shared.equals("all")){
            fbtn_chat.setVisibility(View.GONE);
            fbtn_recording.setVisibility(View.GONE);
        }


        // ?????? ??????.
        fbtn_recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fbtn_recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start_clicked){ // start_clicked == true ??? ??? ????????? ???????????? ??????
                    Log.d("yjyj", "MapActivity.java, onClick, in if");
                    start_clicked = false;
                    fbtn_recording.setImageResource(R.drawable.ic_start);
                    upload_coordinates(journey_name, cpns, getLat, getLng);
                } else {
                    start_clicked = true;
                    fbtn_recording.setImageResource(R.drawable.ic_stop);
                    getLat = new ArrayList<>();
                    getLng = new ArrayList<>();
                }
            }
        });

        fbtn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, ChatActivity.class);
                intent.putExtra("journey_name", journey_name);
                intent.putExtra("cpns", cpns);
                intent.putExtra("curr_id", curr_id);
                startActivity(intent);
            }
        });
        // ?????? ?????????
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mIsRestore = savedInstanceState != null;

        // ?????? ?????? ????????? ?????? ?????? START
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // ?????? ?????? ????????? ?????? ?????? END

        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(this);
        // Construct a FusedLocationProviderClient.
        setUpMap();
    }

    private void save_markers(String journey_name, String cpns, String address, String lat, String lng){
        HashMap<String, String> map = new HashMap<>();
        map.put("journey_name", journey_name);

        map.put("id", cpns);

        map.put("address", address);

        map.put("lat", lat);

        map.put("lng", lng);

        Call<Void> call = LoginActivity.retrofitInterface.saveMarkers(map);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void get_markers(String journey_name, String cpns){
        HashMap<String, String> map = new HashMap<>();
        map.put("journey_name", journey_name);

        map.put("id", cpns);

        Call<List<MarkerResult>> call = LoginActivity.retrofitInterface.getMarkers(map);

        call.enqueue(new Callback<List<MarkerResult>>() {
            @Override
            public void onResponse(Call<List<MarkerResult>> call, Response<List<MarkerResult>> response) {
                if (response.code() == 200) {
                    List<MarkerResult> result = response.body();
                    for(int i = 0; i<result.size(); i++){
                        String address = result.get(i).getAddress();
                        String lat = result.get(i).getMarkerLat();
                        String lng = result.get(i).getMarkerLng();
                        LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                        mMap.addMarker(new MarkerOptions().position(latLng).title(address).draggable(true));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MarkerResult>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void update_marker(String journey_name, String cpns, String address, String lat, String lng){
        HashMap<String, String> map = new HashMap<>();
        map.put("journey_name", journey_name);

        map.put("id", cpns);

        map.put("address", address);

        map.put("lat", lat);

        map.put("lng", lng);

        Call<Void> call = LoginActivity.retrofitInterface.updateMarkers(map);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(getApplicationContext(), "updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void get_coordinates(String journey_name, String cpns){
        HashMap<String, List<String>> map = new HashMap<>();
        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayList1.add(journey_name);
        map.put("journey_name", arrayList1);

        String[] temp = cpns.split(", ");
        ArrayList<String> arrayList2 = new ArrayList<>();
        for(int i=0; i<temp.length; i++){
            arrayList2.add(temp[i]);
        }
        map.put("id", arrayList2);

        Call<CoordResult> call = LoginActivity.retrofitInterface.getCoordinates(map);

        call.enqueue(new Callback<CoordResult>() {
            @Override
            public void onResponse(Call<CoordResult> call, Response<CoordResult> response) {
                Log.d("asdfasdf", "yes");
                if (response.code() == 200) {
                    Log.d("asdfasdf", "yes2");
                    CoordResult result = response.body();
                    List<String> coordinates = result.getCoordinates();
                    List<String> lat = result.getLat();
                    List<String> lng = result.getLng();
                    Log.d("asdfasdf", "yes3");

                    for(int j=1; j<coordinates.size(); j++){
                        Log.d("asdfasdf", coordinates.get(j));
                    }

                    LatLng initLL = new LatLng(Double.parseDouble(coordinates.get(0)), Double.parseDouble(coordinates.get(1)));
                    // ????????? ?????? ??????
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initLL, 17));

                    Log.d("asdfasdf", result.getLat().get(0));
                    if(lat.size() != 1){
                        for(int i=0; i<lat.size(); i++){
                            Log.d("asdfasdf", "in for loop");

                            Double tempLat = Double.parseDouble(lat.get(i));
                            Double tempLng = Double.parseDouble(lng.get(i));
                            LatLng tempLL = new LatLng(tempLat, tempLng);
                            heatMap.add(tempLL);
                            if (mProvider == null) {
                                mProvider = new HeatmapTileProvider.Builder().data(heatMap).build(); //data(ArrayList<LatLng>).build();
                                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                            } else {
                                mProvider.setData(heatMap);
                                mOverlay.clearTileCache();
                            }

                            if (mDefaultRadius) {
                                mProvider.setRadius(ALT_HEATMAP_RADIUS);
                            }
                            if (mDefaultGradient) {
                                mProvider.setGradient(ALT_HEATMAP_GRADIENT);
                            }
                            if (mDefaultOpacity) {
                                mProvider.setOpacity(ALT_HEATMAP_OPACITY);
                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CoordResult> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void upload_coordinates(String journey_name, String cpns, ArrayList<String> Lats, ArrayList<String> Lngs){
        HashMap<String, List<String>> map = new HashMap<>();
        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayList1.add(journey_name);
        map.put("journey_name", arrayList1);

        String[] temp = cpns.split(", ");
        ArrayList<String> arrayList2 = new ArrayList<>();
        for(int i=0; i<temp.length; i++){
            arrayList2.add(temp[i]);
        }
        map.put("id", arrayList2);

        ArrayList<String> arrayList3 = new ArrayList<>();
        arrayList3.add(Lats.get(0));
        arrayList3.add(Lngs.get(0));
        map.put("coordinates", arrayList3);

        map.put("lat", Lats);
        map.put("lng", Lngs);

        Call<Void> call = LoginActivity.retrofitInterface.updateCoordinates(map);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    //Toast.makeText(getApplicationContext(), "your journey is updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // [START maps_current_place_on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("yjyj", "MainActivity.java, onSaveInstanceState");
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
    // [END maps_current_place_on_save_instance_state]

    @Override
    public void onMapReady(GoogleMap map) {
        count = 0;
        Log.d("yjyj", "MainActivity.java, onMapReady");
        if (mMap != null) {
            return;
        } else mMap = map;
        mMap.setMyLocationEnabled(true);

        heatMap.clear();
        get_coordinates(journey_name, cpns);

        get_markers(journey_name, cpns);

        // ?????? ???
        mMap.setMinZoomPreference(7.0f);

//        // ????????? ?????? ??????
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initLL, 17));

        // ????????? ?????????, ?????? ????????? ??????
        LatLngBounds Bounds = new LatLngBounds(
                new LatLng(32.791414, 124.156785), // South west corner
                new LatLng(39.001056, 132.205621)); // North east cornerLatLngBounds Bounds = new LatLngBounds(
        mMap.setLatLngBoundsForCameraTarget(Bounds);

        // ????????? ????????? ????????? ????????? ?????? ???????????? ???????????? ???????????? ????????? ?????????
        LatLngBounds overlayBounds = new LatLngBounds(
                new LatLng(28.791414, 121.156785), // South west corner
                new LatLng(43.001056, 135.205621)); // North east corner
        groundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.black))
                .positionFromBounds(overlayBounds)
                .transparency(0.5f));

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            startLocationUpdates(); // 3. ?????? ???????????? ??????

        }else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                        ActivityCompat.requestPermissions( MapActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // ?????? ???????????? ?????? ????????????

        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull @NotNull LatLng latLng) {
                if(!shared.equals("all")) {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    String address = "";
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addressList != null && addressList.size() > 0) {
                            if (addressList.get(0).getThoroughfare() != null) {
                                address += addressList.get(0).getThoroughfare();
                                if (addressList.get(0).getSubThoroughfare() != null) {
                                    address += addressList.get(0).getSubThoroughfare();
                                }
                            }
                        }

                        save_markers(journey_name, cpns, address, Double.toString(latLng.latitude), Double.toString(latLng.longitude));

                    } catch (Exception e) {}
                    mMap.addMarker(new MarkerOptions().position(latLng).title(address).draggable(true));
                    CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom);
                    mMap.animateCamera(loc);
                }
            }
        });
    }




    /**
     * Helper class - stores data sets and sources.
     */
    private class DataSet {
        private ArrayList<LatLng> mDataset;

        public DataSet(ArrayList<LatLng> dataSet) {
            this.mDataset = dataSet;
        }

        public ArrayList<LatLng> getData() {
            return mDataset;
        }
    }

    private void setUpMap() {
        Log.d("yjyj", "MainActivity.java, setUpMap");
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    protected void startDemo(boolean isRestore) {
        Log.d("yjyj", "MainActivity.java, startDemo");
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(KAIST, 17));
        }
    }

    public void changeRadius(View view) {
        if (mDefaultRadius) {
            mProvider.setRadius(ALT_HEATMAP_RADIUS);
        } else {
            mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
        }
        mOverlay.clearTileCache();
        mDefaultRadius = !mDefaultRadius;
    }

    public void changeGradient(View view) {
        Log.d("yjyj", "HeatmapsDemoActivity.java, changeGradient");
        if (mDefaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
        } else {
            mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT);
        }
        mOverlay.clearTileCache();
        mDefaultGradient = !mDefaultGradient;
    }

    public void changeOpacity(View view) {
        Log.d("yjyj", "HeatmapsDemoActivity.java, changeOpacity");
        if (mDefaultOpacity) {
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
        } else {
            mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
        }
        mOverlay.clearTileCache();
        mDefaultOpacity = !mDefaultOpacity;
    }

    protected GoogleMap getMap() {
        Log.d("yjyj", "MainActivity.java, getMap");
        return mMap;
    }

    // ?????? ?????????
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d("yjyj", "MainActivity.java, onLocationResult");
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "??????:" + String.valueOf(location.getLatitude())
                        + " ??????:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                //?????? ????????? ?????? ???????????? ??????
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
                Log.d("yjyj", "*** Lat: " + mCurrentLocatiion.getLatitude() +
                        ", Long: " + mCurrentLocatiion.getLongitude());

                if(start_clicked) {
                    LatLng temp = new LatLng(mCurrentLocatiion.getLatitude(), mCurrentLocatiion.getLongitude());
                    heatMap.add(temp);
                    getLat.add(String.valueOf(location.getLatitude()));
                    getLng.add(String.valueOf(location.getLongitude()));

                    if (mProvider == null) {
                        mProvider = new HeatmapTileProvider.Builder().data(heatMap).build(); //data(ArrayList<LatLng>).build();
                        mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    } else {
                        mProvider.setData(heatMap);
                        mOverlay.clearTileCache();
                    }

                    if (mDefaultRadius) {
                        mProvider.setRadius(ALT_HEATMAP_RADIUS);
                    }
                    if (mDefaultGradient) {
                        mProvider.setGradient(ALT_HEATMAP_GRADIENT);
                    }
                    if (mDefaultOpacity) {
                        mProvider.setOpacity(ALT_HEATMAP_OPACITY);
                    }
                }
            }

        }
    };

    private void startLocationUpdates() {
        Log.d("yjyj", "MainActivity.java, startLocationUpdates");

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : ????????? ???????????? ??????");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission() && !shared.equals("all"))
                mMap.setMyLocationEnabled(true);
            else {
                mMap.setMyLocationEnabled(false); // ?????? ????????? ?????? ???????????? ????????? ?????? ?????? ?????? ??????.
            }
        }
    }


    @Override
    protected void onStart() {
        Log.d("yjyj", "MainActivity.java, onStart");
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    protected void onStop() {
        Log.d("yjyj", "MainActivity.java, onStop");

        super.onStop();

        if (fusedLocationProviderClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {
        Log.d("yjyj", "MainActivity.java, getCurrentAddress");

        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus() {
        Log.d("yjyj", "MainActivity.java, checkLocationServicesStatus");
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        Log.d("yjyj", "MainActivity.java, setCurrentLocation");
        if (currentMarker != null) currentMarker.remove();
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(count == 0) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mMap.moveCamera(cameraUpdate);
        }
        count++;
    }


    public void setDefaultLocation() {
        Log.d("yjyj", "MapActivity.java, setDefaultLocation");

        /*
        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????";
        */

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(KAIST, 17);
        mMap.moveCamera(cameraUpdate);
    }


    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {
        Log.d("yjyj", "MainActivity.java, checkPermission");
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }



    /*
     * ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        Log.d("yjyj", "MainActivity.java, onRequestPermissionsResult");
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;

            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                // ???????????? ??????????????? ?????? ??????????????? ???????????????.
                startLocationUpdates();
            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {
                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {
        Log.d("yjyj", "MainActivity.java, showDialogForLocationServiceSetting");

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("yjyj", "MainActivity.java, onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }
}
