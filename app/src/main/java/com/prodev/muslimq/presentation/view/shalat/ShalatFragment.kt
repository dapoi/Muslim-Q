package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.apachat.primecalendar.core.hijri.HijriCalendar
import com.prodev.muslimq.R
import com.prodev.muslimq.data.source.local.model.ShalatEntity
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.prodev.muslimq.utils.Resource
import com.prodev.muslimq.utils.isOnline
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShalatFragment : Fragment() {

    private lateinit var binding: FragmentShalatBinding

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
        } else {
            binding = FragmentShalatBinding.inflate(inflater, container, false)
            setViewModel()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivIconChoose.setOnClickListener {
                findNavController().navigate(R.id.action_shalatFragment_to_shalatProvinceFragment)
            }

            dateGregorianAndHijri()
        }
    }

    private fun setViewModel() {
        dataStoreViewModel.getCityData.observe(viewLifecycleOwner) { cityData ->
            Log.d("ShalatFragment", "setViewModel: $cityData")
            if (cityData.isEmpty()) {
                binding.tvChooseLocation.text = resources.getString(R.string.choose_your_location)
            } else {
                binding.tvChooseLocation.text = cityData
            }
            shalatViewModel.getShalatDaily(cityData).observe(viewLifecycleOwner) {
                with(binding) {
                    srlShalat.apply {
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
                                    clNoInternet.visibility = View.VISIBLE
                                    shalatLayout.root.visibility = View.GONE
                                    setRefreshing(false)
                                }
                            }
                        })
                    }

                    when {
                        it is Resource.Loading && it.data == null -> {
                            progressBar.visibility = View.VISIBLE
                            clNoInternet.visibility = View.GONE
                            shalatLayout.root.visibility = View.GONE
                        }
                        it is Resource.Error && it.data == null -> {
                            progressBar.visibility = View.GONE
                            clNoInternet.visibility = View.VISIBLE
                            shalatLayout.root.visibility = View.GONE
                        }
                        else -> {
                            getData(it.data!!)
                        }
                    }
                }
            }
        }
    }

    private fun getData(data: ShalatEntity) {
        with(binding) {
            progressBar.visibility = View.GONE
            shalatLayout.root.visibility = View.VISIBLE
            shalatLayout.tvShubuhTime.text = data.shubuh
            shalatLayout.tvDzuhurTime.text = data.dzuhur
            shalatLayout.tvAsharTime.text = data.ashar
            shalatLayout.tvMaghribTime.text = data.maghrib
            shalatLayout.tvIsyaTime.text = data.isya
            clNoInternet.visibility = View.GONE
        }
    }

    private fun dateGregorianAndHijri() {
        binding.apply {
            val indonesia = Locale("in", "ID")
            val simpleDateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", indonesia)
            val date = simpleDateFormat.format(Date())
            tvGregorianDate.text = date

            val hijriCalendar = HijriCalendar()
            val hijriDate =
                "${hijriCalendar.dayOfMonth} ${hijriCalendar.monthName} ${hijriCalendar.year}H"
            tvIslamicDate.text = hijriDate
        }
    }
}