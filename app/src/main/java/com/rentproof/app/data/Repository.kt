package com.rentproof.app.data

import android.content.Context
import android.graphics.*
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.rentproof.app.RentProofApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RecordRepository(private val context: Context) {
    
    private val database = RentProofApp.database ?: AppDatabase.getDatabase(context)
    private val houseDao = database.houseDao()
    private val recordDao = database.recordDao()
    private val photoDao = database.photoDao()
    
    // ==================== 房源操作 ====================
    
    suspend fun getAllHouses(): List<House> = withContext(Dispatchers.IO) {
        houseDao.getAll()
    }
    
    suspend fun getHouse(id: String): House? = withContext(Dispatchers.IO) {
        houseDao.getById(id)
    }
    
    suspend fun saveHouse(house: House) = withContext(Dispatchers.IO) {
        houseDao.insert(house)
    }
    
    suspend fun deleteHouse(house: House) = withContext(Dispatchers.IO) {
        houseDao.delete(house)
    }
    
    // ==================== 记录操作 ====================
    
    suspend fun getRecords(houseId: String): List<Record> = withContext(Dispatchers.IO) {
        recordDao.getByHouse(houseId)
    }
    
    suspend fun getRecord(id: String): Record? = withContext(Dispatchers.IO) {
        recordDao.getById(id)
    }
    
    suspend fun saveRecord(record: Record) = withContext(Dispatchers.IO) {
        recordDao.insert(record)
    }
    
    suspend fun deleteRecord(record: Record) = withContext(Dispatchers.IO) {
        recordDao.delete(record)
        photoDao.deleteByRecord(record.id)
    }
    
    // ==================== 照片操作 ====================
    
    suspend fun getPhotos(recordId: String): List<Photo> = withContext(Dispatchers.IO) {
        photoDao.getByRecord(recordId)
    }
    
    suspend fun savePhoto(photo: Photo) = withContext(Dispatchers.IO) {
        photoDao.insert(photo)
    }
    
    suspend fun savePhotos(photos: List<Photo>) = withContext(Dispatchers.IO) {
        photoDao.insertAll(photos)
    }
    
    // ==================== 水印处理 ====================
    
    suspend fun addWatermark(
        sourceUri: Uri,
        latitude: Double,
        longitude: Double
    ): Uri = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        
        val locationStr = if (latitude != 0.0 && longitude != 0.0) {
            "${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"
        } else {
            "未知位置"
        }
        
        // 读取原图
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // 创建可变位图
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        
        // 水印画笔
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }
        
        // 绘制水印
        val padding = 30f
        val text1 = "时间: $timestamp"
        val text2 = "地点: $locationStr"
        
        canvas.drawText(text1, padding, mutableBitmap.height - 100f, paint)
        canvas.drawText(text2, padding, mutableBitmap.height - 50f, paint)
        
        // 保存到文件
        val fileName = "watermarked_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        
        FileOutputStream(file).use { out ->
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    
    // ==================== 导出功能 ====================
    
    suspend fun generateEvidenceSummary(
        house: House,
        records: List<Record>
    ): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        
        sb.appendLine("【租房存证汇总】")
        sb.appendLine()
        sb.appendLine("房源：${house.name}")
        sb.appendLine("地址：${house.address}")
        sb.appendLine("房东：${house.landlordName} ${house.landlordPhone}")
        sb.appendLine("押金：${house.deposit} 元")
        sb.appendLine()
        sb.appendLine("--- 存证记录 ---")
        sb.appendLine()
        
        records.forEachIndexed { index, record ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timeStr = dateFormat.format(Date(record.createdAt))
            
            sb.appendLine("${index + 1}. 【${record.typeName}】")
            sb.appendLine("   时间：$timeStr")
            if (record.note.isNotEmpty()) {
                sb.appendLine("   备注：${record.note}")
            }
            
            val photos = photoDao.getByRecord(record.id)
            sb.appendLine("   照片：${photos.size}张")
            sb.appendLine()
        }
        
        sb.appendLine("--- 共${records.size}条记录 ---")
        sb.appendLine()
        sb.appendLine("本存证由「租房存证」App生成")
        
        sb.toString()
    }
    
    // ==================== 统计 ====================
    
    suspend fun getStats(houseId: String): RecordStats = withContext(Dispatchers.IO) {
        val records = recordDao.getByHouse(houseId)
        RecordStats(
            total = records.size,
            checkin = records.count { it.type == RecordType.CHECKIN },
            problem = records.count { it.type == RecordType.PROBLEM },
            damage = records.count { it.type == RecordType.DAMAGE },
            repair = records.count { it.type == RecordType.REPAIR }
        )
    }
}

data class RecordStats(
    val total: Int,
    val checkin: Int,
    val problem: Int,
    val damage: Int,
    val repair: Int
)
