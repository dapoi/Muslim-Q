package com.prodev.muslimq.presentation.view.others

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentBottomSheetMuadzinBinding

class BottomSheetMuadzinFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetMuadzinBinding? = null
    private val binding get() = _binding!!

    private var countClick = 0
    private var selectedChip = emptyMap<String, String>()
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var callback: BottomSheetMuadzinCallback

    internal fun setBottomSheetMuadzinCallback(callback: BottomSheetMuadzinCallback) {
        this.callback = callback
    }

    interface BottomSheetMuadzinCallback {
        fun onMuadzinSelected(muadzinRegular: String, muadzinShubuh: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetMuadzinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val muadzinRegular = arguments?.getStringArray(KEY_MUADZIN_REGULAR)
        val muadzinFajr = arguments?.getStringArray(KEY_MUADZIN_FAJR)
        val adzanName = arguments?.getStringArray(KEY_MUADZIN_SELECTED)

        initChipGroup(muadzinRegular, adzanName, binding.cgMuadzinRegular, CHIP_TYPE_REGULAR)
        initChipGroup(muadzinFajr, adzanName, binding.cgMuadzinShubuh, CHIP_TYPE_FAJR)
    }

    private fun initChipGroup(
        listMuadzin: Array<String>?,
        adzanName: Array<String>?,
        chipGroup: ChipGroup,
        chipType: String
    ) {
        val context = requireContext()
        val colorWhiteBackground = ContextCompat.getColorStateList(context, R.color.white_second)
        val colorGreen = ContextCompat.getColorStateList(context, R.color.green_button)
        val colorWhite = ContextCompat.getColor(context, R.color.white_always)
        val colorBlack = ContextCompat.getColor(context, R.color.black)

        listMuadzin?.forEach { muadzin ->
            // create chip
            val chip = Chip(context).apply {
                isCheckable = true
                checkedIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                chipCornerRadius = 18f
                chipBackgroundColor = colorWhiteBackground
                chipStrokeColor = colorGreen
                chipStrokeWidth = 3f
                text = muadzin
            }

            // action when chip is checked
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    for (i in 0 until chipGroup.childCount) {
                        val checkedChip = chipGroup.getChildAt(i) as? Chip
                        if (checkedChip?.id != buttonView.id) {
                            checkedChip?.isChecked = false
                            checkedChip?.chipBackgroundColor = colorWhiteBackground
                            checkedChip?.setTextColor(colorBlack)
                        }
                    }

                    chip.chipBackgroundColor = colorGreen
                    chip.setTextColor(colorWhite)
                    countClick = 0
                    selectedChip = selectedChip.plus(chipType to muadzin)

                    mediaPlayer?.apply {
                        if (isPlaying) {
                            stop()
                            release()
                            mediaPlayer = null
                        }
                    }
                }
            }

            // set chip checked
            chip.isChecked = adzanName?.contains(muadzin) == true

            // set first chip color
            if (chip.isChecked) {
                chip.chipBackgroundColor = colorGreen
                chip.setTextColor(colorWhite)
                countClick++
                selectedChip = selectedChip.plus(chipType to muadzin)
            }

            // action when chip is clicked
            chip.setOnClickListener {
                // reset chip icon each click
                binding.apply {
                    cgMuadzinRegular.resetChipIcon(context, R.drawable.ic_play)
                    cgMuadzinShubuh.resetChipIcon(context, R.drawable.ic_play)
                }

                countClick++

                if (mediaPlayer != null) {
                    mediaPlayer?.apply {
                        if (isPlaying) {
                            stop()
                            release()
                            mediaPlayer = null
                        }
                    }
                } else {
                    if (countClick > 1) {
                        when {
                            muadzin.contains("Ali") -> {
                                playAdzan(context, R.raw.ali_ahmed_mulla, chip)
                            }

                            muadzin.contains("Hafiz") -> {
                                playAdzan(context, R.raw.hafiz_mustafa, chip)
                            }

                            muadzin.contains("Mishary") -> {
                                playAdzan(context, R.raw.mishary_rasyid, chip)
                            }

                            muadzin.contains("Hazim") -> {
                                playAdzan(context, R.raw.abu_hazim_shubuh, chip)
                            }

                            muadzin.contains("Salah") -> {
                                playAdzan(context, R.raw.salah_mansoor_shubuh, chip)
                            }
                        }
                    }
                }
            }

            // add chip to chip group
            chipGroup.isSelectionRequired = true
            chipGroup.addView(chip)

            // action when button save is clicked
            binding.btnSave.setOnClickListener {
                val muadzinRegular = selectedChip[CHIP_TYPE_REGULAR].toString()
                val muadzinFajr = selectedChip[CHIP_TYPE_FAJR].toString()
                callback.onMuadzinSelected(muadzinRegular, muadzinFajr)
                dismiss()
            }
        }
    }

    private fun ChipGroup.resetChipIcon(context: Context, @DrawableRes iconResId: Int) {
        children.forEach { chip ->
            (chip as? Chip)?.let {
                it.checkedIcon = ContextCompat.getDrawable(context, iconResId)
            }
        }
    }

    private fun playAdzan(context: Context, sound: Int, chip: Chip) {
        chip.checkedIcon = ContextCompat.getDrawable(context, R.drawable.ic_pause)
        mediaPlayer = MediaPlayer.create(context, sound).apply {
            isLooping = false
            start()

            setOnCompletionListener {
                chip.checkedIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                stop()
                release()
                mediaPlayer = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
            }
        }
        _binding = null
    }

    companion object {
        const val KEY_MUADZIN_REGULAR = "muadzin_regular"
        const val KEY_MUADZIN_FAJR = "muadzin_fajr"
        const val KEY_MUADZIN_SELECTED = "muadzin_selected"
        private const val CHIP_TYPE_REGULAR = "chip_regular"
        private const val CHIP_TYPE_FAJR = "chip_fajr"
    }
}