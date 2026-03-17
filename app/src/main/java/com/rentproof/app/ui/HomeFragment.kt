package com.rentproof.app.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.rentproof.app.R
import com.rentproof.app.data.House
import com.rentproof.app.data.Photo
import com.rentproof.app.data.Record
import com.rentproof.app.data.RecordStats
import com.rentproof.app.data.RecordType
import com.rentproof.app.databinding.FragmentHomeBinding
import com.rentproof.app.databinding.DialogAddHouseBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    
    private lateinit var recordAdapter: RecordAdapter
    private var currentLocation: Location? = null
    
    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        
        if (cameraGranted) {
            openCamera()
        }
    }
    
    // 拍照
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            processPhoto(photoUri!!)
        }
    }
    
    private var photoUri: Uri? = null
    private var currentRecordType: RecordType = RecordType.CHECKIN
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeData()
    }
    
    private fun setupViews() {
        // 记录列表
        recordAdapter = RecordAdapter(
            onItemClick = { record -> showRecordDetail(record) },
            onItemDelete = { record -> deleteRecord(record) }
        )
        binding.recyclerRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordAdapter
        }
        
        // 快捷存证按钮
        binding.cardCheckin.setOnClickListener { quickRecord(RecordType.CHECKIN) }
        binding.cardProblem.setOnClickListener { quickRecord(RecordType.PROBLEM) }
        binding.cardDamage.setOnClickListener { quickRecord(RecordType.DAMAGE) }
        binding.cardRepair.setOnClickListener { quickRecord(RecordType.REPAIR) }
        
        // 添加房源
        binding.btnAddHouse.setOnClickListener { showAddHouseDialog() }
        
        // 导出
        binding.btnExport.setOnClickListener { exportEvidence() }
        
        // 查看全部
        binding.btnViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_timeline)
        }
    }
    
    private fun observeData() {
        viewModel.currentHouse.observe(viewLifecycleOwner) { house ->
            house?.let {
                binding.textHouseName.text = it.name
                binding.textHouseAddress.text = it.address
                binding.textDeposit.text = "${it.deposit.toInt()}元押金"
            } ?: run {
                binding.textHouseName.text = "请添加房源"
                binding.textHouseAddress.text = ""
                binding.textDeposit.text = ""
            }
        }
        
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.textTotalCount.text = stats.total.toString()
            binding.textCheckinCount.text = stats.checkin.toString()
            binding.textProblemCount.text = (stats.problem + stats.damage).toString()
            binding.textRepairCount.text = stats.repair.toString()
        }
        
        viewModel.records.observe(viewLifecycleOwner) { records ->
            recordAdapter.submitList(records.take(5))
            binding.textNoRecords.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    // ==================== 存证操作 ====================
    
    private fun quickRecord(type: RecordType) {
        if (viewModel.currentHouse.value == null) {
            Toast.makeText(requireContext(), "请先添加房源", Toast.LENGTH_SHORT).show()
            showAddHouseDialog()
            return
        }
        
        currentRecordType = type
        checkPermissionsAndOpenCamera()
    }
    
    private fun checkPermissionsAndOpenCamera() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        val needRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needRequest.isEmpty()) {
            openCamera()
        } else {
            permissionLauncher.launch(needRequest.toTypedArray())
        }
    }
    
    private fun openCamera() {
        // 创建临时文件
        val photoFile = java.io.File.createTempFile(
            "photo_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().cacheDir
        )
        photoUri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(photoUri)
    }
    
    private fun processPhoto(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            // 添加水印
            val watermarkedUri = viewModel.addWatermark(uri, 0.0, 0.0)
            
            // 显示备注对话框
            showNoteDialog(watermarkedUri)
        }
    }
    
    private fun showNoteDialog(photoUri: Uri) {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "备注说明（选填）"
            setPadding(50, 30, 50, 30)
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(currentRecordType.displayName)
            .setView(input)
            .setPositiveButton("保存") { _, _ ->
                saveRecord(photoUri, input.text.toString())
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun saveRecord(photoUri: Uri, note: String) {
        val house = viewModel.currentHouse.value ?: return
        
        val record = Record(
            houseId = house.id,
            type = currentRecordType,
            note = note
        )
        
        val photo = Photo(
            recordId = record.id,
            path = photoUri.toString()
        )
        
        viewModel.saveRecord(record, listOf(photo))
        
        Toast.makeText(requireContext(), "存证成功", Toast.LENGTH_SHORT).show()
    }
    
    // ==================== 房源管理 ====================
    
    private fun showAddHouseDialog() {
        val dialogBinding = DialogAddHouseBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("添加房源")
            .setView(dialogBinding.root)
            .setPositiveButton("保存") { _, _ ->
                val house = House(
                    name = dialogBinding.editName.text.toString(),
                    address = dialogBinding.editAddress.text.toString(),
                    landlordName = dialogBinding.editLandlord.text.toString(),
                    landlordPhone = dialogBinding.editPhone.text.toString(),
                    deposit = dialogBinding.editDeposit.text.toString().toDoubleOrNull() ?: 0.0
                )
                viewModel.saveHouse(house)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    // ==================== 记录操作 ====================
    
    private fun showRecordDetail(record: Record) {
        viewLifecycleOwner.lifecycleScope.launch {
            val photos = viewModel.getPhotos(record.id)
            
            val message = buildString {
                append("类型：${record.typeName}\n")
                append("时间：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(record.createdAt))}\n")
                if (record.note.isNotEmpty()) {
                    append("备注：${record.note}\n")
                }
                append("照片：${photos.size}张")
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(record.typeName)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show()
        }
    }
    
    private fun deleteRecord(record: Record) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认删除")
            .setMessage("删除后无法恢复，确定要删除这条存证记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteRecord(record)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    // ==================== 导出 ====================
    
    private fun exportEvidence() {
        viewLifecycleOwner.lifecycleScope.launch {
            val summary = viewModel.generateSummary()
            if (summary != null) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("存证汇总", summary)
                clipboard.setPrimaryClip(clip)
                
                Toast.makeText(requireContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
