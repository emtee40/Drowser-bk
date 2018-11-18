package com.jarsilio.android.drowser.models

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Database
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Update
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import timber.log.Timber
import android.arch.persistence.room.Room
import android.arch.lifecycle.LiveData
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

@Entity
data class AppItem(
    @PrimaryKey var packageName: String,
    var name: String,
    var isSystem: Boolean,
    var isDrowseCandidate: Boolean,
    var show: Boolean
) {

    fun getIcon(context: Context): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            null
        }
    }
}

@Dao
interface BaseDao<in T> {
    @Insert()
    fun insert(t: T)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIfNotExists(t: T)
    @Update
    fun update(t: T)
    @Delete
    fun delete(t: T)
}

@Dao
interface AppItemsDao : BaseDao<AppItem> {
    @get:Query("SELECT * FROM appitem ORDER BY name COLLATE UNICODE")
    val all: List<AppItem>

    @get:Query("SELECT * FROM appitem ORDER BY name COLLATE UNICODE")
    val allLive: LiveData<List<AppItem>>

    @get:Query("SELECT * FROM appitem WHERE isDrowseCandidate = 1 ORDER BY name COLLATE UNICODE")
    val drowseCandidates: List<AppItem>

    @get:Query("SELECT * FROM appitem WHERE isDrowseCandidate = 1 AND show = 1 ORDER BY name COLLATE UNICODE")
    val drowseCandidatesLive: LiveData<List<AppItem>>

    @get:Query("SELECT * FROM appitem WHERE isDrowseCandidate = 0 AND show = 1 ORDER BY name COLLATE UNICODE")
    val nonDrowseCandidatesLive: LiveData<List<AppItem>>

    @Query("SELECT * FROM appitem WHERE packageName IN (:packageNames)")
    fun loadAllByPackageNames(packageNames: List<String>): List<AppItem>

    @Query("SELECT * FROM appitem WHERE packageName LIKE :packageName LIMIT 1")
    fun loadByPackageName(packageName: String): AppItem?

    @Query("UPDATE AppItem SET isDrowseCandidate = :isDrowseCandidate WHERE packageName = :packageName")
    fun setDrowseCandidate(packageName: String, isDrowseCandidate: Boolean)

    @Query("UPDATE AppItem SET show = 1 WHERE isSystem = 1")
    fun showSystemApps()

    @Query("UPDATE AppItem SET show = 0 WHERE isSystem = 1")
    fun hideSystemApps()
}

@Database(entities = arrayOf(AppItem::class), version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appItemsDao(): AppItemsDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppDatabase::class.java, "AppItems")
                .addMigrations(MIGRATION_1_2)
                .build()
    })
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE appitem ADD COLUMN show INTEGER NOT NULL DEFAULT 1")
    }
}