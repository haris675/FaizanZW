package com.app.faizanzw.network;

import android.content.Context;

import com.app.faizanzw.BuildConfig;
import com.app.faizanzw.FaizanApp;
import com.app.faizanzw.utils.Constants;
import com.google.gson.JsonElement;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    public static String token = "";
    private static Retrofit retrofit;

    public static void init(Context context) {
        retrofit = null;
    }

    public static RequestBody getRequestBody(String text) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), text);
    }

    public static MultipartBody.Part getImageBody(String key, String imagePath) {
        MultipartBody.Part fileBody = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            fileBody = MultipartBody.Part.createFormData(key, file.getName(), requestFile);
        }
        return fileBody;
    }


    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            /*if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(interceptor).build();
            }*/


            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }

        return retrofit;
    }

    public static ApiServices getAPI() {
        return ApiClient.getClient().create(ApiServices.class);
    }

    // ---------------------------------  api  --------------------------------- //

    /*public static void LoginApi(String companyCode,
                                String loginUserID,
                                String branchID,
                                String tranType,
                                String taskPriority,
                                String taskDescription,
                                String tranDate,
                                String assignTo,
                                String image,
                                String tranID,
                                String subject,
                                OnApiResponseListener<JsonElement> listener) {
        Call<JsonElement> call = getAPI().doaddTaskEntry2(
                companyCode, loginUserID, branchID,
                tranType,
                taskPriority,
                taskDescription,
                tranDate,
                assignTo,
                image,
                tranID,
                subject);
        call.enqueue(new APICallBack<>(listener, 0));
    }*/

    public static void updateDeliveryTask(String url,String code, int id, String description, String image, OnApiResponseListener<JsonElement> listener) {
        Call<JsonElement> call = getAPI().doSaveAssosiateTask2(
                url,
                getRequestBody(code),
                getRequestBody("0"),
                getRequestBody(id + ""),
                getRequestBody(description),
                getRequestBody("100"),
                getRequestBody("COMPLETE"),
                getImageBody("DocByte", image));
        call.enqueue(new APICallBack<>(listener, 0));
    }

}
