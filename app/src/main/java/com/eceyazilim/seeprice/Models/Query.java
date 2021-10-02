package com.eceyazilim.seeprice.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Query {

    @SerializedName("Result")
    @Expose
    private QueryResult result;

    public QueryResult getResult() {
        return result;
    }

    public void setResult(QueryResult result) {
        this.result = result;
    }

}