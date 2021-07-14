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

    // 지도 띄우기 변수
    private GoogleMap mMap;
    private boolean mIsRestore;

    private static final LatLng KAIST = new LatLng(36.372151, 127.360594);

    // 지도의 채도와 밝기를 낮추기 위해 사용되는 그라운드 오버레이 이미지
    private GroundOverlay groundOverlay;

    // Heat Map 을 위한 변수 설정
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

    // 현재 위치를 overlay 하기 위한 변수 설정
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

    // 오버레이 업데이트 실시간 위한 변수 설정
    private static final int UPDATE_INTERVAL_MS = 500;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 250; // 0.5초
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private Marker currentMarker = null;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    String[] REQUIRED_PERMISSIONS  = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

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


        // 마커 넣기.
        fbtn_recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fbtn_recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start_clicked){ // start_clicked == true 일 때 화면에 일시정지 표현
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
        // 지도 띄우기
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mIsRestore = savedInstanceState != null;

        // 현재 위치 실시간 반영 수정 START
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // 현재 위치 실시간 반영 수정 END

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
                    // 카메라 초기 위치
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

        // 최소 줌
        mMap.setMinZoomPreference(7.0f);

//        // 카메라 초기 위치
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initLL, 17));

        // 카메라 바운드, 한계 테두리 설정
        LatLngBounds Bounds = new LatLngBounds(
                new LatLng(32.791414, 124.156785), // South west corner
                new LatLng(39.001056, 132.205621)); // North east cornerLatLngBounds Bounds = new LatLngBounds(
        mMap.setLatLngBoundsForCameraTarget(Bounds);

        // 지도의 채도와 밝기를 낮추기 위해 사용되는 그라운드 오버레이 이미지 올리기
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
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( MapActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 현재 오동작을 해서 주석처리

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

    // 지도 띄우기
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
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                //현재 위치에 마커 생성하고 이동
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

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission() && !shared.equals("all"))
                mMap.setMyLocationEnabled(true);
            else {
                mMap.setMyLocationEnabled(false); // 다른 사람의 공유 맵에서는 내위치 굳이 보일 필요 없음.
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

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

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
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";
        */

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(KAIST, 17);
        mMap.moveCamera(cameraUpdate);
    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
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
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        Log.d("yjyj", "MainActivity.java, onRequestPermissionsResult");
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        Log.d("yjyj", "MainActivity.java, showDialogForLocationServiceSetting");

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
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
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }
}
