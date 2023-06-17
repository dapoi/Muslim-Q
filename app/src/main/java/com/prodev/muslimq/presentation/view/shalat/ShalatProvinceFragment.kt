package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.core.utils.swipeRefresh
import com.prodev.muslimq.databinding.FragmentShalatProvinceBinding
import com.prodev.muslimq.presentation.adapter.ProvinceAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShalatProvinceFragment :
    BaseFragment<FragmentShalatProvinceBinding>(FragmentShalatProvinceBinding::inflate) {

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val provinceAdapter: ProvinceAdapter by lazy { ProvinceAdapter(binding.emptyState.root) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            swipeRefresh(
                requireContext(),
                { setViewModel() },
                srlProvince,
                clNoInternet,
                rvProvince
            )
        }

        setAdapter()
        setViewModel()
    }

    private fun setAdapter() {
        binding.rvProvince.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = provinceAdapter
        }

        provinceAdapter.onClick = {
            val id = it.id
            val name = it.name
            viewLifecycleOwner.lifecycleScope.launch {
                dataStoreViewModel.saveProvinceData(id, name)
            }
            findNavController().navigate(R.id.action_shalatProvinceFragment_to_shalatCityFragment)
        }
    }

    private fun setViewModel() {
        shalatViewModel.getProvinceResult.observe(viewLifecycleOwner) {
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
                            if (shalatViewModel.searchQuery.isNotEmpty() &&
                                shalatViewModel.filteredData.isNotEmpty()
                            ) {
                                shalatViewModel.filteredData
                            } else {
                                it.data!!
                            }
                        )

                        tvTitleProvince.isVisible = shalatViewModel.searchQuery.isEmpty()
                        svProvince.isIconified = shalatViewModel.searchQuery.isEmpty()

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

        shalatViewModel.apply {
            searchQuery = binding.svProvince.query.toString()
            filteredData = provinceAdapter.getList()
        }
    }
}