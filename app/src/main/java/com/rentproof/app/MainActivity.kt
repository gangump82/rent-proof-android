package com.rentproof.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate started")
        
        try {
            val textView = TextView(this).apply {
                text = "租房存证 App 测试版\n\n如果你看到这个页面，说明 App 可以正常启动了！"
                textSize = 20f
                setPadding(50, 50, 50, 50)
            }
            setContentView(textView)
            Log.d(TAG, "MainActivity onCreate success")
        } catch (e: Exception) {
            Log.e(TAG, "MainActivity onCreate failed", e)
        }
    }
}
