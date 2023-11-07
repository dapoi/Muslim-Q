package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.databinding.FragmentShalatProvinceBinding
import com.prodev.muslimq.helper.hideKeyboard
import com.prodev.muslimq.helper.swipeRefresh
import com.prodev.muslimq.presentation.adapter.ProvinceAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.ProvinceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShalatProvinceFragment :
    BaseFragment<FragmentShalatProvinceBinding>(FragmentShalatProvinceBinding::inflate) {

    private val provinceViewModel: ProvinceViewModel by viewModels()
    private val provinceAdapter: ProvinceAdapter by lazy { ProvinceAdapter(binding.emptyState.root) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            swipeRefresh(
                { provinceViewModel.setProvince() }, srlProvince
            )
        }

        initAdapter()
        initViewModel()
    }

    private fun initAdapter() {
        binding.rvProvince.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = provinceAdapter
        }

        provinceAdapter.onClick = {
            val id = it.id
            val name = it.name
            findNavController().navigate(
                ShalatProvinceFragmentDirections.actionShalatProvinceFragmentToShalatCityFragment(
                    id, name
                )
            )
        }
    }

    private fun initViewModel() {
        provinceViewModel.getProvince.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        stateNoInternetView(false)
                        stateLoading(true)
                    }

                    is Resource.Success -> {
                        stateNoInternetView(false)
                        stateLoading(false)
                        provinceAdapter.setList(
                            if (provinceViewModel.searchQuery.isNotEmpty() && provinceViewModel.filteredData.isNotEmpty()) {
                                provinceViewModel.filteredData
                            } else {
                                it.data!!
                            }
                        )

                        tvTitleProvince.isVisible = provinceViewModel.searchQuery.isEmpty()
                        svProvince.isIconified = provinceViewModel.searchQuery.isEmpty()

                        svProvince.apply {
                            setOnSearchClickListener {
                                tvTitleProvince.visibility = View.GONE
                            }

                            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    hideKeyboard(requireActivity())
                                    return true
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    provinceAdapter.filter.filter(newText)
                                    return true
                                }
                            })

                            setOnCloseListener {
                                provinceAdapter.setList(it.data!!)
                                tvTitleProvince.visibility = View.VISIBLE
                                false
                            }
                        }
                    }

                    is Resource.Error -> {
                        stateNoInternetView(true)
                        stateLoading(false)
                        rvProvince.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        with(binding) {
            emptyState.root.visibility = View.GONE
            if (state) {
                progressBar.visibility = View.VISIBLE
                rvProvince.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                rvProvince.visibility = View.VISIBLE
            }
        }
    }

    private fun stateNoInternetView(state: Boolean) {
        binding.clNoInternet.visibility = if (state) View.VISIBLE else View.GONE
    }

    override fun onPause() {
        super.onPause()

        provinceViewModel.apply {
            searchQuery = binding.svProvince.query.toString()
            filteredData = provinceAdapter.getList()
        }
    }
}