package com.prodev.muslimq.presentation.view.tasbih

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.databinding.DialogSearchAyahBinding

class InputDialog {
    fun showInputDialog(
        isDzikir: Boolean,
        maxCount: Int = 0,
        listOfDzikir: List<TasbihEntity> = listOf(),
        inflater: LayoutInflater,
        context: Context,
        result: (String, List<TasbihEntity>?) -> Unit
    ) {
        val curvedDialog by lazy {
            AlertDialog.Builder(context, R.style.CurvedDialog)
        }
        val dialogLayout = DialogSearchAyahBinding.inflate(inflater)
        val etInput = dialogLayout.etAyah
        val btnSave = dialogLayout.btnSearch
        etInput.inputType = if (isDzikir) {
            InputType.TYPE_CLASS_TEXT
        } else {
            InputType.TYPE_CLASS_NUMBER
        }
        etInput.hint = if (isDzikir) {
            context.getString(R.string.input_dzikir)
        } else {
            context.getString(R.string.input_max_dzikir)
        }
        etInput.setText(if (isDzikir) "" else maxCount.toString())
        btnSave.text = context.getString(R.string.save)
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            etInput.setOnEditorActionListener { _, _, _ -> btnSave.performClick() }
            btnSave.setOnClickListener {
                val dzikir = etInput.text.toString()
                if (isDzikir) {
                    result.invoke(dzikir, listOfDzikir)
                } else {
                    result.invoke(dzikir, null)
                }

                dismiss()
            }
            show()
        }
    }
}