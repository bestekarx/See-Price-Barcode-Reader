package com.eceyazilim.seeprice.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Parameter {

    @SerializedName("Result")
    @Expose
    private ParameterResult result;

    public ParameterResult getResult() {
        return result;
    }

    public void setResult(ParameterResult result) {
        this.result = result;
    }

}