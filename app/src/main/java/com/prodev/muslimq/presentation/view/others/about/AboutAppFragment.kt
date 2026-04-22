package com.prodev.muslimq.presentation.view.others.about

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        // Safely extract system bar physical height using modern WindowInsets without deprecated APIs
        val insets = ViewCompat.getRootWindowInsets(requireActivity().window.decorView)
        val topPadding = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        val bottomPadding = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        binding.root.setPadding(0, topPadding, 0, bottomPadding)

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
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        if (state) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            window.statusBarColor = ContextCompat.getColor(
                requireContext(), R.color.white_base
            )
            window.navigationBarColor = ContextCompat.getColor(
                requireContext(), R.color.white_second
            )
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI(true)
    }

    override fun onPause() {
        super.onPause()
        hideSystemUI(false)
    }
}