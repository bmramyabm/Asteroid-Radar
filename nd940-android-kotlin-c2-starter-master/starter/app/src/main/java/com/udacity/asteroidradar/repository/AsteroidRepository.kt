package com.udacity.asteroidradar.repository

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.domain.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.domain.PictureOfDay
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    /**
     * Picture of the Day can be shown on the screen
     */

    val pictureOfDay: LiveData<PictureOfDay> = Transformations.map(
        database.pictureOfDayDao.getPictureOfDay()
    ) { it?.asDomainModel() }

    /**
     * Display the list of asteroid based on the option from the menu chosen
     */

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroidsList()) { it.asDomainModel() }
    val todayAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getTodayAsteroids()) { it.asDomainModel() }
    val weeklyAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getWeeklyAsteroids()) { it.asDomainModel() }


    suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            Timber.d("Network Request to display Picture of the day")
            val pictureOfDay =
                AsteroidApi.retrofitMoshiService.getPictureOfDay(apiKey = Constants.API_KEY)
            database.pictureOfDayDao.insertPictureOfDay(pictureOfDay.asDatabaseModel())
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    fun getFormattedDate(days: Int = 0): String {
        val calendar = Calendar.getInstance()
        if (days > 0) {
            calendar.add(Calendar.DAY_OF_YEAR, days)
        }
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val today = getFormattedDate()
                val connectionString = AsteroidApi.retrofitMoshiService.getAsteroids(
                    apiKey = Constants.API_KEY, startDate = today, endDate = null
                )
                Timber.d("The results from start $today are $connectionString")
                val networkAsteroids = parseAsteroidsJsonResult(JSONObject(connectionString))
                database.asteroidDao.insertAll(*networkAsteroids.asDatabaseModel())
            } catch (e: Exception) {
                Timber.e("Error uploading Data: $e")
            }
        }
    }


}

