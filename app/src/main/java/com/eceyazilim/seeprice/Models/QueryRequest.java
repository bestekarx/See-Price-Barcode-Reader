package com.eceyazilim.seeprice.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QueryRequest
{

    @SerializedName("barcode")
    @Expose
    private String barcode;
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
