package com.example.ravihome.di

import android.content.Context
import androidx.room.Room
import com.example.ravihome.data.dao.WorkDao
import com.example.ravihome.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "works_db"
        ).build()
    }

    @Provides
    fun provideWorkDao(db: AppDatabase): WorkDao = db.workDao()
}
