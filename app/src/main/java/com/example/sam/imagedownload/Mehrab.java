package com.example.sam.imagedownload;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sam on 11/8/2014.
 */
public class Mehrab {
    @SerializedName("Latitude")
    double latitude;
    @SerializedName("Longitude")
    double longitude;
    @SerializedName("Name")
    String name;
    @SerializedName("ID")
    int id;
    @SerializedName("MosqueGallaries")
    MosqueGallaries[]mosqueGallarieses;
    public class MosqueGallaries{
        @SerializedName("Image")
        String imageUrl;
    }
}

