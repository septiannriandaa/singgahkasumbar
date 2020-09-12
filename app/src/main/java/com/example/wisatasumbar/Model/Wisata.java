package com.example.wisatasumbar.Model;

public class Wisata {

    private static final double EARTH_EQUATORIAL_RADIUS = 6378.13780D;
    private static final double CONVERT_DEGREES_TO_DECIMAL = Math.PI/180;
    private static final double CONVERT_KM_TO_MILES = 0.621371;

    private String lokasi;
    private String namaWisata;
    private String wisataId;
    private String deskripsi;
    private String foto;
    private double latitude;
    private double longtitude;



    public Wisata() {
    }

    public Wisata(String lokasi, String namaWisata, String wisataId, String deskripsi, String foto, double latitude, double longtitude) {
        this.lokasi = lokasi;
        this.namaWisata = namaWisata;
        this.wisataId = wisataId;
        this.deskripsi = deskripsi;
        this.foto = foto;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public Wisata(String wisataId, double latitude, double longtitude) {
        this.wisataId = wisataId;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getNamaWisata() {
        return namaWisata;
    }

    public void setNamaWisata(String namaWisata) {
        this.namaWisata = namaWisata;
    }

    public String getWisataId() {
        return wisataId;
    }

    public void setWisataId(String wisataId) {
        this.wisataId = wisataId;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String fotoLokasi) {
        this.foto = fotoLokasi;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double measureDistance(Wisata wisata){
        double deltaLatitude = wisata.getLatitude() - this.getLatitude();
        double deltaLongtitude = wisata.getLongtitude() - this.getLongtitude();

        double a = Math.pow(Math.sin(deltaLatitude / 2D),2D) +
                Math.cos(this.getLatitude()) * Math.cos(wisata.getLatitude()) *
                        Math.pow(Math.sin(deltaLongtitude / 2D),2D);
        return CONVERT_KM_TO_MILES * EARTH_EQUATORIAL_RADIUS * 2D * Math.atan2(Math.sqrt(a),Math.sqrt(1D-a));
    }
}
