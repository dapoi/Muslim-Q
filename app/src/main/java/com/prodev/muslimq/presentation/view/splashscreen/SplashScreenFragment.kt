package com.prodev.muslimq.presentation.view.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentSplashScreenBinding

class SplashScreenFragment : Fragment() {

    private val binding: FragmentSplashScreenBinding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentSplashScreenBinding.inflate(layoutInflater)
    }

    private val duration = 3000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(
                R.id.action_splashScreenFragment_to_baseActivity
            )
            requireActivity().finish()
        }, duration)
    }
}