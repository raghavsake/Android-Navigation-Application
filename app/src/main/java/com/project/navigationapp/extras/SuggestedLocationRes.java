package com.project.navigationapp.extras;

import com.google.gson.annotations.SerializedName;

public class SuggestedLocationRes {

    @SerializedName("placeAddress")
    private String placeAddress;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("placeName")
    private String placeName;

    @SerializedName("alternateName")
    private String alternateName;

    public String getPlaceAddress() {
        return placeAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

}
