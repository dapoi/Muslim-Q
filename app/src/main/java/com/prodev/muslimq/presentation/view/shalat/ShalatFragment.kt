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
import com.apachat.primecalendar.core.hijri.HijriCalendar
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.presentation.BaseActivity
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.prodev.muslimq.service.AdzanReceiver
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShalatFragment : Fragment() {

    private lateinit var binding: FragmentShalatBinding

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val adzanReceiver = AdzanReceiver()

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
    ) {
        dataStoreViewModel.saveNotifState(it)
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

        isOnline = isOnline(requireContext())

        binding.ivIconChoose.setOnClickListener {
            findNavController().navigate(R.id.action_shalatFragment_to_shalatProvinceFragment)
        }

        if (isFirstLoad) {
            setViewModel()
        }

        swipeRefresh()
        dateGregorianAndHijri()
        checkStatusIconReminder()
    }

    private fun swipeRefresh() {
        with(binding) {
            srlShalat.apply {
                setLottieAnimation("loading.json")
                setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                    override fun onRefresh() {
                        val handlerData = Handler(Looper.getMainLooper())
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

    private fun checkStatusIconReminder() {
        binding.apply {

            ivIconAdzanState.setOnClickListener {
                if (Build.VERSION.SDK_INT >= 33) {
                    checkNotificationPermissionWhenLaunch()
                } else {
                    stateNotifIcon()
                }
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
                    stateNotifIcon()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    (activity as BaseActivity).customSnackbar(
                        true, requireContext(), binding.root, "Izinkan notifikasi adzan", true
                    )
                }
                else -> {
                    requestPermissionPostNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun stateNotifIcon() {
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

    private fun setViewModel() {
        dataStoreViewModel.getCityData.observe(viewLifecycleOwner) { cityData ->
            if (cityData.isEmpty()) {
                binding.tvChooseLocation.text = resources.getString(R.string.choose_your_location)
            } else {
                binding.tvChooseLocation.text = cityData
            }

            shalatViewModel.getShalatDaily(cityData).observe(viewLifecycleOwner) {
                with(binding) {
                    when {
                        it is Resource.Loading && it.data == null -> {
                            progressBar.visibility = View.VISIBLE
                            clNegativeCase.visibility = View.GONE
                            shalatLayout.root.visibility = View.GONE
                        }
                        it is Resource.Error && it.data == null -> {
                            progressBar.visibility = View.GONE
                            if (!isOnline) {
                                negativeCase(true)
                            } else {
                                negativeCase(false)
                            }
                            shalatLayout.root.visibility = View.GONE
                        }
                        else -> {
                            it.data?.let { data -> getAllShalatData(data) }
                            setReminderAdzanTime()
                        }
                    }
                }
            }
        }
    }

    private fun getAllShalatData(data: ShalatEntity) {
        // shalat time with zone
        shubuhWithZone = data.shubuh
        dzuhurWithZone = data.dzuhur
        asharWithZone = data.ashar
        maghribWithZone = data.maghrib
        isyaWithZone = data.isya

        // shalat time without zone
        shubuh = shubuhWithZone.substring(0, shubuhWithZone.indexOf(" "))
        dzuhur = dzuhurWithZone.substring(0, dzuhurWithZone.indexOf(" "))
        ashar = asharWithZone.substring(0, asharWithZone.indexOf(" "))
        maghrib = maghribWithZone.substring(0, maghribWithZone.indexOf(" "))
        isya = isyaWithZone.substring(0, isyaWithZone.indexOf(" "))

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


    @SuppressLint("SetTextI18n")
    private fun setReminderAdzanTime() {
        timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val listAdzanTime = mapOf(
            "Adzan Shubuh" to shubuh,
            "Adzan Dzuhur" to dzuhur,
            "Adzan Ashar" to ashar,
            "Adzan Maghrib" to maghrib,
            "Adzan Isya" to isya
        )

        for ((adzanName, adzanTime) in listAdzanTime) {
            if (notifAdzanState) {
                adzanReceiver.setAdzanReminder(
                    requireActivity(),
                    adzanTime,
                    adzanName
                )
            } else {
                adzanReceiver.cancelAdzanReminder(requireActivity(), adzanName)
            }
        }

        binding.apply {
            when {
                timeNow < dzuhur && timeNow > shubuh -> {
                    tvTimeShalat.text = "${countDownShalat(dzuhur)} menuju dzuhur"
                    setNextPrayShalatBackground(shalatLayout.clDzuhur)
                }
                timeNow < ashar && timeNow > dzuhur -> {
                    tvTimeShalat.text = "${countDownShalat(ashar)} menuju ashar"
                    setNextPrayShalatBackground(shalatLayout.clAshar)
                }
                timeNow < maghrib && timeNow > ashar -> {
                    tvTimeShalat.text = "${countDownShalat(maghrib)} menuju maghrib"
                    setNextPrayShalatBackground(shalatLayout.clMaghrib)
                }
                timeNow < isya && timeNow > maghrib -> {
                    tvTimeShalat.text = "${countDownShalat(isya)} menuju isya"
                    setNextPrayShalatBackground(shalatLayout.clIsya)
                }
                else -> {
                    tvTimeShalat.text = "${countDownShalat(shubuh)} menuju shubuh"
                    setNextPrayShalatBackground(shalatLayout.clShubuh)
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

    private fun setNextPrayShalatBackground(clShalat: ConstraintLayout) {
        clShalat.background = ContextCompat.getDrawable(
            requireActivity(), R.drawable.bg_item_shalat
        )
    }

    private fun negativeCase(noInternet: Boolean) {
        with(binding) {
            clNegativeCase.visibility = View.VISIBLE
            shalatLayout.root.visibility = View.GONE
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
}