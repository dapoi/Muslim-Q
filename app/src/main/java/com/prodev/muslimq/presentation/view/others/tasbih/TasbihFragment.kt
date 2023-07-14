package com.prodev.muslimq.presentation.view.others.tasbih

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentTasbihBinding
import com.prodev.muslimq.presentation.view.BaseFragment

class TasbihFragment : BaseFragment<FragmentTasbihBinding>(FragmentTasbihBinding::inflate) {

    private var selectedItemIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val listOfDzikir = listOf(
            "Subhanallah",
            "Alhamdulillah",
            "Allahu Akbar",
            "La Ilaha Illallah",
            "Astaghfirullah",
            "Laa Haula Wala Quwwata Illa Billah"
        )

        val context = requireContext()
        val colorWhiteBase = ContextCompat.getColorStateList(context, R.color.white_base)
        val colorGreenBase = ContextCompat.getColorStateList(context, R.color.green_base)
        val colorBlack = ContextCompat.getColor(context, R.color.black)
        val chipCornerRadius = 18F
        val chipStrokeWidth = 3f

        listOfDzikir.forEachIndexed { index, dzikir ->
            val chip = Chip(context).apply {
                text = dzikir
                isCheckable = true
                this.chipCornerRadius = chipCornerRadius
                isCheckedIconVisible = false
                chipBackgroundColor = colorWhiteBase
                chipStrokeColor = colorGreenBase
                this.chipStrokeWidth = chipStrokeWidth
            }

            if (index == 0) {
                chip.isChecked = true
                chip.chipBackgroundColor = colorGreenBase
                chip.setTextColor(colorWhiteBase)
                Toast.makeText(context, dzikir, Toast.LENGTH_SHORT).show()
            }

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    for (i in 0 until binding.cgDzikir.childCount) {
                        val checkedChip = binding.cgDzikir.getChildAt(i) as? Chip
                        if (checkedChip?.id != buttonView.id) {
                            checkedChip?.isChecked = false
                            checkedChip?.chipBackgroundColor = colorWhiteBase
                            checkedChip?.setTextColor(colorBlack)
                        }
                    }

                    chip.chipBackgroundColor = colorGreenBase
                    chip.setTextColor(colorWhiteBase)
                    Toast.makeText(context, dzikir, Toast.LENGTH_SHORT).show()

                    selectedItemIndex = index
                }
            }

            binding.cgDzikir.apply {
                isSingleSelection = true
                addView(chip)
            }
        }

        binding.chipAdd.setOnClickListener {
            val currentIndex = selectedItemIndex
            val maxIndex = binding.cgDzikir.childCount - 1

            if (currentIndex < maxIndex) {
                val targetIndex = currentIndex + 1

                val currentChip = binding.cgDzikir.getChildAt(currentIndex) as Chip
                val targetChip = binding.cgDzikir.getChildAt(targetIndex) as Chip

                currentChip.isChecked = false
                targetChip.isChecked = true
                selectedItemIndex = targetIndex
            }

            binding.svChip.post {
                // follow item index
                binding.svChip.smoothScrollTo(
                    binding.cgDzikir.getChildAt(selectedItemIndex).left,
                    binding.cgDzikir.getChildAt(selectedItemIndex).top
                )
            }
        }
    }
}