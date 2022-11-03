package com.prodev.muslimq.presentation.view.shalat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var alarmReceiver: AlarmReceiver

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private var shubuh = ""
    private var dzuhur = ""
    private var ashar = ""
    private var maghrib = ""
    private var isya = ""
    private var isAlarmOn = false

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

        alarmReceiver = AlarmReceiver()

        binding.apply {
            ivIconChoose.setOnClickListener {
                findNavController().navigate(R.id.action_shalatFragment_to_shalatProvinceFragment)
            }

            dateGregorianAndHijri()
            stateAlarmImage()
        }
    }

    private fun stateAlarmImage() {
        with(binding.shalatLayout) {
            ivNotifShubuhOn.setOnClickListener {
                ivNotifShubuhOn.visibility = View.GONE
                ivNotifShubuhOff.visibility = View.VISIBLE
                isAlarmOn = false
                Toast.makeText(context, "Alarm Shubuh Off", Toast.LENGTH_SHORT).show()
            }
            ivNotifShubuhOff.setOnClickListener {
                ivNotifShubuhOff.visibility = View.GONE
                ivNotifShubuhOn.visibility = View.VISIBLE
                isAlarmOn = true
                Toast.makeText(context, "Alarm Shubuh On", Toast.LENGTH_SHORT).show()
            }
            ivNotifDzuhurOn.setOnClickListener {
                ivNotifDzuhurOn.visibility = View.GONE
                ivNotifDzuhurOff.visibility = View.VISIBLE
                isAlarmOn = false
                Toast.makeText(context, "Alarm Dzuhur Off", Toast.LENGTH_SHORT).show()
            }
            ivNotifDzuhurOff.setOnClickListener {
                ivNotifDzuhurOff.visibility = View.GONE
                ivNotifDzuhurOn.visibility = View.VISIBLE
                isAlarmOn = true
                Toast.makeText(context, "Alarm Dzuhur On", Toast.LENGTH_SHORT).show()
            }
            ivNotifAsharOn.setOnClickListener {
                ivNotifAsharOn.visibility = View.GONE
                ivNotifAsharOff.visibility = View.VISIBLE
                isAlarmOn = false
                Toast.makeText(context, "Alarm Ashar Off", Toast.LENGTH_SHORT).show()
            }
            ivNotifAsharOff.setOnClickListener {
                ivNotifAsharOff.visibility = View.GONE
                ivNotifAsharOn.visibility = View.VISIBLE
                isAlarmOn = true
                Toast.makeText(context, "Alarm Ashar On", Toast.LENGTH_SHORT).show()
            }
            ivNotifMaghribOn.setOnClickListener {
                ivNotifMaghribOn.visibility = View.GONE
                ivNotifMaghribOff.visibility = View.VISIBLE
                isAlarmOn = false
                Toast.makeText(context, "Alarm Maghrib Off", Toast.LENGTH_SHORT).show()
            }
            ivNotifMaghribOff.setOnClickListener {
                ivNotifMaghribOff.visibility = View.GONE
                ivNotifMaghribOn.visibility = View.VISIBLE
                isAlarmOn = true
                Toast.makeText(context, "Alarm Maghrib On", Toast.LENGTH_SHORT).show()
            }
            ivNotifIsyaOn.setOnClickListener {
                ivNotifIsyaOn.visibility = View.GONE
                ivNotifIsyaOff.visibility = View.VISIBLE
                isAlarmOn = false
                Toast.makeText(context, "Alarm Isya Off", Toast.LENGTH_SHORT).show()
            }
            ivNotifIsyaOff.setOnClickListener {
                ivNotifIsyaOff.visibility = View.GONE
                ivNotifIsyaOn.visibility = View.VISIBLE
                isAlarmOn = true
                Toast.makeText(context, "Alarm Isya On", Toast.LENGTH_SHORT).show()
            }
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
                            nexTimePray()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
                tvShubuhTime.text = "$shubuh WIB"
                tvDzuhurTime.text = "$dzuhur WIB"
                tvAsharTime.text = "$ashar WIB"
                tvMaghribTime.text = "$maghrib WIB"
                tvIsyaTime.text = "$isya WIB"
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

    @SuppressLint("SetTextI18n")
    private fun nexTimePray() {
        val timeNow = SimpleDateFormat("HH:mm", Locale("in", "ID")).format(Date())

        binding.apply {
            when {
                timeNow < shubuh -> {
                    tvTimeShalat.text = "Shubuh pukul $shubuh WIB"
                    shalatLayout.clShubuh.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (isAlarmOn) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            shubuh,
                            "Adzan Shubuh",
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
                timeNow < dzuhur -> {
                    tvTimeShalat.text = "Dzuhur pukul $dzuhur WIB"
                    shalatLayout.clDzuhur.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (isAlarmOn) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            dzuhur,
                            "Adzan Dzuhur",
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
                timeNow < ashar -> {
                    tvTimeShalat.text = "Ashar pukul $ashar WIB"
                    shalatLayout.clAshar.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (isAlarmOn) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            ashar,
                            "Adzan Ashar",
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
                timeNow < maghrib -> {
                    tvTimeShalat.text = "Maghrib pukul $maghrib WIB"
                    shalatLayout.clMaghrib.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (isAlarmOn) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            maghrib,
                            "Adzan Maghrib",
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
                timeNow < isya -> {
                    tvTimeShalat.text = "Isya pukul $isya WIB"
                    shalatLayout.clIsya.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (isAlarmOn) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            isya,
                            "Adzan Isya",
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
                else -> {
                    tvTimeShalat.text = "Shubuh pukul $shubuh WIB"
                    shalatLayout.clShubuh.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (isAlarmOn) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            shubuh,
                            "Adzan Shubuh",
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
            }
        }
    }
}