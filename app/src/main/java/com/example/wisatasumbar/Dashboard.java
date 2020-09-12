package com.example.wisatasumbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class Dashboard extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 712;
    List<AuthUI.IdpConfig> providers;

    DatabaseReference currentUserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        showSignInOptions();
//        if (ContextCompat.checkSelfPermission(Dashboard.this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            if (ActivityCompat.shouldShowRequestPermissionRationale(Dashboard.this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)){
//                ActivityCompat.requestPermissions(Dashboard.this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }else{
//                ActivityCompat.requestPermissions(Dashboard.this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }
//        }
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                           int[] grantResults){
//        switch (requestCode){
//            case 1: {
//                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    if (ContextCompat.checkSelfPermission(Dashboard.this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
//                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                    }
//                }else{
//                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//        }
//    }
    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .setTheme(R.style.MyTheme)
                        .build(),MY_REQUEST_CODE
        );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,""+response.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onDataWisata(View v){
        Intent intent = new Intent(Dashboard.this, DaftarWisata.class);
        startActivity(intent);
    }
    public void onWisata(View v){
        Intent intent = new Intent(Dashboard.this, LokasiTerdekat.class);
        startActivity(intent);
    }
    public void onCari(View v){
        Intent intent = new Intent(Dashboard.this, CariLokasi3.class);
        startActivity(intent);
    }
    public void onTentang(View v){
        Intent intent = new Intent(Dashboard.this, Tentang.class);
        startActivity(intent);
    }
    public void logout(View v){
        currentUserRef = FirebaseDatabase.getInstance().getReference("infoUser").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserRef.removeValue();
        AuthUI.getInstance().signOut(Dashboard.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showSignInOptions();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Dashboard.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
