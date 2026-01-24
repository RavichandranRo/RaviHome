package com.example.ravihome.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ravihome.data.dao.WorkDao
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.data.converter.WorkStatusConverter

@Database(
    entities = [WorkEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(WorkStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workDao(): WorkDao
}
