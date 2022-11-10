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
import com.prodev.muslimq.presentation.BaseActivity
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
    private var alarmShubuh = true
    private var alarmDzuhur = true
    private var alarmAshar = true
    private var alarmMaghrib = true
    private var alarmIsya = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShalatBinding.inflate(inflater, container, false)
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
        checkShubuhState()
        checkDzuhurState()
        checkAsharState()
        checkMaghribState()
        checkIsyaState()
    }

    private fun checkShubuhState() {
        with(binding.shalatLayout) {
            ivNotifShubuhOn.setOnClickListener {
                alarmShubuh = false
                dataStoreViewModel.saveShubuhState(alarmShubuh)
                ivNotifShubuhOn.visibility = View.GONE
                ivNotifShubuhOff.visibility = View.VISIBLE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Shubuh Dinonaktifkan",
                )
            }

            ivNotifShubuhOff.setOnClickListener {
                alarmShubuh = true
                dataStoreViewModel.saveShubuhState(alarmShubuh)
                ivNotifShubuhOn.visibility = View.VISIBLE
                ivNotifShubuhOff.visibility = View.GONE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Shubuh Diaktifkan",
                )
            }

            dataStoreViewModel.getShubuhState.observe(viewLifecycleOwner) { state ->
                if (state) {
                    ivNotifShubuhOn.visibility = View.VISIBLE
                    ivNotifShubuhOff.visibility = View.GONE
                } else {
                    ivNotifShubuhOn.visibility = View.GONE
                    ivNotifShubuhOff.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkDzuhurState() {
        with(binding.shalatLayout) {
            ivNotifDzuhurOn.setOnClickListener {
                alarmDzuhur = false
                dataStoreViewModel.saveDzuhurState(alarmDzuhur)
                ivNotifDzuhurOn.visibility = View.GONE
                ivNotifDzuhurOff.visibility = View.VISIBLE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Dzuhur Dinonaktifkan",
                )
            }

            ivNotifDzuhurOff.setOnClickListener {
                alarmDzuhur = true
                dataStoreViewModel.saveDzuhurState(alarmDzuhur)
                ivNotifDzuhurOn.visibility = View.VISIBLE
                ivNotifDzuhurOff.visibility = View.GONE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Dzuhur Diaktifkan",
                )
            }

            dataStoreViewModel.getDzuhurState.observe(viewLifecycleOwner) { state ->
                if (state) {
                    ivNotifDzuhurOn.visibility = View.VISIBLE
                    ivNotifDzuhurOff.visibility = View.GONE
                } else {
                    ivNotifDzuhurOn.visibility = View.GONE
                    ivNotifDzuhurOff.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkAsharState() {
        with(binding.shalatLayout) {
            ivNotifAsharOn.setOnClickListener {
                alarmAshar = false
                dataStoreViewModel.saveAsharState(alarmAshar)
                ivNotifAsharOn.visibility = View.GONE
                ivNotifAsharOff.visibility = View.VISIBLE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Ashar Dinonaktifkan",
                )
            }

            ivNotifAsharOff.setOnClickListener {
                alarmAshar = true
                dataStoreViewModel.saveAsharState(alarmAshar)
                ivNotifAsharOn.visibility = View.VISIBLE
                ivNotifAsharOff.visibility = View.GONE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Ashar Diaktifkan",
                )
            }

            dataStoreViewModel.getAsharState.observe(viewLifecycleOwner) { state ->
                if (state) {
                    ivNotifAsharOn.visibility = View.VISIBLE
                    ivNotifAsharOff.visibility = View.GONE
                } else {
                    ivNotifAsharOn.visibility = View.GONE
                    ivNotifAsharOff.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkMaghribState() {
        with(binding.shalatLayout) {
            ivNotifMaghribOn.setOnClickListener {
                alarmMaghrib = false
                dataStoreViewModel.saveMaghribState(alarmMaghrib)
                ivNotifMaghribOn.visibility = View.GONE
                ivNotifMaghribOff.visibility = View.VISIBLE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Maghrib Dinonaktifkan",
                )
            }

            ivNotifMaghribOff.setOnClickListener {
                alarmMaghrib = true
                dataStoreViewModel.saveMaghribState(alarmMaghrib)
                ivNotifMaghribOn.visibility = View.VISIBLE
                ivNotifMaghribOff.visibility = View.GONE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Maghrib Diaktifkan",
                )
            }

            dataStoreViewModel.getMaghribState.observe(viewLifecycleOwner) { state ->
                if (state) {
                    ivNotifMaghribOn.visibility = View.VISIBLE
                    ivNotifMaghribOff.visibility = View.GONE
                } else {
                    ivNotifMaghribOn.visibility = View.GONE
                    ivNotifMaghribOff.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkIsyaState() {
        with(binding.shalatLayout) {
            ivNotifIsyaOn.setOnClickListener {
                alarmIsya = false
                dataStoreViewModel.saveIsyaState(alarmIsya)
                ivNotifIsyaOn.visibility = View.GONE
                ivNotifIsyaOff.visibility = View.VISIBLE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Isya Dinonaktifkan",
                )
            }

            ivNotifIsyaOff.setOnClickListener {
                alarmIsya = true
                dataStoreViewModel.saveIsyaState(alarmIsya)
                ivNotifIsyaOn.visibility = View.VISIBLE
                ivNotifIsyaOff.visibility = View.GONE
                (requireActivity() as BaseActivity).customSnackbar(
                    requireActivity(),
                    binding.root,
                    "Adzan Isya Diaktifkan",
                )
            }

            dataStoreViewModel.getIsyaState.observe(viewLifecycleOwner) { state ->
                if (state) {
                    ivNotifIsyaOn.visibility = View.VISIBLE
                    ivNotifIsyaOff.visibility = View.GONE
                } else {
                    ivNotifIsyaOn.visibility = View.GONE
                    ivNotifIsyaOff.visibility = View.VISIBLE
                }
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
    fun nexTimePray() {
        val timeNow = SimpleDateFormat("HH:mm", Locale("in", "ID")).format(Date())

        binding.apply {
            when {
                timeNow < shubuh -> {
                    tvTimeShalat.text = "Shubuh pukul $shubuh WIB"
                    shalatLayout.clShubuh.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_item_shalat
                    )
                    if (alarmShubuh) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            shubuh,
                            "Adzan Shubuh",
                            "Waktunya shalat shubuh"
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
                    if (alarmDzuhur) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            dzuhur,
                            "Adzan Dzuhur",
                            "Waktunya shalat dzuhur"
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
                    if (alarmAshar) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            ashar,
                            "Adzan Ashar",
                            "Waktunya shalat ashar"
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
                    if (alarmMaghrib) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            maghrib,
                            "Adzan Maghrib",
                            "Waktunya shalat maghrib"
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
                    if (alarmIsya) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            isya,
                            "Adzan Isya",
                            "Waktunya shalat isya"
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
                    if (alarmShubuh) {
                        alarmReceiver.setRepeatingAlarm(
                            requireActivity(),
                            shubuh,
                            "Adzan Shubuh",
                            "Waktunya shalat shubuh"
                        )
                    } else {
                        alarmReceiver.cancelAlarm(requireActivity())
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setViewModel()
    }
}