package com.example.wisatasumbar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import okhttp3.HttpUrl;

public class Tentang extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang);

        String webText = String.valueOf(Html.fromHtml(
                "<![CDATA[<body style=\"text-align:justify;font-size:14px;margin:0px\"><p style=\"text-indent: 10px\">"
                        +"Aplikasi Singgah Ka Sumbar merupakan aplikasi" +
                        "yang dapat membantu wisatawan dalam mengenal wisata yang ada di Sumatera Barat." +
                        "Aplikasi ini menyediakan fitur yang dapat membantu para wisatawan mencari wisata terdekat." +
                        "Aplikasi ini juga menyediakan fitur pencarian lokasi wisata di Sumatera Barat." +
                        "Aplikasi ini memiliki informasi tentang wisata yang ada di Sumatera Barat."+"</p></body>]]>"
        ));
    }
    public void formGoogle(View view){
        Intent browserForm = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSe1jJ_6o8OWca1LvS-Xjn-Y8eeV8f7lvEFkgjzSbV9cMM_BSA/viewform?usp=sf_link"));
        startActivity(browserForm);
    }
}
