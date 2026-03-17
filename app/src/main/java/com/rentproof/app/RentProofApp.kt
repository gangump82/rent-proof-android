package com.rentproof.app

import android.app.Application
import com.rentproof.app.data.AppDatabase

class RentProofApp : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        // 初始化
    }
}
