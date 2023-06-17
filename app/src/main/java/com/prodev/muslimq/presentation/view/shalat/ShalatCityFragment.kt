package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.capitalizeEachWord
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.core.utils.swipeRefresh
import com.prodev.muslimq.databinding.FragmentShalatCityBinding
import com.prodev.muslimq.presentation.adapter.CityAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShalatCityFragment :
    BaseFragment<FragmentShalatCityBinding>(FragmentShalatCityBinding::inflate) {

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val cityAdapter: CityAdapter by lazy { CityAdapter(binding.emptyState.root) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }

            svCity.apply {
                setOnSearchClickListener {
                    tvTitleCity.visibility = View.GONE
                }

                setOnCloseListener {
                    tvTitleCity.visibility = View.VISIBLE
                    false
                }

                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        hideKeyboard(requireActivity())
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        cityAdapter.filter.filter(newText)
                        return true
                    }
                })
            }

            swipeRefresh(
                requireContext(),
                { setViewModel() },
                srlCity,
                clNoInternet,
                rvCity
            )
        }

        setAdapter()
        setViewModel()
    }

    private fun setAdapter() {
        binding.rvCity.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        cityAdapter.onCLick = { city ->
            viewLifecycleOwner.lifecycleScope.launch {
                dataStoreViewModel.saveAreaData(city.name, "Indonesia")
            }

            setFragmentResult(REQUEST_CITY_KEY, bundleOf(BUNDLE_CITY to true))
            findNavController().popBackStack(R.id.shalatFragment, false)
        }
    }

    private fun setViewModel() {
        dataStoreViewModel.getProvinceData.observe(viewLifecycleOwner) { dataProv ->
            shalatViewModel.getAllCity(dataProv.first)

            val provinceName = when (dataProv.second) {
                "DKI JAKARTA" -> {
                    "DKI Jakarta"
                }

                "DI YOGYAKARTA" -> {
                    "Yogyakarta"
                }

                else -> {
                    capitalizeEachWord(dataProv.second)
                }
            }
            binding.tvTitleCity.text = getString(R.string.city_choose, provinceName)
        }

        shalatViewModel.getCityResult.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        stateNoInternetView(false)
                        stateLoading(true)
                    }

                    is Resource.Success -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(500)
                            stateNoInternetView(false)
                            stateLoading(false)
                            cityAdapter.setList(it.data!!)
                        }
                    }

                    is Resource.Error -> {
                        stateNoInternetView(true)
                        stateLoading(false)
                        rvCity.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        with(binding) {
            if (state) {
                progressBar.visibility = View.VISIBLE
                rvCity.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                rvCity.visibility = View.VISIBLE
            }
        }
    }

    private fun stateNoInternetView(state: Boolean) {
        binding.clNoInternet.visibility = if (state) View.VISIBLE else View.GONE
    }

    companion object {
        const val REQUEST_CITY_KEY = "request_city_key"
        const val BUNDLE_CITY = "bundle_city"
    }
}