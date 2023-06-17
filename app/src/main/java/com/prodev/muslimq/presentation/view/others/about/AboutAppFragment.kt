package com.prodev.muslimq.presentation.view.others.about

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.BuildConfig
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentAboutAppBinding
import com.prodev.muslimq.presentation.view.BaseFragment

class AboutAppFragment : BaseFragment<FragmentAboutAppBinding>(FragmentAboutAppBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

            btnLicense.setOnClickListener {
                Toast.makeText(requireContext(), "License", Toast.LENGTH_SHORT).show()
            }
        }
    }
}