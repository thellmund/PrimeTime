package com.hellmund.primetime.database

import android.arch.persistence.room.Room
import android.content.Context

object PrimeTimeDatabase {

    private var instance: AppDatabase? = null

    @JvmStatic
    fun getInstance(context: Context): AppDatabase {
        if (instance == null) {
            instance = Room
                    .databaseBuilder(context, AppDatabase::class.java, "db")
                    .allowMainThreadQueries() // TODO
                    .build()
        }

        return instance!!
    }

}
