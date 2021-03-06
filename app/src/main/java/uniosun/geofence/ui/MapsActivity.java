package uniosun.geofence.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import uniosun.geofence.R;
import uniosun.geofence.model.SimpleGeofence;
import uniosun.geofence.model.SimplePoint;
import uniosun.geofence.services.GeofenceTransitionsIntentService;
import uniosun.geofence.util.Utilities;

import static uniosun.geofence.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int CONST_GEOFENCE_REQUEST = 0x01;
    private static final int CONST_LOCATION_REQUEST = 0x02;
    /*Handler to manage map camera changes*/
    private static final int MESSAGE_CAMERA_UPDATE = 0x1;
    private static final int MESSAGE_NO_INTERNET = 0x2;
    private static final long QUERY_UPDATE_DELAY_MILLIS = 2000;
    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> mGeofenceList;
    // [END define_database_reference]
    List<SimpleGeofence> mSimpleGeofences;
    private GoogleMap mMap;
    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mApiClient;
    private Location mCurrentLocation;
    private View mCenterMarkerLayout;
    /*Progressbar and marker to indicate on camera update at bottom bar*/
    private ProgressBar mCameraFocusProgressBar;

    //private InteractionListener mListener;
    private View mCameraFocusImage;
    /*Text view to hold map focus*/
    private TextSwitcher mCameraFocusTextSwitcher;
    private Location mMapCenter;
    private String mCenterDescription;
    private LocationSearchHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mCameraFocusProgressBar = (ProgressBar) findViewById(R.id.mapFocusProgress);
        mCameraFocusImage = findViewById(R.id.mapFocusImage);
        mCenterMarkerLayout = findViewById(R.id.center_marker_layout);


        mCameraFocusTextSwitcher = (TextSwitcher) findViewById(R.id.camera_focus_switcher);
        mCameraFocusTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(MapsActivity.this);
                return t;
            }
        });

        mHandler = new LocationSearchHandler(this);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        getGeofences(mDatabase);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Rather than displayng this activity, simply display a toast indicating that the geofence
        // service is being created. This should happen in less than a second.
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //.enableAutoManage(this, this)
                .build();

        /*mApiClient.connect();*/

        // Instantiate the current List of geofences.
        mGeofenceList = new ArrayList<>();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);


        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                final CameraPosition cameraFocus = mMap.getCameraPosition();
                requestCameraUpdate(cameraFocus);

            }
        });

        // Add a marker in current place and move the camera
        showMarker();
    }

    private void showMarker() {
        if (mCurrentLocation == null) {
            Log.i(TAG, "Trying to show marker but location not yet available");
            return;
        }
        // Set the map type back to normal.

        Log.i(TAG, "Confident to show marker");
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f));


        // Set the map type back to normal.
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        drawCirlce(5.0);

        if (mSimpleGeofences != null) {
            for (SimpleGeofence geofence :
                    mSimpleGeofences) {
                LatLng latLng1 = new LatLng(geofence.getLatitude(), geofence.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng1).title(geofence.getDescription()));
            }
        }
    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here. A real app might
     * dynamically create geofences based on the user's location.13.5
     */
    public void createGeofences(List<SimpleGeofence> simpleGeofenceList) {
        for (SimpleGeofence simpleGeofence : simpleGeofenceList) {
            if (simpleGeofence.getRadius() <= 0.0f)
                continue;
            mGeofenceList.add(simpleGeofence.toGeofence());
        }


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofences.
        Log.i(TAG, "onConnected called");
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CONST_GEOFENCE_REQUEST);
            return;
        }
        setUpGeofences();
        //Toast.makeText(this, getString(R.string.start_geofence_service), Toast.LENGTH_SHORT).show();

        /*if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        }

        if(mCurrentLocation != null){
            Log.i(TAG, "current location from last location:" + mCurrentLocation.toString());
            showMarker();
        }*/

        if (mCurrentLocation == null) {
            //try request
            Log.i(TAG, "still can't get location, trying to get make request");
            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(10).setInterval(5000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, request, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        /*if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        }*/
    }

    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Both permission response returned");
        if (requestCode == CONST_GEOFENCE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    /*&& grantResults[1] == PackageManager.PERMISSION_GRANTED*/) {
                Log.d(TAG, "Both permissions are granted");
                //getLocation();//TODO do sth now that the permission is available
                setUpGeofences();
            } else {
                Log.d(TAG, "Both permissions not granted");

            }
        }
    }

    private void setUpGeofences() {
        if (!mApiClient.isConnected()) {
            Log.d(TAG, "Can't set up geofences, api client not yet connected");
            return;
        }
        if (mGeofenceList == null || mGeofenceList.isEmpty()) {
            Log.d(TAG, "Can't set up geofences, data not available");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        GeofencingRequest request = new GeofencingRequest.Builder()
                .addGeofences(mGeofenceList)
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
        LocationServices.GeofencingApi.addGeofences(mApiClient, request, mGeofenceRequestIntent);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(TAG, mCurrentLocation.toString());
        if (mCurrentLocation != null) {
            Log.d(TAG, "location from request:" + mCurrentLocation.toString());
            showMarker();
        }
    }

    private void getGeofences(DatabaseReference databaseReference) {
        Query query = databaseReference.child("geofences");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                Log.d(TAG, "singlevalueondatachange:" + dataSnapshot.getValue().toString());

                GenericTypeIndicator<HashMap<String, SimpleGeofence>> typeIndicator =
                        new GenericTypeIndicator<HashMap<String, SimpleGeofence>>() {
                        };

                Collection<SimpleGeofence> geofenceCollection = dataSnapshot.getValue(typeIndicator).values();

                mSimpleGeofences = new ArrayList<>(geofenceCollection);

                addGeofenceMarker();
                createGeofences(mSimpleGeofences);
                setUpGeofences();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /*Map<String, SimpleGeofence> td = (HashMap<String,SimpleGeofence>) dataSnapshot.getValue();
                onGeofenceAvailable(td);*/
                //Log.d(TAG, "childadded:" + dataSnapshot.getValue().getClass());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Log.d(TAG, "ondatachange:" + dataSnapshot.getValue().getClass());

                //Map<String, SimpleGeofence> td = (HashMap<String,SimpleGeofence>) dataSnapshot.getValue();
                //onGeofenceAvailable(td);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void drawCirlce(double radius) {
        CircleOptions options = new CircleOptions()
                .radius(radius)
                .strokeColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
                .center(new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude()));
        mMap.addCircle(options);

    }

    private void addGeofenceMarker() {
        if (mSimpleGeofences == null || mMap == null) return;

        for (SimpleGeofence geofence :
                mSimpleGeofences) {
            LatLng latLng = new LatLng(geofence.getLatitude(), geofence.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(geofence.getDescription()));
        }
    }

    public void addGeofence(View view) {
        SimplePoint point = getLocationDetail();
        Intent intent = new Intent(this, CreateGeofenceActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(CreateGeofenceActivity.POINT_DETAILS, point);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    protected int getSelfNav() {
        return R.id.nav_home;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mApiClient.disconnect();
    }

    private void requestCameraUpdate(CameraPosition cameraPosition) {
        if (mMap == null) return;
        showProgress(true);
        mHandler.removeMessages(MESSAGE_CAMERA_UPDATE);
        if (!Utilities.isOnline(this)) {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_NO_INTERNET, null),
                    QUERY_UPDATE_DELAY_MILLIS);
        } else {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_CAMERA_UPDATE, cameraPosition),
                    QUERY_UPDATE_DELAY_MILLIS);
        }
    }

    private void notifyInternetError() {
        showProgress(false);
        mCameraFocusTextSwitcher.setText(getString(R.string.no_internet));
    }

    private void getAddress(CameraPosition cameraPosition) {
        mMapCenter = new Location(LocationManager.GPS_PROVIDER);
        // set map center
        mMapCenter.setLatitude(cameraPosition.target.latitude);
        mMapCenter.setLongitude(cameraPosition.target.longitude);
        try {
            List<Address> addresses = new Geocoder(this)
                    .getFromLocation(cameraPosition.target.latitude,
                            cameraPosition.target.longitude, 1);
            if (!addresses.isEmpty()) {
                //mMapFocusView.setText(addresses.get(0).getAddressLine(0));
                mCenterDescription = addresses.get(0).getAddressLine(0);
                mCameraFocusTextSwitcher.setText(mCenterDescription);
                showProgress(false);

            } else {
                mCameraFocusTextSwitcher.setText(getString(R.string.loading));
                showProgress(false);
            }

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the progress UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        // do donut animation

        if (mMap == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mCameraFocusProgressBar.setVisibility(View.VISIBLE);
            mCameraFocusProgressBar.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCameraFocusProgressBar.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mCameraFocusImage.setVisibility(View.VISIBLE);
            mCameraFocusImage.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCameraFocusImage.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });

            mCenterMarkerLayout.setVisibility(View.VISIBLE);
            mCenterMarkerLayout.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCenterMarkerLayout.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });


        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mCameraFocusProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mCameraFocusImage.setVisibility(show ? View.GONE : View.VISIBLE);
        }

    }

    private SimplePoint getLocationDetail() {
        SimplePoint point = new SimplePoint();
        point.setLatitude(mMapCenter.getLatitude());
        point.setLongitude(mMapCenter.getLongitude());
        point.setDescription(mCenterDescription);
        return point;
    }

    /**
     * {@code Handler} that sends search queries to the Home.
     */
    private static class LocationSearchHandler extends Handler {

        public static final int MESSAGE_QUERY_UPDATE = 1;

        private final WeakReference<MapsActivity> mFragmentReference;

        LocationSearchHandler(MapsActivity activity) {
            mFragmentReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MapsActivity instance = mFragmentReference.get();
            if (instance != null) {
                if (msg.what == MESSAGE_CAMERA_UPDATE) {
                    CameraPosition cameraPosition = (CameraPosition) msg.obj;
                    instance.getAddress(cameraPosition);
                } else if (msg.what == MESSAGE_NO_INTERNET) {
                    instance.notifyInternetError();
                }
            }
        }

    }

}
