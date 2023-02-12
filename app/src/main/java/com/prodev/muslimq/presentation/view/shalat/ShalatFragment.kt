package com.prodev.muslimq.presentation.view.shalat

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.apachat.primecalendar.core.hijri.HijriCalendar
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.presentation.BaseActivity
import com.prodev.muslimq.presentation.receiver.AlarmReceiver
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
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

    private var timeNow = ""

    // prayer time with zone
    private var shubuhWithZone = ""
    private var dzuhurWithZone = ""
    private var asharWithZone = ""
    private var maghribWithZone = ""
    private var isyaWithZone = ""

    // prayer time without zone
    private var shubuh = ""
    private var dzuhur = ""
    private var ashar = ""
    private var maghrib = ""
    private var isya = ""

    private var notifAdzanState = false
    private var isOnline = false
    private var isFirstLoad = false

    private val requestPermissionPostNotification = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) dataStoreViewModel.saveNotifState(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
            isFirstLoad = false
        } else {
            binding = FragmentShalatBinding.inflate(inflater, container, false)
            isFirstLoad = true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivIconChoose.setOnClickListener {
            findNavController().navigate(R.id.action_shalatFragment_to_shalatProvinceFragment)
        }

        if (isFirstLoad) {
            alarmReceiver = AlarmReceiver()
            setViewModel()
        }

        dateGregorianAndHijri()
        stateNotifButton()
    }

    private fun stateNotifButton() {
        binding.apply {

            ivIconAdzanState.setOnClickListener {
                checkNotificationPermissionWhenLaunch()
            }

            dataStoreViewModel.getNotifState.observe(viewLifecycleOwner) { state ->
                notifAdzanState = state

                if (state) {
                    ivIconAdzanState.setImageResource(R.drawable.ic_notif_on)
                } else {
                    ivIconAdzanState.setImageResource(R.drawable.ic_notif_off)
                }
            }
        }
    }

    private fun checkNotificationPermissionWhenLaunch() {
        if (Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    with(binding) {
                        if (notifAdzanState) {
                            dataStoreViewModel.saveNotifState(false)
                            ivIconAdzanState.setImageResource(R.drawable.ic_notif_off)
                            (activity as BaseActivity).customSnackbar(
                                false,
                                requireContext(),
                                binding.root,
                                "Notifikasi adzan dinonaktifkan",
                            )
                        } else {
                            dataStoreViewModel.saveNotifState(true)
                            ivIconAdzanState.setImageResource(R.drawable.ic_notif_on)
                            (activity as BaseActivity).customSnackbar(
                                true,
                                requireContext(),
                                binding.root,
                                "Notifikasi adzan diaktifkan",
                            )
                        }
                    }
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    (activity as BaseActivity).customSnackbar(
                        true,
                        requireContext(),
                        binding.root,
                        "Izinkan notifikasi adzan",
                        true
                    )
                }
                else -> {
                    requestPermissionPostNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setViewModel() {
        dataStoreViewModel.getCityData.observe(viewLifecycleOwner) { cityData ->
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
                                isOnline = isOnline(requireContext())
                                if (isOnline) {
                                    handlerData.postDelayed({
                                        setRefreshing(false)
                                    }, 2000)

                                    handlerData.postDelayed({
                                        clNegativeCase.visibility = View.GONE
                                        setViewModel()
                                    }, 2350)
                                } else {
                                    negativeCase(true)
                                    setRefreshing(false)
                                }
                            }
                        })
                    }

                    when {
                        it is Resource.Loading && it.data == null -> {
                            progressBar.visibility = View.VISIBLE
                            clNegativeCase.visibility = View.GONE
                            shalatLayout.root.visibility = View.GONE
                        }
                        it is Resource.Error && it.data == null -> {
                            progressBar.visibility = View.GONE
                            if (isOnline) {
                                negativeCase(true)
                            } else {
                                negativeCase(false)
                            }
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
        shubuhWithZone = data.shubuh
        dzuhurWithZone = data.dzuhur
        asharWithZone = data.ashar
        maghribWithZone = data.maghrib
        isyaWithZone = data.isya

        try {
            shubuh = shubuhWithZone.substring(0, shubuhWithZone.indexOf(" "))
            dzuhur = dzuhurWithZone.substring(0, dzuhurWithZone.indexOf(" "))
            ashar = asharWithZone.substring(0, asharWithZone.indexOf(" "))
            maghrib = maghribWithZone.substring(0, maghribWithZone.indexOf(" "))
            isya = isyaWithZone.substring(0, isyaWithZone.indexOf(" "))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        with(binding) {
            progressBar.visibility = View.GONE
            shalatLayout.apply {
                root.visibility = View.VISIBLE
                tvShubuhTime.text = shubuhWithZone
                tvDzuhurTime.text = dzuhurWithZone
                tvAsharTime.text = asharWithZone
                tvMaghribTime.text = maghribWithZone
                tvIsyaTime.text = isyaWithZone
            }
            clNegativeCase.visibility = View.GONE
        }
    }

    private fun negativeCase(noInternet: Boolean) {
        with(binding) {
            clNegativeCase.visibility = View.VISIBLE
            if (noInternet) {
                lottieNoLocation.visibility = View.GONE
                lottieNoInternet.visibility = View.VISIBLE
                tvResult.text = getString(R.string.no_internet)
                clNegativeCase.applyConstraint {
                    topToBottom(tvResult, lottieNoInternet, 16)
                }
            } else {
                lottieNoLocation.visibility = View.VISIBLE
                lottieNoInternet.visibility = View.GONE
                tvResult.text = getString(R.string.no_prayer_time)
                clNegativeCase.applyConstraint {
                    topToBottom(tvResult, lottieNoLocation, 16)
                }
            }
        }
    }

    private fun ConstraintLayout.applyConstraint(block: ConstraintSet.() -> Unit) {
        ConstraintSet().apply {
            clone(this@applyConstraint)
            block(this)
        }.applyTo(this)
    }

    private fun ConstraintSet.topToBottom(v1: View, v2: View, @Px margin: Int = 0) {
        connect(v1.id, TOP, v2.id, BOTTOM, margin)
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
        timeNow = SimpleDateFormat("HH:mm", Locale("in", "ID")).format(Date())

        val listAdzanTime = mapOf(
            "Adzan Shubuh" to shubuh,
            "Adzan Dzuhur" to dzuhur,
            "Adzan Ashar" to ashar,
            "Adzan Maghrib" to maghrib,
            "Adzan Isya" to isya
        )

        if (notifAdzanState) {
            for (adzanTime in listAdzanTime) {
                if (timeNow == adzanTime.value) {
                    if (timeNow == shubuh) {
                        alarmReceiver.scheduleAdzan(
                            requireActivity(),
                            adzanTime.value,
                            adzanTime.key,
                            true
                        )
                    } else {
                        alarmReceiver.scheduleAdzan(
                            requireActivity(),
                            adzanTime.value,
                            adzanTime.key,
                            false
                        )
                    }
                }
            }
        } else {
            alarmReceiver.removeAdzan(requireActivity())
        }

        binding.apply {
            when {
                timeNow < dzuhur && timeNow > shubuh -> {
                    tvTimeShalat.text = "${countDownShalat(dzuhur)} menuju dzuhur"
                    shalatLayout.clDzuhur.background = ContextCompat.getDrawable(
                        requireActivity(), R.drawable.bg_item_shalat
                    )

                }
                timeNow < ashar && timeNow > dzuhur -> {
                    tvTimeShalat.text = "${countDownShalat(ashar)} menuju ashar"
                    shalatLayout.clAshar.background = ContextCompat.getDrawable(
                        requireActivity(), R.drawable.bg_item_shalat
                    )
                }
                timeNow < maghrib && timeNow > ashar -> {
                    tvTimeShalat.text = "${countDownShalat(maghrib)} menuju maghrib"
                    shalatLayout.clMaghrib.background = ContextCompat.getDrawable(
                        requireActivity(), R.drawable.bg_item_shalat
                    )
                }
                timeNow < isya && timeNow > maghrib -> {
                    tvTimeShalat.text = "${countDownShalat(isya)} menuju isya"
                    shalatLayout.clIsya.background = ContextCompat.getDrawable(
                        requireActivity(), R.drawable.bg_item_shalat
                    )
                }
                else -> {
                    tvTimeShalat.text = "${countDownShalat(shubuh)} menuju shubuh"
                    shalatLayout.clShubuh.background = ContextCompat.getDrawable(
                        requireActivity(), R.drawable.bg_item_shalat
                    )
                }
            }
        }
    }

    private fun countDownShalat(timeShalat: String): String {
        val timeNowComponent = timeNow.split(":").map { it.toInt() }
        val timeShalatComponent = timeShalat.split(":").map { it.toInt() }
        var diff =
            (timeShalatComponent[0] + 24 - timeNowComponent[0]) % 24 * 60 + timeShalatComponent[1] - timeNowComponent[1]
        if (diff < 0) diff += 1440

        val hours = diff / 60
        diff %= 60

        val minutes = diff

        return "$hours jam $minutes menit"
    }
}