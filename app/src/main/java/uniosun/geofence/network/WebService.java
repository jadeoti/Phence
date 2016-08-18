package uniosun.geofence.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Morph-Deji on 7/25/2016.
 */

public interface WebService {


    @FormUrlEncoded
    @POST("sendsms")
    Call<JsonObject> sendSms(@Field("token") String token,
                             @Field("sender") String sender,
                             @Field("message") String message,
                             @Field("phone_no") String phone);
}
