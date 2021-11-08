package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception



@RequiresApi(Build.VERSION_CODES.N)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    private val _filterSelected = MutableLiveData<AsteroidApiFilter>(AsteroidApiFilter.SHOW_SAVE)
    val filterSelected: LiveData<AsteroidApiFilter>
        get() = _filterSelected

    val asteroids = Transformations.switchMap(_filterSelected) {
        when (it!!) {
            AsteroidApiFilter.SHOW_WEEK -> asteroidsRepository.weeklyAsteroids
            AsteroidApiFilter.SHOW_TODAY -> asteroidsRepository.todayAsteroids
            else -> asteroidsRepository.asteroids
        }
    }

    val pictureOfDay = asteroidsRepository.pictureOfDay

    init {
        viewModelScope.launch {
            try {
                asteroidsRepository.refreshAsteroids()
            } catch (e: Exception) {
                Timber.e("Asteroid List Not Loading $e")
            }
        }
        viewModelScope.launch {
            try {
                asteroidsRepository.refreshPictureOfDay()
            } catch (e:Exception){
                Timber.e("Could Not Picture of the Day")
            }
        }

    }

    private val _navigateToSelectedProperty = MutableLiveData<Asteroid>()
    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedProperty

    fun displayAsteroidDeatilsComplete() {
        _navigateToSelectedProperty.value = null
    }

    fun displayAsteroidDetail(asteroid: Asteroid) {
        _navigateToSelectedProperty.value = asteroid
    }


    // this will be observed in the fragment and cause doFilter to run, which in turn
    // modifies which source of asteroids from the repository are used
    fun updateFilter(filter: AsteroidApiFilter) {
        Timber.d("Update _filterSelected with ${filter.toString()}")
        _filterSelected.value = filter
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }


}