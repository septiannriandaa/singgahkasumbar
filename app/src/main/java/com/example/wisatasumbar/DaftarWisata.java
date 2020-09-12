package com.example.wisatasumbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.wisatasumbar.Model.Wisata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DaftarWisata extends AppCompatActivity implements AdapterClass.OnClickListener {
    public static final String EXTRA_NAMLOK = "namaWisata";
    public static final String EXTRA_LOKASI = "lokasi";
    public static final String EXTRA_WISATAID = "wisataId";
    public static final String EXTRA_DESKRIPSI = "deskripsi";
    public static final String EXTRA_FOTLOK = "fotoLokasi";

    DatabaseReference ref;
    ArrayList<Wisata> list;
    RecyclerView recyclerView;
    AdapterClass adapterClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_wisata);

        ref = FirebaseDatabase.getInstance().getReference().child("dataWisata");
        recyclerView = findViewById(R.id.rv);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ref != null){
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        list = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            list.add(ds.getValue(Wisata.class));
                        }
                        adapterClass = new AdapterClass(list);
                        recyclerView.setAdapter(adapterClass);
                        adapterClass.setOnItemClickListener(DaftarWisata.this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DaftarWisata.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, HalamanDetail.class);
        Wisata clickedItem = list.get(position);

        detailIntent.putExtra(EXTRA_NAMLOK,clickedItem.getNamaWisata());
        detailIntent.putExtra(EXTRA_LOKASI,clickedItem.getLokasi());
        detailIntent.putExtra(EXTRA_WISATAID,clickedItem.getWisataId());
        detailIntent.putExtra(EXTRA_FOTLOK,clickedItem.getFoto());
        detailIntent.putExtra(EXTRA_DESKRIPSI,clickedItem.getDeskripsi());

        startActivity(detailIntent);
    }
}
