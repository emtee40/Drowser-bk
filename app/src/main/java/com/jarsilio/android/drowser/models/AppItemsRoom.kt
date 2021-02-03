package com.jarsilio.android.drowser.models

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

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

    @get:Query("SELECT * FROM appitem WHERE isDrowseCandidate = 1 AND show = 1 ORDER BY name COLLATE UNICODE")
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

    @Query("UPDATE AppItem SET show = :show WHERE packageName = :packageName")
    fun showApp(packageName: String, show: Boolean)
}

@Database(entities = [AppItem::class], version = 2)
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
