package com.app.faizanzw.network

import android.util.Log
import com.app.faizanzw.BuildConfig
import com.app.faizanzw.utils.Constants
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class RetrofitService {
    private var gson: Gson

    init {
        gson = GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    fun provideHttpClientForBaseApi(mPref: PreferenceModule): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val interceptor =
                HttpLoggingInterceptor().also { it.setLevel(HttpLoggingInterceptor.Level.BODY) }
            httpClient.addInterceptor(interceptor).build()
        } else {
            val interceptor =
                HttpLoggingInterceptor().also { it.setLevel(HttpLoggingInterceptor.Level.NONE) }
            httpClient.addInterceptor(interceptor).build()
        }

        Log.e("eeee : ",mPref.get(PrefEnum.COMPANYURL,"vvv"))

        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .method(original.method, original.body)
                //.header("Authorization", "Bearer ${myPrefs.get(PrefEnum.TOKEN, "")}")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        httpClient.readTimeout(180, TimeUnit.SECONDS)
        httpClient.connectTimeout(180, TimeUnit.SECONDS)
        return httpClient.build()
    }

    @Provides
    @Qualifiers.baseClientWithToken
    fun baseApiServices(
        okHttpClient: OkHttpClient,
        mPref: PreferenceModule
    ): ApiServices = Retrofit.Builder()
        .baseUrl(mPref.get(PrefEnum.COMPANYURL, Constants.BASE_URL))
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiServices::class.java)


    @Provides
    @Qualifiers.nodeClientWithToken
    fun nodeApiServices(
        okHttpClient: OkHttpClient
    ): ApiServices = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiServices::class.java)

}


