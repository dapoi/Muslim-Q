package com.prodev.muslimq.presentation.view.tasbih

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.databinding.FragmentTasbihBinding
import com.prodev.muslimq.helper.capitalizeEachWord
import com.prodev.muslimq.helper.defaultDzikir
import com.prodev.muslimq.helper.vibrateApp
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasbihFragment : BaseFragment<FragmentTasbihBinding>(FragmentTasbihBinding::inflate) {

    private val tasbihViewModel: TasbihViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private var totalSize: Int = 0
    private var successDeleteOrUpdate: Boolean = false
    private var selectedType: DzikirType = DzikirType.DEFAULT
    private var selectedDzikir: TasbihEntity = defaultDzikir()[0]
    private var currentDzikirList: MutableList<TasbihEntity> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(DzikirFragment.REQUEST_DELETE_OR_UPDATE) { _, bundle ->
            val result = bundle.getBoolean(DzikirFragment.BUNDLE_DELETE_OR_UPDATE)
            if (result) successDeleteOrUpdate = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }
        getAllDzikir()
    }

    private fun getAllDzikir() {
        val context = requireContext()
        val colorWhiteBaseState = ContextCompat.getColorStateList(context, R.color.white_base)
        val colorWhiteAlwasyState = ContextCompat.getColorStateList(context, R.color.white_always)
        val colorGreenBaseState = ContextCompat.getColorStateList(context, R.color.green_base)
        val colorGreenBase = ContextCompat.getColor(context, R.color.green_base)
        val colorBlack = ContextCompat.getColor(context, R.color.black)
        val colorLightGray = ContextCompat.getColor(context, R.color.light_gray)

        dataStoreViewModel.getSelectedDzikirType.observe(viewLifecycleOwner) {
            selectedType = DzikirType.entries.toTypedArray()[it]
        }

        if (selectedDzikir.maxCount < tasbihViewModel.dzikirCountVM) {
            tasbihViewModel.apply {
                dzikirCountVM = 0
                binding.tvCountTasbih.text = dzikirCountVM.toString()
            }
        }

        initUI(
            colorWhiteBaseState,
            colorWhiteAlwasyState,
            colorGreenBaseState,
            colorBlack,
            colorGreenBase,
            colorLightGray
        )
    }

    private fun initUI(
        colorWhiteBaseState: ColorStateList?,
        colorWhiteAlwaysState: ColorStateList?,
        colorGreenBaseState: ColorStateList?,
        colorBlack: Int,
        colorGreenBase: Int,
        colorLightGray: Int,
    ) {
        tasbihViewModel.getDzikirList.observe(viewLifecycleOwner) { listOfDzikir ->
            //check if custom is empty, change selected type to default
            if (listOfDzikir.none { it.dzikirType == selectedType }) {
                dataStoreViewModel.saveSelectedDzikirType(DzikirType.DEFAULT)
                selectedType = DzikirType.DEFAULT
            }

            totalSize = listOfDzikir.filter { it.dzikirType == selectedType }.size

            binding.apply {
                cgDzikir.removeAllViews()

                chipType.text = capitalizeEachWord(selectedType.value)

                chipType.setOnClickListener {
                    findNavController().navigate(
                        TasbihFragmentDirections.actionTasbihFragmentToDzikirFragment()
                    )
                    tasbihViewModel.totalSizeVM = totalSize
                }

                currentDzikirList.clear()
                listOfDzikir.filter {
                    it.dzikirType == (selectedType)
                }.forEachIndexed { index, dzikir ->
                    currentDzikirList.add(dzikir)

                    val chip = Chip(context).apply {
                        text = capitalizeEachWord(dzikir.dzikirName)
                        isCheckable = true
                        chipCornerRadius = 18f
                        isCheckedIconVisible = false
                        chipBackgroundColor = colorWhiteBaseState
                        chipStrokeColor = colorGreenBaseState
                        chipStrokeWidth = 3f
                    }

                    cgDzikir.apply {
                        isSingleSelection = true
                        addView(chip)
                    }

                    checkStateChipGroup(
                        chip,
                        index,
                        dzikir,
                        colorWhiteAlwaysState,
                        colorGreenBaseState,
                        colorGreenBase,
                        colorLightGray
                    )

                    chipGroupClicked(
                        index,
                        dzikir,
                        chip,
                        colorWhiteBaseState,
                        colorWhiteAlwaysState,
                        colorBlack,
                        colorGreenBaseState,
                        colorGreenBase,
                        colorLightGray
                    )
                }
            }
        }
    }

    private fun checkStateChipGroup(
        chip: Chip,
        index: Int,
        dzikir: TasbihEntity,
        colorWhiteAlwaysState: ColorStateList?,
        colorGreenBaseState: ColorStateList?,
        colorGreenBase: Int,
        colorLightGray: Int
    ) {
        if (tasbihViewModel.totalSizeVM != totalSize) {
            if (successDeleteOrUpdate) {
                chip.isChecked = index == 0
                tasbihViewModel.apply {
                    selectedItemIndexVM = index
                    dzikirCountVM = 0
                }

                binding.svChip.apply {
                    post {
                        smoothScrollTo(
                            binding.cgDzikir.getChildAt(index).left,
                            binding.cgDzikir.getChildAt(index).top
                        )
                    }
                }

                successDeleteOrUpdate = false
            } else {
                chip.isChecked = tasbihViewModel.selectedItemIndexVM == index
            }
        } else {
            chip.isChecked = tasbihViewModel.selectedItemIndexVM == index
        }

        if (chip.isChecked) {
            chip.chipBackgroundColor = colorGreenBaseState
            chip.setTextColor(colorWhiteAlwaysState)
            tasbihViewModel.dzikirNameVM = dzikir.dzikirName
            changeDetail(dzikir, colorGreenBase, colorLightGray)
        }
    }

    private fun chipGroupClicked(
        index: Int,
        dzikir: TasbihEntity,
        chip: Chip,
        colorWhiteBaseState: ColorStateList?,
        colorWhiteAlwaysState: ColorStateList?,
        colorBlack: Int,
        colorGreenBaseState: ColorStateList?,
        colorGreenBase: Int,
        colorLightGray: Int
    ) {
        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                for (i in 0 until binding.cgDzikir.childCount) {
                    val checkedChip = binding.cgDzikir.getChildAt(i) as? Chip
                    if (checkedChip?.id != buttonView.id) {
                        checkedChip?.isChecked = false
                        checkedChip?.chipBackgroundColor = colorWhiteBaseState
                        checkedChip?.setTextColor(colorBlack)
                    }
                }

                chip.chipBackgroundColor = colorGreenBaseState
                chip.setTextColor(colorWhiteAlwaysState)

                tasbihViewModel.apply {
                    selectedItemIndexVM = index
                    dzikirNameVM = dzikir.dzikirName
                    dzikirCountVM = 0
                    binding.tvCountTasbih.text = dzikirCountVM.toString()
                }

                changeDetail(dzikir, colorGreenBase, colorLightGray)
            }
        }
    }

    private fun changeDetail(dzikir: TasbihEntity, colorGreenBase: Int, colorLightGray: Int) {
        binding.apply {
            tvDzikirArab.isVisible = dzikir.arabText != null
            tvDzikirMeaning.isVisible = dzikir.arabText != null && dzikir.translation != null
            tvDzikirLatin.isVisible =
                dzikir.dzikirName.isNotEmpty() && selectedType == DzikirType.CUSTOM
            tvDzikirArab.text = dzikir.arabText
            tvDzikirMeaning.text = dzikir.translation
            tvDzikirLatin.text = dzikir.dzikirName
            tvMaxCountNew.text = getString(R.string.target_count, dzikir.maxCount.toString())
            selectedDzikir = dzikir
        }

        dataStoreViewModel.apply {
            getHapticFeedbackState.observe(viewLifecycleOwner) { hapticActive ->
                interactTasbih(hapticActive, dzikir.maxCount)

                binding.apply {
                    if (hapticActive) {
                        ivFeedback.setBackgroundColor(colorGreenBase)
                    } else {
                        ivFeedback.setBackgroundColor(colorLightGray)
                    }

                    ivFeedback.setOnClickListener {
                        saveHapticFeedbackState(!hapticActive)

                        Toast.makeText(
                            requireContext(),
                            if (hapticActive) "Efek getar dimatikan" else "Efek getar diaktifkan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                        }

                        svChip.post {
                            svChip.smoothScrollTo(
                                cgDzikir.getChildAt(selectedItemIndexVM).left,
                                cgDzikir.getChildAt(selectedItemIndexVM).top
                            )
                        }

                        svDetail.post {
                            svDetail.scrollTo(0, 0)
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
}