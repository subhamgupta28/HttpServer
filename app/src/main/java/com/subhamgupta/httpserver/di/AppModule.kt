package com.subhamgupta.httpserver.di

import android.app.Application
import com.subhamgupta.httpserver.data.repository.MainRepository
import com.subhamgupta.httpserver.domain.model.InvalidateToken
import com.subhamgupta.httpserver.domain.model.LoggedInUser
import com.subhamgupta.httpserver.domain.model.ReceivedFile
import com.subhamgupta.httpserver.domain.model.RequestLog
import com.subhamgupta.httpserver.domain.model.ServerSession
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.domain.model.UserBasedRoles
import com.subhamgupta.httpserver.utils.SettingStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun providesRealm(): Realm {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                ServerSession::class,
                User::class,
                InvalidateToken::class,
                UserBasedRoles::class,
                LoggedInUser::class,
                RequestLog::class,
                ReceivedFile::class
            )
        ).compactOnLaunch()
            .build()
        return Realm.open(config)
    }

    @Singleton
    @Provides
    fun provideSettingStorage(
        application: Application
    ): SettingStorage = SettingStorage(application)

    @Singleton
    @Provides
    fun provideMainRepository(
        realm: Realm,
        settingStorage: SettingStorage,
        application: Application
    ): MainRepository = MainRepository(
        realm,
        settingStorage,
        application
    )


}