package com.rentproof.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rentproof.app.R
import com.rentproof.app.data.RecordType
import com.rentproof.app.databinding.FragmentTimelineBinding
import kotlinx.coroutines.launch

class TimelineFragment : Fragment() {
    
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    
    private lateinit var recordAdapter: RecordAdapter
    private var currentFilter: RecordType? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeData()
    }
    
    private fun setupViews() {
        recordAdapter = RecordAdapter(
            onItemClick = { record -> showRecordDetail(record) },
            onItemDelete = { record -> deleteRecord(record) }
        )
        
        binding.recyclerTimeline.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recordAdapter
        }
        
        // 筛选
        binding.chipAll.setOnClickListener { filterByType(null) }
        binding.chipCheckin.setOnClickListener { filterByType(RecordType.CHECKIN) }
        binding.chipProblem.setOnClickListener { filterByType(RecordType.PROBLEM) }
        binding.chipDamage.setOnClickListener { filterByType(RecordType.DAMAGE) }
        binding.chipRepair.setOnClickListener { filterByType(RecordType.REPAIR) }
    }
    
    private fun observeData() {
        viewModel.records.observe(viewLifecycleOwner) { records ->
            val filtered = if (currentFilter != null) {
                records.filter { it.type == currentFilter }
            } else {
                records
            }
            recordAdapter.submitList(filtered)
            binding.textNoRecords.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun filterByType(type: RecordType?) {
        currentFilter = type
        
        // 更新选中状态
        binding.chipAll.isChecked = type == null
        binding.chipCheckin.isChecked = type == RecordType.CHECKIN
        binding.chipProblem.isChecked = type == RecordType.PROBLEM
        binding.chipDamage.isChecked = type == RecordType.DAMAGE
        binding.chipRepair.isChecked = type == RecordType.REPAIR
        
        // 刷新列表
        viewModel.records.value?.let { records ->
            val filtered = if (type != null) {
                records.filter { it.type == type }
            } else {
                records
            }
            recordAdapter.submitList(filtered)
        }
    }
    
    private fun showRecordDetail(record: com.rentproof.app.data.Record) {
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
    
    private fun deleteRecord(record: com.rentproof.app.data.Record) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认删除")
            .setMessage("删除后无法恢复，确定要删除这条存证记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteRecord(record)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
