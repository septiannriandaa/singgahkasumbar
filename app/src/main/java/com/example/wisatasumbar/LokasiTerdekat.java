package com.example.wisatasumbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.wisatasumbar.DirectionHelpers.FetchURL;
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
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LokasiTerdekat extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GeoQueryEventListener, IOnLoadLocationListene, TaskLoadedCallback {
    private static final Object REQUEST_LOCATION = 112;
    private GoogleMap mMap;
    private DatabaseReference mLokasi,lokasiUser,ref;
    private Marker marker,currentUser;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private GeoFire geoFire;
    private MarkerOptions lokasiTujuan;
    private List<LatLng> areaWisata;
    private IOnLoadLocationListene listener;
    private Polyline currentPolyline;
    ArrayList<Wisata> listt = new ArrayList<>();
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lokasi_terdekat);

        buildLocationCallback();
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LokasiTerdekat.this);
        mLokasi = FirebaseDatabase.getInstance().getReference().child("dataWisata");
        ref = FirebaseDatabase.getInstance().getReference().child("dataWisata");
        mLokasi.push().setValue(marker);
        initArea();
        settingGeoFire();


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Wisata wisata = ds.getValue(Wisata.class);
                        listt.add(wisata);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void myLocation(View v){
        LocationManager lm = (LocationManager)
                getSystemService(Context. LOCATION_SERVICE ) ;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }

        if (ContextCompat.checkSelfPermission(LokasiTerdekat.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(LokasiTerdekat.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(LokasiTerdekat.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(LokasiTerdekat.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this)
                    .setMessage("Silahkan Aktifkan Lokasi Anda")
                    .setPositiveButton("Settings", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        else{
            if(currentUser != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser.getPosition(), 15.0f));
            }else {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(LokasiTerdekat.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
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
                        }
                    });
                }
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng awal = new LatLng(-0.661100,100.513922);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(awal,8));

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper() );
        for(LatLng latLng : areaWisata){
            mMap.addCircle(new CircleOptions().center(latLng)
                    .radius(5000)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f));

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),5f);
            geoQuery.addGeoQueryEventListener(LokasiTerdekat.this);
        }


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

    public void showAlert(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Ada wisata di dekat Anda");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.create().show();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Ada wisata di dekat Anda");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.create().show();
    }

    @Override
    public void onKeyExited(String key) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Tidak Ada wisata di dekat Anda");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.create().show();
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

    public void lokTer(View v){
//        Log.d("MyLog","Lat is: "+currentUser.getPosition().latitude + ", " +
        locationEnabled();

    }

    private void locationEnabled () {
        LocationManager lm = (LocationManager)
                getSystemService(Context. LOCATION_SERVICE );
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }

        if (ContextCompat.checkSelfPermission(LokasiTerdekat.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(LokasiTerdekat.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(LokasiTerdekat.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(LokasiTerdekat.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this)
                    .setMessage("Silahkan Aktifkan Lokasi Anda")
                    .setPositiveButton("Settings", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        }else{
            if(currentUser != null){
                simpleHillClimbing();
            }else {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
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
                .setSmallIcon(R.mipmap.icon_app)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_app));

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(),notification);
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
        mapFragment.getMapAsync(LokasiTerdekat.this);
    }

    @Override
    public void onLoadLocationFailed(String message) {

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
    private void cobaBaru(){
        if(ref != null){
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String hasilAkhir = null;
                        Integer jarakTujuan = null;
                        LatLng lokasiAwal = new LatLng(currentUser.getPosition().latitude,currentUser.getPosition().longitude);
                        ArrayList<Wisata> list = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Wisata wisata = ds.getValue(Wisata.class);
                            list.add(wisata);
                        }
                        for(int i = 0;i < list.size(); i++){
                            LatLng lokasiTujuan = new LatLng(list.get(i).getLatitude(),list.get(i).getLongtitude());
                            double distance = SphericalUtil.computeDistanceBetween(lokasiAwal, lokasiTujuan);
                            int jarakHitung = (int)distance / 1000;
                            if(jarakTujuan != null){
                                if(jarakHitung <= jarakTujuan){
                                    jarakTujuan = jarakHitung;
                                    hasilAkhir = list.get(i).getNamaWisata();
                                    Log.d("LOG", "Data berubah"+list.get(i).getLatitude()+"+"+list.get(i).getLongtitude());
                                }
                            }else{
                                jarakTujuan = jarakHitung;
                                hasilAkhir = list.get(i).getNamaWisata();
                            }
                        }
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Wisata wisata = ds.getValue(Wisata.class);
                            String lokasi = hasilAkhir;
                            if(lokasi.equalsIgnoreCase(wisata.getNamaWisata())){
                                wisata.getWisataId();
                                LatLng lokasiAkhir = new LatLng(wisata.getLatitude(), wisata.getLongtitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAkhir,15.0f));
                                lokasiTujuan = new MarkerOptions().position(new LatLng(wisata.getLatitude(), wisata.getLongtitude())).title(wisata.getNamaWisata());
                                new FetchURL(LokasiTerdekat.this).execute(getUrl(currentUser.getPosition(), lokasiTujuan.getPosition(), "driving"), "driving");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void simpleHillClimbing(){
        Integer jarakTujuan = null;
        LatLng lokasiAwal = new LatLng(currentUser.getPosition().latitude,currentUser.getPosition().longitude);
        LatLng lokasiAkhir = null;

        for(int i = 0;i < listt.size(); i++){
            LatLng lokasiHitung = new LatLng(listt.get(i).getLatitude(),listt.get(i).getLongtitude());
            double distance = SphericalUtil.computeDistanceBetween(lokasiAwal, lokasiHitung);
            int jarakHitung = (int)distance / 1000;

            if(jarakTujuan != null){
                if(jarakHitung <= jarakTujuan){
                    jarakTujuan = jarakHitung;
                    lokasiAkhir = new LatLng(listt.get(i).getLatitude(),listt.get(i).getLongtitude());
                }
            }else{
                jarakTujuan = jarakHitung;
                lokasiAkhir = new LatLng(listt.get(i).getLatitude(),listt.get(i).getLongtitude());
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAkhir,15.0f));
        lokasiTujuan = new MarkerOptions().position(new LatLng(lokasiAkhir.latitude, lokasiAkhir.longitude));
        new FetchURL(LokasiTerdekat.this).execute(getUrl(currentUser.getPosition(), lokasiTujuan.getPosition(), "driving"), "driving");
    }
    private void methodBaru(){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String hasilAkhir = null;
                Integer jarakTujuan = null;
                LatLng lokasiAwal = new LatLng(currentUser.getPosition().latitude,currentUser.getPosition().longitude);
                LatLng lokasiAkhir = null;
                ArrayList<Wisata> list = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Wisata wisata = ds.getValue(Wisata.class);
                        list.add(wisata);
                    }
                }
                for(int i = 0;i < list.size(); i++){
                    LatLng lokasiHitung = new LatLng(list.get(i).getLatitude(),list.get(i).getLongtitude());
                    double distance = SphericalUtil.computeDistanceBetween(lokasiAwal, lokasiHitung);
                    int jarakHitung = (int)distance / 1000;
                    if(jarakTujuan != null){
                        if(jarakHitung <= jarakTujuan){
                            jarakTujuan = jarakHitung;
//                            hasilAkhir = list.get(i).getNamaWisata();
                            lokasiAkhir = new LatLng(list.get(i).getLatitude(),list.get(i).getLongtitude());
                        }
                    }else{
                        jarakTujuan = jarakHitung;
//                        hasilAkhir = list.get(i).getNamaWisata();
                        lokasiAkhir = new LatLng(list.get(i).getLatitude(),list.get(i).getLongtitude());
                    }
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lokasiAkhir,15.0f));
                lokasiTujuan = new MarkerOptions().position(new LatLng(lokasiAkhir.latitude, lokasiAkhir.longitude));
                new FetchURL(LokasiTerdekat.this).execute(getUrl(currentUser.getPosition(), lokasiTujuan.getPosition(), "driving"), "driving");
//                Log.d("LOG", "Data berubah"+lokasiAkhir);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void lokasiTerdekat(){
        if(ref != null){
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String compare = null;
                        Integer akhir = null;
                        LatLng locationA = new LatLng(currentUser.getPosition().latitude,currentUser.getPosition().longitude);
                        ArrayList<Wisata> list = new ArrayList<>();

                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Wisata wisata = ds.getValue(Wisata.class);
                            list.add(wisata);
                        }
                        for (int i = 0; i < list.size(); i++) {
                            LatLng latLng = new LatLng(list.get(i).getLatitude(),list.get(i).getLongtitude());
                            double distance = SphericalUtil.computeDistanceBetween(locationA, latLng);
                            int hasil = (int)distance / 1000;
                            if(akhir != null){
                                if(hasil <= akhir){
                                    akhir = hasil;
                                    compare = list.get(i).getNamaWisata();
//                                    Log.d("LOG", "Data berubah");
                                }
                            }else{
                                akhir = hasil;
                                compare = list.get(i).getNamaWisata();
//                                Log.d("LOG", "Data Tetap " + iter++);
                            }
//                            Log.d("LOG", "Data Tetap " + compare);
                        }
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Wisata wisata = ds.getValue(Wisata.class);
                            String lokasi = compare;
                            if(lokasi.equalsIgnoreCase(wisata.getNamaWisata())){
                                wisata.getWisataId();
                                LatLng latLng = new LatLng(wisata.getLatitude(), wisata.getLongtitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
                                lokasiTujuan = new MarkerOptions().position(new LatLng(wisata.getLatitude(), wisata.getLongtitude())).title(wisata.getNamaWisata());
                                new FetchURL(LokasiTerdekat.this).execute(getUrl(currentUser.getPosition(), lokasiTujuan.getPosition(), "driving"), "driving");
                            }
                        }
//                        Log.d("LOG", "Loksai "+compare+" dengan jarak " + akhir +" KM");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(LokasiTerdekat.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
