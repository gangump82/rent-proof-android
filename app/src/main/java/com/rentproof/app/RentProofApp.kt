package com.rentproof.app

import android.app.Application
import android.util.Log
import com.rentproof.app.data.AppDatabase

class RentProofApp : Application() {
    
    companion object {
        private const val TAG = "RentProofApp"
        var database: AppDatabase? = null
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            database = AppDatabase.getDatabase(this)
            Log.d(TAG, "Database initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize database", e)
        }
    }
}
