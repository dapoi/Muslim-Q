package com.prodev.muslimq.presentation.view.splashscreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentSplashScreenBinding
import kotlinx.coroutines.launch

class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!

    private val duration = 3000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(
                    R.id.action_splashScreenFragment_to_baseActivity
                )
                requireActivity().finish()
            }, duration)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}