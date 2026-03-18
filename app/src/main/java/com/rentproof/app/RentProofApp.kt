package com.rentproof.app

import android.app.Application
import android.util.Log

class RentProofApp : Application() {
    
    companion object {
        private const val TAG = "RentProofApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RentProofApp onCreate started")
    }
}
