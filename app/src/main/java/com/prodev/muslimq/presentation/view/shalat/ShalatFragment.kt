package com.prodev.muslimq.presentation.view.shalat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    private var shubuh = ""
    private var dzuhur = ""
    private var ashar = ""
    private var maghrib = ""
    private var isya = ""

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
                            it.data?.let { data -> getData(data) }
                            nextTimePray()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun nextTimePray() {
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val nextTimeShalat = binding.tvTimeShalat
        binding.apply {
            when {
                timeNow < shubuh -> {
                    nextTimeShalat.text = "Shubuh pukul $shubuh"
                    shalatLayout.clShubuh.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_item_shalat
                    )
                }
                timeNow < dzuhur -> {
                    nextTimeShalat.text = "Dzuhur pukul $dzuhur"
                    shalatLayout.clDzuhur.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_item_shalat
                    )
                }
                timeNow < ashar -> {
                    nextTimeShalat.text = "Ashar pukul $ashar"
                    shalatLayout.clAshar.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_item_shalat
                    )
                }
                timeNow < maghrib -> {
                    nextTimeShalat.text = "Maghrib pukul $maghrib"
                    shalatLayout.clMaghrib.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_item_shalat
                    )
                }
                timeNow < isya -> {
                    nextTimeShalat.text = "Isya pukul $isya"
                    shalatLayout.clIsya.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_item_shalat
                    )
                }
                else -> {
                    nextTimeShalat.text = "Shubuh pukul $shubuh"
                    shalatLayout.clShubuh.background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_item_shalat
                    )
                }
            }
        }
    }

//    private fun getDifferentTime(time: String): Long {
//        return convertToDateFormat(time).time - convertToDateFormat(convertTo24HrFormat(getCurrentTimeFormat)).time
//    }
//
//    // Date Format
//    @SuppressLint("SimpleDateFormat")
//    private val m24HourSDF = SimpleDateFormat("HH:mm")
//
//    @SuppressLint("SimpleDateFormat")
//    private val m12HourSDF = SimpleDateFormat("hh:mm aa")
//
//    @SuppressLint("SimpleDateFormat")
//    val getCurrentTimeFormat: String = SimpleDateFormat("hh:mm aa").format(Date())
//
//    @SuppressLint("SimpleDateFormat")
//    fun convertTo24HrFormat(date: String): String = m24HourSDF.format(m12HourSDF.parse(date)!!)
//
//    fun convertToDateFormat(date: String) = m24HourSDF.parse(date)!!

    private fun getData(data: ShalatEntity) {
        val currentFormat = "hh:mm a"
        val targetFormat = "HH:mm"
        val timeZone = "Asia/Jakarta"
        val idFormat = Locale("in", "ID")
        val currentTimeFormat = SimpleDateFormat(currentFormat, idFormat)
        currentTimeFormat.timeZone = TimeZone.getTimeZone(timeZone)
        val targetTimeFormat = SimpleDateFormat(targetFormat, idFormat)

        try {
            val newShubuh = currentTimeFormat.parse(data.shubuh)
            val newDzuhur = currentTimeFormat.parse(data.dzuhur)
            val newAshar = currentTimeFormat.parse(data.ashar)
            val newMaghrib = currentTimeFormat.parse(data.maghrib)
            val newIsya = currentTimeFormat.parse(data.isya)
            shubuh = targetTimeFormat.format(newShubuh!!)
            dzuhur = targetTimeFormat.format(newDzuhur!!)
            ashar = targetTimeFormat.format(newAshar!!)
            maghrib = targetTimeFormat.format(newMaghrib!!)
            isya = targetTimeFormat.format(newIsya!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        with(binding) {
            progressBar.visibility = View.GONE
            shalatLayout.apply {
                root.visibility = View.VISIBLE
                tvShubuhTime.text = shubuh
                tvDzuhurTime.text = dzuhur
                tvAsharTime.text = ashar
                tvMaghribTime.text = maghrib
                tvIsyaTime.text = isya
            }
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