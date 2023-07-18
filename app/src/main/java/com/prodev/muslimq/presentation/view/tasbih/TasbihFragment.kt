package com.prodev.muslimq.presentation.view.tasbih

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.capitalizeEachWord
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.databinding.DialogSearchAyahBinding
import com.prodev.muslimq.databinding.FragmentTasbihBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasbihFragment : BaseFragment<FragmentTasbihBinding>(FragmentTasbihBinding::inflate) {

    private val tasbihViewModel: TasbihViewModel by viewModels()

    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }

    private var totalSize = 0
    private var successDelete = false
    private var successAdd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(DzikirFragment.REQUEST_DELETE) { _, bundle ->
            val result = bundle.getBoolean(DzikirFragment.BUNDLE_DELETE)
            if (result) successDelete = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAllDzikir()
    }

    private fun getAllDzikir() {
        val context = requireContext()
        val colorWhiteBase = ContextCompat.getColorStateList(context, R.color.white_base)
        val colorGreenBase = ContextCompat.getColorStateList(context, R.color.green_base)
        val colorBlack = ContextCompat.getColor(context, R.color.black)
        val cornerRadius = 18F
        val strokeWidth = 3f

        tasbihViewModel.getDzikirList.observe(viewLifecycleOwner) { listOfDzikir ->
            totalSize = listOfDzikir.size
            binding.apply {
                cgDzikir.removeAllViews()
                chipAdd.setOnClickListener { showInputDzikirDialog(context) }
                ivSettings.setOnClickListener {
                    findNavController().navigate(R.id.action_tasbihFragment_to_dzikirFragment)
                    tasbihViewModel.totalSizeVM = totalSize
                }

                tasbihViewModel.apply {
                    listOfDzikir.forEachIndexed { index, dzikir ->
                        val chip = Chip(context).apply {
                            text = capitalizeEachWord(dzikir.dzikirName)
                            isCheckable = true
                            chipCornerRadius = cornerRadius
                            isCheckedIconVisible = false
                            chipBackgroundColor = colorWhiteBase
                            chipStrokeColor = colorGreenBase
                            chipStrokeWidth = strokeWidth
                        }

                        if (totalSizeVM != totalSize) {
                            if (successDelete) {
                                chip.isChecked = index == 0
                                currentIndexVM = index
                                selectedItemIndexVM = index
                                dzikirCountVM = 0

                                svChip.post {
                                    // follow item index
                                    svChip.smoothScrollTo(
                                        cgDzikir.getChildAt(index).left,
                                        cgDzikir.getChildAt(index).top
                                    )
                                }

                                successDelete = false
                            } else {
                                chip.isChecked = currentIndexVM == index
                                if (successAdd) {
                                    svChip.post {
                                        // follow item index
                                        svChip.smoothScrollTo(
                                            cgDzikir.getChildAt(currentIndexVM).left,
                                            cgDzikir.getChildAt(currentIndexVM).top
                                        )
                                    }

                                    successAdd = false
                                }
                            }
                        } else {
                            chip.isChecked = currentIndexVM == index
                        }

                        if (chip.isChecked) {
                            chip.chipBackgroundColor = colorGreenBase
                            chip.setTextColor(colorWhiteBase)
                            dzikirNameVM = dzikir.dzikirName
                            tvDzikir.text = capitalizeEachWord(dzikirNameVM)
                        }

                        chip.setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked) {
                                for (i in 0 until cgDzikir.childCount) {
                                    val checkedChip = cgDzikir.getChildAt(i) as? Chip
                                    if (checkedChip?.id != buttonView.id) {
                                        checkedChip?.isChecked = false
                                        checkedChip?.chipBackgroundColor = colorWhiteBase
                                        checkedChip?.setTextColor(colorBlack)
                                    }
                                }

                                chip.chipBackgroundColor = colorGreenBase
                                chip.setTextColor(colorWhiteBase)

                                selectedItemIndexVM = index
                                currentIndexVM = index
                                dzikirCountVM = 0
                                tvCountTasbih.text = dzikirCountVM.toString()
                                dzikirNameVM = dzikir.dzikirName
                                tvDzikir.text = capitalizeEachWord(dzikirNameVM)
                            }
                        }

                        cgDzikir.apply {
                            isSingleSelection = true
                            addView(chip)
                        }
                    }

                    val vibrator = vibrateApp(context)
                    val duration = 200L

                    fabTasbih.setOnClickListener {
                        if (dzikirCountVM == 33) {
                            val currentIndex = selectedItemIndexVM
                            val maxIndex = cgDzikir.childCount - 1

                            if (currentIndex < maxIndex) {
                                val targetIndex = currentIndex + 1

                                val currentChip = cgDzikir.getChildAt(currentIndex) as Chip
                                val targetChip = cgDzikir.getChildAt(targetIndex) as Chip

                                currentChip.isChecked = false
                                targetChip.isChecked = true
                                selectedItemIndexVM = targetIndex
                                currentIndexVM = targetIndex
                            }

                            svChip.post {
                                // follow item index
                                svChip.smoothScrollTo(
                                    cgDzikir.getChildAt(selectedItemIndexVM).left,
                                    cgDzikir.getChildAt(selectedItemIndexVM).top
                                )
                            }

                            if (selectedItemIndexVM != maxIndex) {
                                dzikirCountVM = 0
                                tvCountTasbih.text = dzikirCountVM.toString()
                            }
                        } else {
                            dzikirCountVM++
                            tvCountTasbih.text = dzikirCountVM.toString()
                        }

                        if (dzikirCountVM > 32) {
                            if (selectedItemIndexVM <= cgDzikir.childCount - 1) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(
                                        VibrationEffect.createOneShot(
                                            duration, VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )
                                } else {
                                    @Suppress("DEPRECATION") vibrator.vibrate(duration)
                                }
                            }
                        }
                    }

                    tvCountTasbih.text = dzikirCountVM.toString()

                    btnReset.setOnClickListener {
                        dzikirCountVM = 0
                        tvCountTasbih.text = dzikirCountVM.toString()
                    }
                }
            }
        }
    }

    private fun vibrateApp(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE
            ) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun showInputDzikirDialog(context: Context) {
        val dialogLayout = DialogSearchAyahBinding.inflate(layoutInflater)
        val etDzikir = dialogLayout.etAyah
        val btnSave = dialogLayout.btnSearch
        etDzikir.inputType = InputType.TYPE_CLASS_TEXT
        etDzikir.hint = getString(R.string.input_dzikir)
        btnSave.text = getString(R.string.save)
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            etDzikir.setOnEditorActionListener { _, _, _ -> btnSave.performClick() }
            btnSave.setOnClickListener {
                val dzikirName = etDzikir.text.toString()
                val state = dzikirName.isNotEmpty()

                (activity as MainActivity).customSnackbar(
                    state = state,
                    context = context,
                    view = binding.root,
                    message = if (state) "Dzikir berhasil ditambahkan" else "Tidak ada dzikir yang ditambahkan"
                )

                if (dzikirName.isNotEmpty()) {
                    tasbihViewModel.insertDzikir(TasbihEntity(dzikirName))
                    hideKeyboard(requireActivity())

                    tasbihViewModel.apply {
                        binding.apply {
                            val cgSize = cgDzikir.childCount - 1
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                selectedItemIndexVM = cgSize + 1
                                currentIndexVM = cgSize + 1
                                dzikirCountVM = 0
                            }
                        }
                    }

                    successAdd = true
                }

                dismiss()
            }
            show()
        }
    }
}