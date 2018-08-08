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
import android.support.v4.content.ContextCompat
import com.jarsilio.android.drowser.R
import timber.log.Timber
import android.arch.persistence.room.Room
import android.arch.lifecycle.LiveData

@Entity
data class AppItem(
    @PrimaryKey var packageName: String,
    var name: String,
    var isSystem: Boolean,
    var isDrowseCandidate: Boolean
) {

    fun getIcon(context: Context): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
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
    val all: LiveData<List<AppItem>>

    @get:Query("SELECT * FROM appitem WHERE isDrowseCandidate = 1 ORDER BY name COLLATE UNICODE")
    val drowseCandidates: List<AppItem>

    @get:Query("SELECT * FROM appitem WHERE isDrowseCandidate = 1 ORDER BY name COLLATE UNICODE")
    val drowseCandidatesLive: LiveData<List<AppItem>>

    @get:Query("SELECT * FROM appitem WHERE isSystem = 0 ORDER BY name COLLATE UNICODE")
    val userApps: List<AppItem>

    @get:Query("SELECT * FROM appitem WHERE isSystem = 0 ORDER BY name COLLATE UNICODE")
    val userAppsLive: LiveData<List<AppItem>>

    @get:Query("SELECT * FROM appitem WHERE isSystem = 1 ORDER BY name COLLATE UNICODE")
    val systemApps: List<AppItem>

    @Query("SELECT * FROM appitem WHERE packageName IN (:packageNames)")
    fun loadAllByPackageNames(packageNames: List<String>): List<AppItem>

    @Query("SELECT * FROM appitem WHERE packageName LIKE :packageName LIMIT 1")
    fun loadByPackageName(packageName: String): AppItem

    @Query("UPDATE AppItem SET isDrowseCandidate = :isDrowseCandidate WHERE packageName = :packageName")
    fun setDrowseCandidate(packageName: String, isDrowseCandidate: Boolean)
}

@Database(entities = arrayOf(AppItem::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appItemsDao(): AppItemsDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, AppDatabase::class.java, "AppItems").build()
    })
}
