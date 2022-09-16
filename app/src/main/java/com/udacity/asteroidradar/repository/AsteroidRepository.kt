package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.api.AsteroidApiService
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsteroidRepository(private val database: AsteroidDatabase) {


    val allAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAllAsteroids()) {
            it.asDomainModel()
        }

    val todayAsteroids: LiveData<List<Asteroid>> = Transformations.map(
        database.asteroidDao.getAsteroidsOnDay(
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)
        )
    ) {
        it.asDomainModel()
    }

    val weekAsteroids: LiveData<List<Asteroid>> = Transformations.map(
        database.asteroidDao.getAsteroidsDateRange(
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
            LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_DATE)
        )
    ) {
        it.asDomainModel()
    }


    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val asteroids = AsteroidApiService.AsteroidApi.retrofitService.getAsteroids(API_KEY)
                val result = parseAsteroidsJsonResult(JSONObject(asteroids))
                database.asteroidDao.insertAll(*result.asDatabaseModel())
            } catch (e: Exception) {
                Log.e("AsteroidRepository", e.message.toString())
            }
        }
    }


}