package com.eceyazilim.seeprice;

import com.eceyazilim.seeprice.Models.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestInterface
{
    @POST("parameter")
    Call<Parameter> getParameter();

    @POST("query_1")
    Call<Query> query(@Body QueryRequest barcode);
}
