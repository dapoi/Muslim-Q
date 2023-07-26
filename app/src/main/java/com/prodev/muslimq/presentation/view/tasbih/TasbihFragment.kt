package com.prodev.muslimq.presentation.view.tasbih

import android.content.Context
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.InputType
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.core.utils.capitalizeEachWord
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.core.utils.vibrateApp
import com.prodev.muslimq.databinding.DialogSearchAyahBinding
import com.prodev.muslimq.databinding.FragmentTasbihBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasbihFragment : BaseFragment<FragmentTasbihBinding>(FragmentTasbihBinding::inflate) {

    private val tasbihViewModel: TasbihViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private var totalSize = 0
    private var successDelete = false
    private var selectedType = DzikirType.DEFAULT

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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getAllDzikir() {
        val context = requireContext()
        val colorWhiteBaseState = ContextCompat.getColorStateList(context, R.color.white_base)
        val colorGreenBaseState = ContextCompat.getColorStateList(context, R.color.green_base)
        val colorGreenBase = ContextCompat.getColor(context, R.color.green_base)
        val colorBlack = ContextCompat.getColor(context, R.color.black)
        val colorLightGray = ContextCompat.getColor(context, R.color.light_gray)
        val cornerRadius = 18F
        val strokeWidth = 3f

        dataStoreViewModel.getSelectedDzikirType.observe(viewLifecycleOwner){
            selectedType = DzikirType.values()[it]
        }
        tasbihViewModel.getDzikirList.observe(viewLifecycleOwner) { listOfDzikir ->
            //check if custom is empty, change selected type to default
            if (listOfDzikir.none { it.dzikirType == selectedType }){
                dataStoreViewModel.saveSelectedDzikirType(DzikirType.DEFAULT)
                selectedType = DzikirType.DEFAULT
            }
            totalSize = listOfDzikir.filter{it.dzikirType == selectedType}.size
            binding.apply {
                cgDzikir.removeAllViews()
                chipType?.text = capitalizeEachWord(selectedType.value)
                ivSettings.setOnClickListener {
                    findNavController().navigate(R.id.action_tasbihFragment_to_dzikirFragment)
                    tasbihViewModel.totalSizeVM = totalSize
                }
                toggleMaxCount.addOnButtonCheckedListener { group, _, _ ->
                    group.clearChecked()
                }

                tasbihViewModel.apply {
                    listOfDzikir.filter { it.dzikirType == (selectedType) }.forEachIndexed { index, dzikir ->
                        val chip = Chip(context).apply {
                            text = capitalizeEachWord(dzikir.dzikirName)
                            isCheckable = true
                            chipCornerRadius = cornerRadius
                            isCheckedIconVisible = false
                            chipBackgroundColor = colorWhiteBaseState
                            chipStrokeColor = colorGreenBaseState
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
                                chip.isChecked = index == 0
                            }
                        } else {
                            chip.isChecked = currentIndexVM == index
                        }

                        if (chip.isChecked) {
                            chip.chipBackgroundColor = colorGreenBaseState
                            chip.setTextColor(colorWhiteBaseState)
                            dzikirNameVM = dzikir.dzikirName
                            changeDetail(dzikir)
                        }

                        chip.setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked) {
                                for (i in 0 until cgDzikir.childCount) {
                                    val checkedChip = cgDzikir.getChildAt(i) as? Chip
                                    if (checkedChip?.id != buttonView.id) {
                                        checkedChip?.isChecked = false
                                        checkedChip?.chipBackgroundColor = colorWhiteBaseState
                                        checkedChip?.setTextColor(colorBlack)
                                    }
                                }

                                chip.chipBackgroundColor = colorGreenBaseState
                                chip.setTextColor(colorWhiteBaseState)

                                selectedItemIndexVM = index
                                currentIndexVM = index
                                dzikirCountVM = 0
                                tvCountTasbih.text = dzikirCountVM.toString()
                                dzikirNameVM = dzikir.dzikirName
                                changeDetail(dzikir)
                            }
                        }

                        cgDzikir.apply {
                            isSingleSelection = true
                            addView(chip)
                        }
                    }
                }
            }
        }

        dataStoreViewModel.apply {

            getHapticFeedbackState.observe(viewLifecycleOwner) { hapticActive ->
                binding.apply {
                    if (hapticActive) {
                        ivFeedback.setBackgroundColor(colorGreenBase)
                    } else {
                        ivFeedback.setBackgroundColor(colorLightGray)
                    }

                    ivFeedback.setOnClickListener {
                        if (hapticActive) {
                            saveHapticFeedbackState(false)
                        } else {
                            saveHapticFeedbackState(true)
                        }

                        (activity as MainActivity).customSnackbar(
                            state = !hapticActive,
                            context = context,
                            view = binding.root,
                            message = if (hapticActive) "Efek getar dimatikan" else "Efek getar diaktifkan"
                        )
                    }
                }
            }

            getDzikirMaxCount.observe(viewLifecycleOwner) { maxCount ->
                binding.apply {
                    if (maxCount < tasbihViewModel.dzikirCountVM) {
                        tasbihViewModel.apply {
                            dzikirCountVM = 0
                            tvCountTasbih.text = dzikirCountVM.toString()
                        }
                    }
                    tvMaxCount.text = maxCount.toString()
                    tvMaxCount.setOnClickListener {
                        showInputDialog(false, maxCount)
                    }

                    tvPlus.setOnClickListener {
                        saveDzikirMaxCount(maxCount + 1)
                    }

                    tvMinus.setOnClickListener {
                        if (maxCount > 1) {
                            saveDzikirMaxCount(maxCount - 1)
                        } else {
                            return@setOnClickListener
                        }
                    }
                }
            }

            getCombineHapticAndMaxDzikirCount.observe(viewLifecycleOwner) { response ->
                interactTasbih(response.first, response.second)
            }
        }
    }

    private fun changeDetail(dzikir: TasbihEntity) {
        binding.apply {
            dzikir.apply {
                if (arabText != null && translation != null){
                    tvDzikirArab?.visibility = View.VISIBLE
                    tvDzikirMeaning?.visibility = View.VISIBLE
                    tvDzikirArab?.text = arabText
                    tvDzikirMeaning?.text = translation
                } else {
                    tvDzikirArab?.visibility = View.GONE
                    tvDzikirMeaning?.visibility = View.GONE
                }
            }
        }

    }

    private fun interactTasbih(hapticActive: Boolean?, maxDzikir: Int) {
        val isHapticActive = hapticActive ?: true
        binding.apply {
            tasbihViewModel.apply {
                val vibrator = vibrateApp(requireContext())
                val durationDone = 1000L

                fabTasbih.setOnClickListener {
                    fabTasbih.isClickable = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(500L)
                        fabTasbih.isClickable = true
                    }
                    if (isHapticActive && dzikirCountVM < maxDzikir) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } else {
                            @Suppress("DEPRECATION") vibrator.vibrate(100L)
                        }
                    }

                    if (dzikirCountVM == maxDzikir) {
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

                    if (dzikirCountVM > maxDzikir - 1) {
                        if (selectedItemIndexVM <= cgDzikir.childCount - 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        durationDone,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            } else {
                                @Suppress("DEPRECATION") vibrator.vibrate(durationDone)
                            }
                        }
                    }
                }

                tvCountTasbih.text = dzikirCountVM.toString()

                ivRepeat.setOnClickListener {
                    dzikirCountVM = 0
                    tvCountTasbih.text = dzikirCountVM.toString()
                }
            }
        }
    }

    private fun showInputDialog(
        isDzikir: Boolean,
        maxCount: Int = 0,
        listOfDzikir: List<TasbihEntity> = listOf()
    ) {
        InputDialog().showInputDialog(isDzikir, maxCount, listOfDzikir, layoutInflater, requireContext()) { new, oldList ->
            insertMaxDzikir(new)
        }
    }

    private fun insertMaxDzikir(maxDzikir: String) {
        val state = maxDzikir.isNotEmpty() && maxDzikir.toInt() > 0
        val messageSnackbar = when {
            maxDzikir.isEmpty() -> "Jumlah dzikir belum diisi"
            !state -> "Jumlah dzikir tidak valid"
            else -> "Jumlah dzikir berhasil ditambahkan"
        }

        (activity as MainActivity).customSnackbar(
            state = state,
            context = requireContext(),
            view = binding.root,
            message = messageSnackbar
        )

        if (state) {
            dataStoreViewModel.saveDzikirMaxCount(maxDzikir.toInt())
            hideKeyboard(requireActivity())
        }
    }
}