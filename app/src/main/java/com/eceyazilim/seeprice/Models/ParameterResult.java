package com.eceyazilim.seeprice.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ParameterResult {

    @SerializedName("firmname")
    @Expose
    private String firmname;

    public String getFirmname() {
        return firmname;
    }

    public void setFirmname(String firmname) {
        this.firmname = firmname;
    }

}