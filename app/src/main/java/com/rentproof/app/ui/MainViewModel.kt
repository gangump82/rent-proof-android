package com.rentproof.app.ui

import android.app.Application
import androidx.lifecycle.*
import com.rentproof.app.RentProofApp
import com.rentproof.app.data.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = RecordRepository(application)
    
    // 当前房源
    private val _currentHouse = MutableLiveData<House?>()
    val currentHouse: LiveData<House?> = _currentHouse
    
    // 房源列表
    private val _houses = MutableLiveData<List<House>>()
    val houses: LiveData<List<House>> = _houses
    
    // 存证记录
    private val _records = MutableLiveData<List<Record>>()
    val records: LiveData<List<Record>> = _records
    
    // 统计
    private val _stats = MutableLiveData<RecordStats>()
    val stats: LiveData<RecordStats> = _stats
    
    // 加载状态
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadHouses()
    }
    
    // ==================== 房源 ====================
    
    fun loadHouses() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = repository.getAllHouses()
            _houses.value = list
            
            if (list.isNotEmpty()) {
                _currentHouse.value = list.first()
                loadRecords(list.first().id)
            }
            _isLoading.value = false
        }
    }
    
    fun selectHouse(house: House) {
        _currentHouse.value = house
        loadRecords(house.id)
    }
    
    fun saveHouse(house: House) {
        viewModelScope.launch {
            repository.saveHouse(house)
            loadHouses()
        }
    }
    
    fun deleteHouse(house: House) {
        viewModelScope.launch {
            repository.deleteHouse(house)
            loadHouses()
        }
    }
    
    // ==================== 记录 ====================
    
    fun loadRecords(houseId: String) {
        viewModelScope.launch {
            val list = repository.getRecords(houseId)
            _records.value = list
            
            // 更新统计
            _stats.value = repository.getStats(houseId)
        }
    }
    
    fun saveRecord(record: Record, photos: List<Photo>) {
        viewModelScope.launch {
            repository.saveRecord(record)
            repository.savePhotos(photos)
            _currentHouse.value?.let { loadRecords(it.id) }
        }
    }
    
    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            _currentHouse.value?.let { loadRecords(it.id) }
        }
    }
    
    // ==================== 照片 ====================
    
    suspend fun getPhotos(recordId: String): List<Photo> {
        return repository.getPhotos(recordId)
    }
    
    suspend fun addWatermark(uri: android.net.Uri, lat: Double, lng: Double): android.net.Uri {
        return repository.addWatermark(uri, lat, lng)
    }
    
    // ==================== 导出 ====================
    
    suspend fun generateSummary(): String? {
        val house = _currentHouse.value ?: return null
        val records = _records.value ?: emptyList()
        return repository.generateEvidenceSummary(house, records)
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
