package uniosun.geofence.network;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Morph-Deji on 8/16/2016.
 */

/**
 * Retrofit client config
 */
public class GeofenceClient {


    private WebService service;

    public GeofenceClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://104.236.245.200/api/v1/")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .build();

        service = retrofit.create(WebService.class);
    }

    public WebService getService() {
        return service;
    }

}
