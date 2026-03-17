package com.rentproof.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rentproof.app.data.Record
import com.rentproof.app.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(
    private val onItemClick: (Record) -> Unit,
    private val onItemDelete: (Record) -> Unit
) : ListAdapter<Record, RecordAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemDelete(getItem(position))
                }
            }
        }
        
        fun bind(record: Record) {
            binding.textType.text = record.typeName
            binding.textNote.text = record.note.ifEmpty { "无备注" }
            
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            binding.textTime.text = dateFormat.format(Date(record.createdAt))
            
            // 根据类型设置颜色
            val colorRes = when (record.type) {
                com.rentproof.app.data.RecordType.CHECKIN -> android.R.color.holo_blue_light
                com.rentproof.app.data.RecordType.PROBLEM -> android.R.color.holo_orange_light
                com.rentproof.app.data.RecordType.DAMAGE -> android.R.color.holo_red_light
                com.rentproof.app.data.RecordType.REPAIR -> android.R.color.holo_green_light
                com.rentproof.app.data.RecordType.OTHER -> android.R.color.darker_gray
            }
            
            binding.textType.setBackgroundColor(
                binding.root.context.getColor(colorRes)
            )
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem == newItem
        }
    }
}
