package com.rentproof.app.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import java.util.*

// ==================== 房源 ====================

@Entity(tableName = "houses")
@Parcelize
data class House(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String = "",
    val landlordName: String = "",
    val landlordPhone: String = "",
    val rentStart: Long = 0,
    val rentEnd: Long = 0,
    val deposit: Double = 0.0,
    val monthlyRent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Dao
interface HouseDao {
    @Query("SELECT * FROM houses ORDER BY createdAt DESC")
    suspend fun getAll(): List<House>
    
    @Query("SELECT * FROM houses WHERE id = :id")
    suspend fun getById(id: String): House?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(house: House)
    
    @Update
    suspend fun update(house: House)
    
    @Delete
    suspend fun delete(house: House)
}

// ==================== 存证记录 ====================

@Entity(tableName = "records")
@Parcelize
data class Record(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val houseId: String,
    val type: RecordType,
    val note: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    val typeName: String
        get() = type.displayName
}

enum class RecordType(val displayName: String) {
    CHECKIN("入住验房"),
    PROBLEM("房屋问题"),
    DAMAGE("物品损坏"),
    REPAIR("维修记录"),
    OTHER("其他存证")
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM records WHERE houseId = :houseId ORDER BY createdAt DESC")
    suspend fun getByHouse(houseId: String): List<Record>
    
    @Query("SELECT * FROM records WHERE houseId = :houseId AND type = :type ORDER BY createdAt DESC")
    suspend fun getByType(houseId: String, type: RecordType): List<Record>
    
    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: String): Record?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Record)
    
    @Delete
    suspend fun delete(record: Record)
    
    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("SELECT COUNT(*) FROM records WHERE houseId = :houseId")
    suspend fun getCount(houseId: String): Int
}

// ==================== 照片 ====================

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Record::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class Photo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recordId: String,
    val path: String,
    val watermarkTime: String = "",
    val watermarkLocation: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE recordId = :recordId ORDER BY createdAt ASC")
    suspend fun getByRecord(recordId: String): List<Photo>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)
    
    @Delete
    suspend fun delete(photo: Photo)
    
    @Query("DELETE FROM photos WHERE recordId = :recordId")
    suspend fun deleteByRecord(recordId: String)
}

// ==================== 数据库 ====================

@Database(
    entities = [House::class, Record::class, Photo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun houseDao(): HouseDao
    abstract fun recordDao(): RecordDao
    abstract fun photoDao(): PhotoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(application: android.app.Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application.applicationContext,
                    AppDatabase::class.java,
                    "rent_proof_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
