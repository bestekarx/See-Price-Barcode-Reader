package com.eceyazilim.seeprice.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QueryResult {

    @SerializedName("productname")
    @Expose
    private String productname;
    @SerializedName("stockcode")
    @Expose
    private String stockcode;
    @SerializedName("pricelist")
    @Expose
    private Double pricelist;
    @SerializedName("price")
    @Expose
    private Double price;

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getStockcode() {
        return stockcode;
    }

    public void setStockcode(String stockcode) {
        this.stockcode = stockcode;
    }

    public Double getPricelist() {
        return pricelist;
    }

    public void setPricelist(Double pricelist) {
        this.pricelist = pricelist;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

}