package com.example.wisatasumbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisatasumbar.Model.Wisata;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Test extends AppCompatActivity {

    DatabaseReference ref;
    ArrayList<Wisata> list;
    RecyclerView recyclerView;
    AdapterClass adapterClass;
    private int seconds = 0;

    // Is the stopwatch running?
    private boolean running;

    private boolean wasRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ref = FirebaseDatabase.getInstance().getReference().child("dataWisata");

        if (savedInstanceState != null) {

            // Get the previous state of the stopwatch
            // if the activity has been
            // destroyed and recreated.
            seconds
                    = savedInstanceState
                    .getInt("seconds");
            running
                    = savedInstanceState
                    .getBoolean("running");
            wasRunning
                    = savedInstanceState
                    .getBoolean("wasRunning");
        }
        runTimer();
    }
    public void onClickStart(View view)
    {
       dataTesting();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    protected void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }
    private void runTimer()
    {

        // Get the text view.
        final TextView timeView
                = (TextView)findViewById(
                R.id.time_view);

        // Creates a new Handler
        final Handler handler
                = new Handler();

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                timeView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }
    public void testJalan(){
        running = true;
    }
    public void dataTesting(){
        ArrayList<Integer> jarak = new ArrayList<Integer>();
        Integer akhir = null;
        for(int i = 0; i < 1000000; i++){
            jarak.add((int) (100 + Math.random()*1000000));
        }
        Log.d("LOG", "Data "+jarak);

        for(int i = 0; i < jarak.size(); i++){
            int hasil = jarak.get(i);
            if(akhir != null){
                if(hasil <= akhir){
                    akhir = hasil;
                }
            }else{
                akhir = hasil;
            }
        }

        Log.d("LOG", "Nilai Terkecil "+akhir);
        Log.d("LOG", "---------------------------------------------------------");

    }
    public void hitungWaktuCari(){
        if(ref != null){
            running = true;
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int iter = 0;
                        String compare = null;
                        Integer akhir = null;
                        LatLng locationA = new LatLng(-1.471292,101.621403);
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
                                    compare = list.get(i).getWisataId();
                                    iter = 0;
                                    Log.d("LOG", "Data berubah");
                                }
                            }else{
                                akhir = hasil;
                                compare = list.get(i).getWisataId();
                                Log.d("LOG", "Data Tetap " + iter++);
                            }
                        }
                        running = false;
                        Log.d("LOG", "Loksai "+compare+" dengan jarak " + akhir +" KM");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Test.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
