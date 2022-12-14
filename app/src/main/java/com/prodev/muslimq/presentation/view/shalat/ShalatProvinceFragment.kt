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
import com.prodev.muslimq.databinding.FragmentShalatProvinceBinding
import com.prodev.muslimq.presentation.adapter.ProvinceAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.prodev.muslimq.utils.Resource
import com.prodev.muslimq.utils.hideKeyboard
import com.prodev.muslimq.utils.isOnline
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShalatProvinceFragment : Fragment() {

    private lateinit var binding: FragmentShalatProvinceBinding

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private lateinit var provinceAdapter: ProvinceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
        } else {
            binding = FragmentShalatProvinceBinding.inflate(inflater, container, false)
            setAdapter()
            setViewModel()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

            svProvince.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard(requireActivity())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    provinceAdapter.filter.filter(newText)
                    return true
                }
            })
        }
    }

    private fun setAdapter() {
        provinceAdapter = ProvinceAdapter()
        binding.rvProvince.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = provinceAdapter
            setHasFixedSize(true)
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
        shalatViewModel.getAllProvince().observe(viewLifecycleOwner) {
            with(binding) {
                srlProvince.apply {
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
                                rvProvince.visibility = View.GONE
                                clNoInternet.visibility = View.VISIBLE
                                setRefreshing(false)
                            }
                        }
                    })
                }

                when (it) {
                    is Resource.Loading -> stateLoading(true)
                    is Resource.Success -> {
                        stateLoading(false)
                        provinceAdapter.setList(it.data!!)
                        clNoInternet.visibility = View.GONE
                    }
                    is Resource.Error -> {
                        stateLoading(false)
                        rvProvince.visibility = View.GONE
                        clNoInternet.visibility = View.VISIBLE
                        Log.e("Error Province", it.error.toString())
                    }
                }
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        with(binding) {
            if (state) {
                progressBar.visibility = View.VISIBLE
                rvProvince.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                rvProvince.visibility = View.VISIBLE
            }
        }
    }
}