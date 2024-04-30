package com.app.faizanzw.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceModule @Inject constructor(@ApplicationContext context: Context) {

    /*private var instance: SharedPreferences? = null*/

    private var masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    /*@Provides
    fun getInstance(@ApplicationContext context: Context): SharedPreferences =
       context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)*/

    val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "Faizan@Shared@preference",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    operator fun set(key: PrefEnum, value: Any?) {
        when (value) {
            is String -> {
                preferences.edit().putString(key.name, value).apply()
            }
            is Int -> {
                preferences.edit().putInt(key.name, value).apply()
            }
            is Float -> {
                preferences.edit().putFloat(key.name, value).apply()
            }
            is Boolean -> {
                preferences.edit().putBoolean(key.name, value).apply()
            }
        }
    }

    inline operator fun <reified T> get(key: PrefEnum, defaultValue: T?): T {
        return when (T::class) {
            String::class -> {
                preferences.getString(key.name, defaultValue as String) as T
            }
            Int::class -> {
                preferences.getInt(key.name, defaultValue as Int) as T
            }
            Float::class -> {
                preferences.getFloat(key.name, defaultValue as Float) as T
            }
            Boolean::class -> {
                preferences.getBoolean(key.name, defaultValue as Boolean) as T
            }
            else -> {
                throw UnsupportedOperationException("Not yet implemented")
            }
        }
    }


    fun storeUserData(jsonElement: JsonObject) {

        Log.e("data : ", "${jsonElement.get("UserFullName").asString}")

        set(PrefEnum.USERNAME, jsonElement.get("UserFullName").asString)
        set(PrefEnum.PROFILE, jsonElement.get("UserImage").asString)
        set(PrefEnum.USERID, jsonElement.get("UserMasterKey").asInt)
        set(PrefEnum.BRANCHID, jsonElement.get("BranchID").asInt)
        if (jsonElement.has("CompanyCode"))
            set(PrefEnum.COMPANYCODE, jsonElement.get("CompanyCode").asString)
        else
            set(PrefEnum.COMPANYCODE, "FZ")
        set(PrefEnum.ISLOGIN, 1)
        set(PrefEnum.ALLOWTOSHOW, jsonElement.get("AllowToShowAllbranch").asBoolean)
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}