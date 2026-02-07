package com.example.ravihome.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS works_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    date INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    amount REAL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )

            val cursor = db.query("SELECT id, title, description, date, status, amount, createdAt FROM works")
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val description = cursor.getString(2)
                val dateRaw = cursor.getString(3)
                val status = cursor.getString(4)
                val amount = if (cursor.isNull(5)) null else cursor.getDouble(5)
                val createdAt = cursor.getLong(6)
                val epochDay = runCatching {
                    java.time.LocalDate.parse(dateRaw, dateFormatter).toEpochDay()
                }.getOrElse {
                    runCatching { java.time.LocalDate.parse(dateRaw).toEpochDay() }.getOrDefault(0L)
                }

                db.execSQL(
                    "INSERT INTO works_new (id, title, description, date, status, amount, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(id, title, description, epochDay, status, amount, createdAt)
                )
            }
            cursor.close()

            db.execSQL("DROP TABLE works")
            db.execSQL("ALTER TABLE works_new RENAME TO works")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "works_db"
        ).addMigrations(migration1To2).build()
    }

    @Provides
    fun provideWorkDao(db: AppDatabase): WorkDao = db.workDao()
}
