package com.example.wisatasumbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.wisatasumbar.DirectionHelpers.FetchURL;
import com.example.wisatasumbar.DirectionHelpers.GetDirectionsData;
import com.example.wisatasumbar.DirectionHelpers.TaskLoadedCallback;
import com.example.wisatasumbar.Model.Wisata;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CariLokasi extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GeoQueryEventListener, IOnLoadLocationListene, TaskLoadedCallback {
    private GoogleMap mMap;
    private SearchView searchView;
    private DatabaseReference mLokasi,lokasiUser;
    private Marker marker,currentUser;
    private MarkerOptions lokasiTujuan;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private IOnLoadLocationListene listener;
    private GeoFire geoFire;
    private List<LatLng> areaWisata;
    private Polyline currentPolyline;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_lokasi);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        buildLocationCallback();
        updateLocation();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CariLokasi.this);
        mLokasi = FirebaseDatabase.getInstance().getReference().child("dataWisata");
        mLokasi.push().setValue(marker);
        initArea();
        settingGeoFire();
        searchView = findViewById(R.id.s_lokasi);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mLokasi.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot s : dataSnapshot.getChildren()) {
                            Wisata wisata = s.getValue(Wisata.class);
                            String location = searchView.getQuery().toString();
                            if(location.equalsIgnoreCase(wisata.getNamaWisata())){
                                wisata.getWisataId();
                                LatLng latLng = new LatLng(wisata.getLatitude(), wisata.getLongtitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
                                lokasiTujuan = new MarkerOptions().position(new LatLng(wisata.getLatitude(), wisata.getLongtitude())).title(wisata.getNamaWisata());
                                new FetchURL(CariLokasi.this).execute(getUrl(currentUser.getPosition(), lokasiTujuan.getPosition(), "driving"), "driving");
                                ukurJarak();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
    public void myLocation(View v){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser.getPosition(),15.0f));
    }
    private void settingGeoFire() {
        lokasiUser = FirebaseDatabase.getInstance().getReference().child("infoUser").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        geoFire = new GeoFire(lokasiUser);
    }
    private void initArea(){
        listener = this;
        FirebaseDatabase.getInstance().getReference().child("dataWisata")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Wisata> latLngList = new ArrayList<>();
                        for(DataSnapshot s : dataSnapshot.getChildren()){
                            Wisata latLng = s.getValue(Wisata.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(final LocationResult locationResult){
                if(mMap != null){
                    geoFire.setLocation("Lokasi Anda", new GeoLocation(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if(currentUser != null) currentUser.remove();
                            currentUser = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                            locationResult.getLastLocation().getLongitude())).title("Lokasi Anda"));

                            Log.d("MyLog","Lat is: "+locationResult.getLastLocation().getLatitude() + ", " +
                                    "Lng is: "+locationResult.getLastLocation().getLongitude());
                        }
                    });
                }
            }
        };

    }
    private void updateLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        buildLocationRequest();
//        fusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent());
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        /*locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);*/
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

        googleMap.setOnMarkerClickListener(this);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mLokasi.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    Wisata wisata = s.getValue(Wisata.class);
                    LatLng location = new LatLng(wisata.getLatitude(), wisata.getLongtitude());
                    mMap.addMarker(new MarkerOptions().position(location).icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).title(wisata.getNamaWisata()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper() );
        for(LatLng latLng : areaWisata){
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(5000)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f));

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),5f);
            geoQuery.addGeoQueryEventListener(CariLokasi.this);
        }
    }
    public void ukurJarak(){
        url = getUrl(currentUser.getPosition(), lokasiTujuan.getPosition(), "driving");
        Object dataTransfer[] = new Object[3];
        GetDirectionsData getDirectionsData = new GetDirectionsData();
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = new LatLng(lokasiTujuan.getPosition().latitude, lokasiTujuan.getPosition().longitude);
        getDirectionsData.execute(dataTransfer);
    }
    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onLoadLocationSuccess(List<Wisata> latLngs) {
        areaWisata = new ArrayList<>();
        for(Wisata wisata : latLngs){
            LatLng convert = new LatLng(wisata.getLatitude(),wisata.getLongtitude());
            areaWisata.add(convert);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(CariLokasi.this);
    }

    @Override
    public void onLoadLocationFailed(String message) {

    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("Wisata Sumbar",String.format("Ada lokasi wisata di dekat Anda",key));
    }

    @Override
    public void onKeyExited(String key) {
        sendNotification("Wisata Sumbar",String.format("Tidak ada lokasi wisata di dekat Anda",key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }
    private void sendNotification(String title, String content) {
        Toast.makeText(this,""+content,Toast.LENGTH_SHORT).show();
        String NOTFICATION_CHANNEL_ID = "wisata_multiple_location";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTFICATION_CHANNEL_ID,"My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(),notification);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public PendingIntent getPendingIntent() {
        Intent intent = new Intent(this,MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
