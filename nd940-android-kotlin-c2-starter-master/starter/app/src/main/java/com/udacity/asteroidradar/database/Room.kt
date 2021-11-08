package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface AsteroidDao {

    // To display Asteroids based the option selected from the menu
    @Query("select * from databaseasteroid  order by date(closeApproachDate) asc")
    fun getAsteroidsList(): LiveData<List<DatabaseAsteroid>>
    //To Display the asteroid list for today
    @Query("SELECT * FROM databaseasteroid WHERE closeApproachDate = date('now')")
    fun getTodayAsteroids(): LiveData<List<DatabaseAsteroid>>
    //To display the asteroid list for the week
    @Query("SELECT * FROM databaseasteroid WHERE closeApproachDate BETWEEN date('now') AND date('now', '+6 day') ORDER BY closeApproachDate ASC")
    fun getWeeklyAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

}

@Dao
interface PictureOfDayDao{
    @Query("select * from databasepictureofday")
    fun getPictureOfDay(): LiveData<DatabasePictureOfDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPictureOfDay(pictureOfDay: DatabasePictureOfDay)

}

@Database(entities = [DatabaseAsteroid::class, DatabasePictureOfDay::class], version = 2)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
    abstract val pictureOfDayDao :  PictureOfDayDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
//    synchronized so it is thread safe and we only ever get one db instance
    // added .fallbackToDestructiveMigration() to avoid migrations when upgrading db version
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids"
            ).fallbackToDestructiveMigration().build()
        }
    }
    return INSTANCE
}