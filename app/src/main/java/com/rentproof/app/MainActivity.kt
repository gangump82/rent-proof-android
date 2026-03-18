package com.rentproof.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = """
                🏠 租房存证 App
                
                最小测试版 v1.0
                
                如果你看到这个页面，
                说明 App 基础框架正常！
                
                接下来可以逐步添加功能。
            """.trimIndent()
            textSize = 20f
            setTextColor(Color.BLACK)
            setPadding(50, 100, 50, 50)
            setBackgroundColor(Color.WHITE)
        }
        setContentView(textView)
    }
}
