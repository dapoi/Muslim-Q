package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.AdzanConstants
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.databinding.FragmentShalatCityBinding
import com.prodev.muslimq.helper.capitalizeEachWord
import com.prodev.muslimq.helper.hideKeyboard
import com.prodev.muslimq.helper.swipeRefresh
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.CityAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.CityViewModel
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShalatCityFragment :
    BaseFragment<FragmentShalatCityBinding>(FragmentShalatCityBinding::inflate) {

    private val cityViewModel: CityViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val args: ShalatCityFragmentArgs by navArgs()
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

            swipeRefresh({ cityViewModel.setCity() }, srlCity)
        }

        initAdapter()
        initViewModel()
    }

    private fun initAdapter() {
        binding.rvCity.apply {
            adapter = cityAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        cityAdapter.onCLick = { city ->
            viewLifecycleOwner.lifecycleScope.launch {
                dataStoreViewModel.saveAreaData(capitalizeEachWord(city.name), "Indonesia")
            }

            listOf(
                AdzanConstants.KEY_ADZAN_SHUBUH,
                AdzanConstants.KEY_ADZAN_DZUHUR,
                AdzanConstants.KEY_ADZAN_ASHAR,
                AdzanConstants.KEY_ADZAN_MAGHRIB,
                AdzanConstants.KEY_ADZAN_ISYA
            ).forEach { adzanName ->
                dataStoreViewModel.saveSwitchState(adzanName, false)
            }

            (activity as MainActivity).customSnackbar(
                state = true,
                context = requireContext(),
                view = binding.root,
                message = "Ubah lokasi berhasil, aktifkan kembali adzan",
                toOtherFragment = true
            )

            findNavController().popBackStack(R.id.shalatFragment, false)
        }
    }

    private fun initViewModel() {
        val provinceName = when (val provinceNameArgs = args.provinceName) {
            "DKI JAKARTA" -> {
                "DKI Jakarta"
            }

            "DI YOGYAKARTA" -> {
                "Yogyakarta"
            }

            else -> {
                capitalizeEachWord(provinceNameArgs)
            }
        }
        binding.tvTitleCity.text = getString(R.string.city_choose, provinceName)

        cityViewModel.getCity.observe(viewLifecycleOwner) { response ->
            binding.apply {
                when (response) {
                    is Resource.Loading -> {
                        stateNoInternetView(false)
                        stateLoading(true)
                    }

                    is Resource.Success -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            stateNoInternetView(false)
                            stateLoading(false)
                            cityAdapter.setList(response.data!!)
                        }, 500)
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
}