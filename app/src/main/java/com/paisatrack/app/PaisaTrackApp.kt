package com.paisatrack.app

import android.app.Application
import com.paisatrack.app.data.local.AppDatabase

class PaisaTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize local Room database
        AppDatabase.getInstance(this)
    }
}
