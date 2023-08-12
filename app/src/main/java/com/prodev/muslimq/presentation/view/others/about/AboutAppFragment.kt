package com.prodev.muslimq.presentation.view.others.about

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.BuildConfig
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.DialogInfoSurahBinding
import com.prodev.muslimq.databinding.FragmentAboutAppBinding
import com.prodev.muslimq.presentation.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAppFragment : BaseFragment<FragmentAboutAppBinding>(FragmentAboutAppBinding::inflate) {

    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            tvVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

            btnLicense.setOnClickListener {
                DialogInfoSurahBinding.inflate(layoutInflater).apply {
                    tvInfoTitle.visibility = View.GONE
                    tvInfoMessage.text = getString(R.string.mit_license)
                    with(curvedDialog.create()) {
                        setView(root)
                        show()

                        tvInfoClose.setOnClickListener { dismiss() }
                    }
                }
            }
        }
    }

    private fun hideSystemUI(state: Boolean) {
        val window = requireActivity().window
        if (state) {
            window.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.green_button)
            window.navigationBarColor =
                ContextCompat.getColor(requireActivity(), R.color.green_button)
        } else {
            window.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.white_base)
            window.navigationBarColor =
                ContextCompat.getColor(requireActivity(), R.color.white_second)
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI(true)
    }

    override fun onDestroyView() {
        hideSystemUI(false)
        super.onDestroyView()
    }
}