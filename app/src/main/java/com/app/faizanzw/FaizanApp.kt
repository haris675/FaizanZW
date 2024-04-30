package com.app.faizanzw

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import io.realm.RealmConfiguration

@HiltAndroidApp
class FaizanApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val config = RealmConfiguration.Builder()
            .name("default-realm")
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .deleteRealmIfMigrationNeeded()
            .compactOnLaunch()
            .build()

        Realm.setDefaultConfiguration(config)
    }

}