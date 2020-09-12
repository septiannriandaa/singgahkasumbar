package com.example.wisatasumbar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.codesgood.views.JustifiedTextView;
import com.squareup.picasso.Picasso;

import static com.example.wisatasumbar.DaftarWisata.EXTRA_DESKRIPSI;
import static com.example.wisatasumbar.DaftarWisata.EXTRA_FOTLOK;
import static com.example.wisatasumbar.DaftarWisata.EXTRA_LOKASI;
import static com.example.wisatasumbar.DaftarWisata.EXTRA_NAMLOK;

public class HalamanDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_detail);

        Intent intent = getIntent();
        String namaWisata = intent.getStringExtra(EXTRA_NAMLOK);
        String lokasi = intent.getStringExtra(EXTRA_LOKASI);
        String deskripsi = intent.getStringExtra(EXTRA_DESKRIPSI);
        String urlFoto = intent.getStringExtra(EXTRA_FOTLOK);

        ImageView fotoLokasi= findViewById(R.id.fotoLokasi);
        TextView tvNamaWisata = findViewById(R.id.txt_namWis);
        TextView tvLokasi = findViewById(R.id.txt_lok);
//        JustifiedTextView tvDeskripsi = findViewById(R.id.txt_deskripsi);
        WebView tvDeskripsi = findViewById(R.id.txt_deskripsi);

        Picasso.with(this).load(urlFoto).fit().centerInside().into(fotoLokasi);
        tvNamaWisata.setText(namaWisata);
        tvLokasi.setText("Lokasi : " + lokasi);
//        tvDeskripsi.setText(deskripsi);

        String webText = String.valueOf(Html.fromHtml(
                "<![CDATA[<body style=\"text-align:justify;font-size:14px;margin:0px\"><p>"
                +deskripsi+"</p></body>]]>"
        ));
        tvDeskripsi.setBackgroundColor(Color.TRANSPARENT);
        tvDeskripsi.loadData(webText,"text/html;charset=utf-8","UTF-8");
    }
}
