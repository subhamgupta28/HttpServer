package com.subhamgupta.httpserver

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.subhamgupta.httpserver.utils.SettingStorage
import dagger.hilt.android.HiltAndroidApp
import io.ktor.server.netty.NettyApplicationEngine
import io.realm.kotlin.Realm
import javax.inject.Inject


@HiltAndroidApp
class MyApp: Application() {



    var httpServer: NettyApplicationEngine? = null

    @Inject
    lateinit var realm: Realm

    @Inject
    lateinit var settingStorage: SettingStorage



    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
    }

    companion object{
        lateinit var instance: MyApp
    }
}
//data class Jedi(val name: String, val age: Int)