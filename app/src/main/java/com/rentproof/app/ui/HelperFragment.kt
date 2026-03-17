package com.rentproof.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.rentproof.app.databinding.FragmentHelperBinding
import com.rentproof.app.databinding.DialogFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class HelperFragment : Fragment() {
    
    private var _binding: FragmentHelperBinding? = null
    private val binding get() = _binding!!
    
    private val templates = listOf(
        Template("deposit", "押金退还申请书", "💰"),
        Template("repair", "房屋维修催告函", "🔧"),
        Template("terminate", "解除合同通知书", "📄"),
        Template("complaint", "投诉举报信", "📮")
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelperBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTemplates()
    }
    
    private fun setupTemplates() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            templates.map { "${it.icon} ${it.name}" }
        )
        
        binding.listTemplates.adapter = adapter
        binding.listTemplates.setOnItemClickListener { _, _, position, _ ->
            showTemplateForm(templates[position])
        }
    }
    
    private fun showTemplateForm(template: Template) {
        val dialogBinding = DialogFormBinding.inflate(layoutInflater)
        
        // 根据模板类型设置表单
        when (template.id) {
            "deposit" -> {
                dialogBinding.editField1.hint = "房东姓名"
                dialogBinding.editField2.hint = "押金金额"
                dialogBinding.editField3.hint = "收款账户"
                dialogBinding.editField3.visibility = View.VISIBLE
            }
            "repair" -> {
                dialogBinding.editField1.hint = "房东姓名"
                dialogBinding.editField2.hint = "问题描述"
                dialogBinding.editField3.hint = "要求维修天数"
                dialogBinding.editField3.visibility = View.VISIBLE
            }
            "terminate" -> {
                dialogBinding.editField1.hint = "房东姓名"
                dialogBinding.editField2.hint = "解除原因"
                dialogBinding.editField3.hint = "计划搬离日期"
                dialogBinding.editField3.visibility = View.VISIBLE
            }
            "complaint" -> {
                dialogBinding.editField1.hint = "投诉部门"
                dialogBinding.editField2.hint = "问题描述"
                dialogBinding.editField3.hint = "证据材料"
                dialogBinding.editField3.visibility = View.VISIBLE
            }
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(template.name)
            .setView(dialogBinding.root)
            .setPositiveButton("生成") { _, _ ->
                val doc = generateDocument(template, dialogBinding)
                showGeneratedDoc(doc)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun generateDocument(
        template: Template,
        binding: DialogFormBinding
    ): String {
        val field1 = binding.editField1.text.toString()
        val field2 = binding.editField2.text.toString()
        val field3 = binding.editField3.text.toString()
        val date = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())
        
        return when (template.id) {
            "deposit" -> """
                押金退还申请书
                
                尊敬的房东 $field1：
                
                本人于 _____ 年 _____ 月 _____ 日与您签订了房屋租赁合同，并支付押金人民币 $field2 元。
                
                现租赁合同已到期/终止，本人已按合同约定完成房屋交接，房屋及设施完好。根据合同约定，请您在收到本申请后 _____ 日内退还押金。
                
                收款账户信息：$field3
                
                申请人：__________________
                日期：$date
                
                ---
                本文档由「租房存证」App生成
            """.trimIndent()
            
            "repair" -> """
                房屋维修催告函
                
                尊敬的房东 $field1：
                
                本人承租您的房屋，现就房屋维修事宜通知如下：
                
                存在问题描述：
                $field2
                
                根据《民法典》第七百一十二条规定，出租人应当履行租赁物的维修义务。请您在收到本函后 $field3 日内完成维修。
                
                如逾期未维修，本人将自行维修，费用从租金中扣除。
                
                承租人：__________________
                日期：$date
                
                ---
                本文档由「租房存证」App生成
            """.trimIndent()
            
            "terminate" -> """
                解除合同通知书
                
                尊敬的房东 $field1：
                
                本人承租您的房屋，现因以下原因申请提前解除租赁合同：
                
                解除原因：$field2
                
                计划搬离日期：$field3
                
                请您在收到本通知后安排房屋交接，并按合同约定退还押金。
                
                承租人：__________________
                日期：$date
                
                ---
                本文档由「租房存证」App生成
            """.trimIndent()
            
            "complaint" -> """
                投诉举报信
                
                致：$field1
                
                投诉事项：
                $field2
                
                证据材料：
                $field3
                
                投诉请求：
                请相关部门依法调查处理，维护投诉人的合法权益。
                
                投诉人：__________________
                日期：$date
                
                ---
                本文档由「租房存证」App生成
            """.trimIndent()
            
            else -> ""
        }
    }
    
    private fun showGeneratedDoc(doc: String) {
        binding.textDocument.text = doc
        binding.textDocument.visibility = View.VISIBLE
        binding.scrollView.visibility = View.VISIBLE
        
        binding.btnCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("维权文档", doc)
            clipboard.setPrimaryClip(clip)
            
            android.widget.Toast.makeText(
                requireContext(),
                "已复制到剪贴板",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    data class Template(val id: String, val name: String, val icon: String)
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
