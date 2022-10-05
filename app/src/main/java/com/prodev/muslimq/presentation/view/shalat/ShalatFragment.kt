package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentShalatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShalatFragment : Fragment() {

    private lateinit var binding: FragmentShalatBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
        } else {
            binding = FragmentShalatBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivIconChoose.setOnClickListener {
            findNavController().navigate(R.id.action_shalatFragment_to_shalatCityFragment)
        }
    }
}