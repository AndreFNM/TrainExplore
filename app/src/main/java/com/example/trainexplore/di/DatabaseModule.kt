package com.example.trainexplore.di

import android.content.Context
import androidx.room.Room
import com.example.trainexplore.database.AppDatabase
import com.google.android.datatransport.runtime.dagger.Module
import com.google.android.datatransport.runtime.dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, AppDatabase :: class.java, "TrainExploreDB").build()

    @Singleton
    @Provides
    fun provideDao(database: AppDatabase) = database.Estacao()
}