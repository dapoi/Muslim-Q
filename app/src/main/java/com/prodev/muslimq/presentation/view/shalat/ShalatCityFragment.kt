package com.prodev.muslimq.presentation.view.shalat

import android.content.Context
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
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.databinding.FragmentShalatCityBinding
import com.prodev.muslimq.presentation.BaseActivity
import com.prodev.muslimq.presentation.adapter.CityAdapter
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.prodev.muslimq.utils.Resource
import com.prodev.muslimq.utils.hideKeyboard
import com.prodev.muslimq.utils.isOnline
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ShalatCityFragment : Fragment() {

    private var listCity = ArrayList<CityResponse>()
    private var listCityFiltered = ArrayList<CityResponse>()

    private var _binding: FragmentShalatCityBinding? = null
    private val binding get() = _binding!!
    private val shalatViewModel: ShalatViewModel by viewModels()

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

            svCity.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard(requireActivity())
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    filterCity(newText)
                    return false
                }
            })
        }
    }

    private fun filterCity(query: String) {
        listCityFiltered.clear()
        listCityFiltered = listCity.filter {
            it.lokasi.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
        } as ArrayList<CityResponse>

        binding.apply {
            clEmpty.visibility = if (listCityFiltered.isEmpty()) View.VISIBLE else View.GONE

            cityAdapter = CityAdapter(listCityFiltered)
            rvCity.adapter = cityAdapter
        }
    }

    private fun setAdapter() {
        cityAdapter = CityAdapter(listCity)
        binding.rvCity.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setViewModel() {
        lifecycleScope.launch {
            shalatViewModel.getAllCity().observe(viewLifecycleOwner) {
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
                }

                when (it) {
                    is Resource.Loading -> stateLoading(true)
                    is Resource.Success -> {
                        stateLoading(false)
                        cityAdapter.setList(it.data!!)
                    }
                    is Resource.Error -> {
                        stateLoading(false)
                        binding.apply {
                            rvCity.visibility = View.GONE
                            clNoInternet.visibility = View.VISIBLE
                        }
                        Log.e("Error", it.error.toString())
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as BaseActivity).hideBottomNavigation()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as BaseActivity).showBottomNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}