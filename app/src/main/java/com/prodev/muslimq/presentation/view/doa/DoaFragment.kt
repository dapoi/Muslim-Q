package com.prodev.muslimq.presentation.view.doa

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.databinding.FragmentDoaBinding
import com.prodev.muslimq.presentation.adapter.DoaAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DoaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoaFragment : BaseFragment<FragmentDoaBinding>(FragmentDoaBinding::inflate) {

    private lateinit var doaAdapter: DoaAdapter

    private val doaViewModel: DoaViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.svDoa.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val imm =
                    requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                doaAdapter.filter.filter(newText)
                return true
            }
        })

        setAdapter()
        setViewModel()
    }

    private fun setAdapter() {
        doaAdapter = DoaAdapter(binding.emptyState.root)
        binding.rvDoa.apply {
            adapter = doaAdapter
            layoutManager = LinearLayoutManager(context)
            (binding.rvDoa.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            setHasFixedSize(true)
        }
    }

    private fun setViewModel() {
        doaViewModel.getDoa().observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    doaAdapter.setDoa(it.data!!)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvDoa.visibility = View.GONE
                }
            }
        }
    }
}