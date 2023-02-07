package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.capitalizeEachWord
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.FragmentShalatCityBinding
import com.prodev.muslimq.presentation.adapter.CityAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.simform.refresh.SSPullToRefreshLayout
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
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
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
        }
    }

    private fun setAdapter() {
        cityAdapter = CityAdapter()
        binding.rvCity.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)

            cityAdapter.onCLick = { city ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreViewModel.saveCityData(city.name)
                }
                findNavController().navigate(R.id.action_shalatCityFragment_to_shalatFragment)
            }
        }
    }

    private fun setViewModel() {
        dataStoreViewModel.getProvinceData.observe(viewLifecycleOwner) { dataProv ->
            shalatViewModel.getAllCity(dataProv.first).observe(viewLifecycleOwner) {
                with(binding) {
                    srlCity.apply {
                        setLottieAnimation("loading.json")
                        setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                        setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                        setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                            override fun onRefresh() {
                                val handlerData = Handler(Looper.getMainLooper())
                                val check = isOnline(requireContext())
                                if (check) {
                                    handlerData.postDelayed({
                                        setRefreshing(false)
                                    }, 2000)

                                    handlerData.postDelayed({
                                        clNoInternet.visibility = View.GONE
                                        setViewModel()
                                    }, 2350)
                                } else {
                                    rvCity.visibility = View.GONE
                                    clNoInternet.visibility = View.VISIBLE
                                    setRefreshing(false)
                                }
                            }
                        })
                    }

                    when (it) {
                        is Resource.Loading -> stateLoading(true)
                        is Resource.Success -> {
                            Handler(Looper.getMainLooper()).postDelayed({
                                stateLoading(false)
                                cityAdapter.setList(it.data!!)
                                clNoInternet.visibility = View.GONE
                            }, 1000)
                        }
                        is Resource.Error -> {
                            stateLoading(false)
                            rvCity.visibility = View.GONE
                            clNoInternet.visibility = View.VISIBLE
                            Log.e("Error", it.error.toString())
                        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}