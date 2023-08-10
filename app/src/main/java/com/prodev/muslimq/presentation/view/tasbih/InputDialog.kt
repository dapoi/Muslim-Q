package com.prodev.muslimq.presentation.view.tasbih

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.databinding.DialogSearchBinding

class InputDialog {

    fun showInputDialog(
        listOfDzikir: List<TasbihEntity> = listOf(),
        inflater: LayoutInflater,
        context: Context,
        result: (String, String, List<TasbihEntity>?) -> Unit
    ) {
        val curvedDialog by lazy {
            AlertDialog.Builder(context, R.style.CurvedDialog)
        }

        val dialogLayout = DialogSearchBinding.inflate(inflater)
        dialogLayout.tilSearch.isStartIconVisible = false
        dialogLayout.tilSearch2.isVisible = true

        val etDzikirName = dialogLayout.etSearch
        val etDzikirCount = dialogLayout.etSearch2
        val btnSave = dialogLayout.btnSearch
        etDzikirName.inputType = InputType.TYPE_CLASS_TEXT
        etDzikirCount.inputType = InputType.TYPE_CLASS_NUMBER
        etDzikirName.hint = context.getString(R.string.input_dzikir)
        etDzikirCount.hint = context.getString(R.string.input_max_dzikir)

        btnSave.text = context.getString(R.string.save)
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            etDzikirName.setOnEditorActionListener { _, _, _ -> btnSave.performClick() }
            btnSave.setOnClickListener {
                val dzikir = etDzikirName.text.toString()
                val count = etDzikirCount.text.toString()
                result(dzikir, count, listOfDzikir)
                dismiss()
            }
            show()
        }
    }
}