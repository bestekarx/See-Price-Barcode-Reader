package com.eceyazilim.seeprice;

import android.util.Log;

import com.eceyazilim.seeprice.Models.Parameter;
import com.eceyazilim.seeprice.Models.ParameterResult;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient
{
    public static final String BASE_URL = "https://b0b8b5f3-2d2b-4039-8e78-9ae8174545d5.mock.pstmn.io/";
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit()
    {
        if (retrofit==null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient())
                    .build();
        }

        return retrofit;
    }
}
