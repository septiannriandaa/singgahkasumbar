package com.example.wisatasumbar;

import com.example.wisatasumbar.Model.Wisata;

import java.util.List;

public interface IOnLoadLocationListene {
    void onLoadLocationSuccess(List<Wisata> latLngs);
    void onLoadLocationFailed(String message);
}
