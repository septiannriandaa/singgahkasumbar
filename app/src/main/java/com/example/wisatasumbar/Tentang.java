package com.example.wisatasumbar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import okhttp3.HttpUrl;

public class Tentang extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang);
    }
    public void formGoogle(View view){
        Intent browserForm = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSe1jJ_6o8OWca1LvS-Xjn-Y8eeV8f7lvEFkgjzSbV9cMM_BSA/viewform?usp=sf_link"));
        startActivity(browserForm);
    }
}
