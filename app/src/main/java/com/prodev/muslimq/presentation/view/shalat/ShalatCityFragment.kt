package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
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
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShalatCityFragment : Fragment() {

    private var _binding: FragmentShalatCityBinding? = null
    private val binding get() = _binding!!

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private lateinit var cityAdapter: CityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShalatCityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
        setViewModel()

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() }
            dataStoreViewModel.getProvinceData.observe(viewLifecycleOwner) { province ->
                val provinceName = when (province.second) {
                    "DKI JAKARTA" -> {
                        "DKI Jakarta"
                    }

                    "DI YOGYAKARTA" -> {
                        "Yogyakarta"
                    }

                    else -> {
                        capitalizeEachWord(province.second)
                    }
                }
                tvTitleCity.text = getString(R.string.city_choose, provinceName)
            }

            svCity.setOnSearchClickListener {
                tvTitleCity.visibility = View.GONE
            }

            svCity.setOnCloseListener {
                tvTitleCity.visibility = View.VISIBLE
                false
            }

            svCity.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard(requireActivity())
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    cityAdapter.filter.filter(newText)
                    return true
                }
            })

            swipeRefresh(
                requireContext(),
                { setViewModel() },
                srlCity,
                clNoInternet,
                rvCity
            )
        }
    }

    private fun setAdapter() {
        cityAdapter = CityAdapter(binding.emptyState.root)
        binding.rvCity.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)

            cityAdapter.onCLick = { city ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreViewModel.apply {
                        saveAreaData(city.name, "Indonesia")
                    }
                }

                setFragmentResult(REQUEST_CITY_KEY, bundleOf(BUNDLE_CITY to true))
                findNavController().popBackStack(R.id.shalatFragment, false)
            }
        }
    }

    private fun setViewModel() {
        dataStoreViewModel.getProvinceData.observe(viewLifecycleOwner) { dataProv ->
            shalatViewModel.getAllCity(dataProv.first)
        }

        shalatViewModel.getCityResult.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        stateNoInternetView(false)
                        stateLoading(true)
                    }

                    is Resource.Success -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            stateNoInternetView(false)
                            stateLoading(false)
                            cityAdapter.setList(it.data!!)
                        }, 700)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_CITY_KEY = "request_city_key"
        const val BUNDLE_CITY = "bundle_city"
    }
}