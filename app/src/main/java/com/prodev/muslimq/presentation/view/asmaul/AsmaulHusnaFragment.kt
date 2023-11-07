package com.prodev.muslimq.presentation.view.asmaul

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.prodev.muslimq.databinding.FragmentAsmaulHusnaBinding
import com.prodev.muslimq.presentation.adapter.AsmaulHusnaAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.AsmaulHusnaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AsmaulHusnaFragment : BaseFragment<FragmentAsmaulHusnaBinding>(
    FragmentAsmaulHusnaBinding::inflate
) {

    private val asmaulHusnaViewModel: AsmaulHusnaViewModel by viewModels()
    private val asmaulHusnaAdapter: AsmaulHusnaAdapter by lazy { AsmaulHusnaAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        initAdapter()
        initViewModel()
    }

    private fun initAdapter() {
        binding.rvAsmaulHusna.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = asmaulHusnaAdapter
        }
    }

    private fun initViewModel() {
        asmaulHusnaViewModel.getAsmaulHusna().observe(viewLifecycleOwner) { result ->
            asmaulHusnaAdapter.submitList(result)
        }
    }
}