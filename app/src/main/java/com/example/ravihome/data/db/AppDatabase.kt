package com.example.ravihome.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ravihome.data.converter.LocalDateConverter
import com.example.ravihome.data.converter.WorkStatusConverter
import com.example.ravihome.data.dao.WorkDao
import com.example.ravihome.data.entity.WorkEntity

@Database(
    entities = [WorkEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(WorkStatusConverter::class, LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workDao(): WorkDao
}
