package com.prodev.muslimq.presentation.view.tasbih

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.databinding.FragmentDzikirBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.DzikirAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DzikirFragment : BaseFragment<FragmentDzikirBinding>(FragmentDzikirBinding::inflate) {

    private val tasbihViewModel: TasbihViewModel by viewModels()
    private lateinit var dzikirAdapter: DzikirAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        initAdapter()
        initDzikir()
    }

    private fun initAdapter() {
        dzikirAdapter = DzikirAdapter(
            deleteListener = {
                tasbihViewModel.deleteDzikir(it.dzikirName)

                setFragmentResult(REQUEST_DELETE, Bundle().apply {
                    putBoolean(BUNDLE_DELETE, true)
                })

                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Dzikir berhasil dihapus"
                )
            }
        )

        binding.rvDzikir.adapter = dzikirAdapter
    }

    private fun initDzikir() {
        tasbihViewModel.getDzikirList.observe(viewLifecycleOwner) { response ->
            dzikirAdapter.submitList(response)
        }
    }

    companion object {
        const val REQUEST_DELETE = "request_delete"
        const val BUNDLE_DELETE = "bundle_delete"
    }
}