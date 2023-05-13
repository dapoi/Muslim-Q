package com.prodev.muslimq.presentation.view.shalat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Px
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.apachat.primecalendar.core.hijri.HijriCalendar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.DialogGetLocationBinding
import com.prodev.muslimq.databinding.DialogLoadingBinding
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.prodev.muslimq.service.location.GPSStatusListener
import com.prodev.muslimq.service.location.TurnOnGps
import com.prodev.muslimq.service.notification.AdzanReceiver
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShalatFragment : Fragment() {

    private lateinit var binding: FragmentShalatBinding
    private lateinit var listOfSwitch: List<SwitchCompat>
    private lateinit var gpsStatusListener: GPSStatusListener
    private lateinit var turnOnGps: TurnOnGps
    private lateinit var fusedLocation: FusedLocationProviderClient

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val adzanReceiver = AdzanReceiver()

    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }
    private val transparentDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.TransparentDialog).create()
    }

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

    private var isOnline = false
    private var isSuccessGetData = false
    private var isCityChange = false
    private var isFirstLoad = false
    private var stateAdzanName = ""
    private var lat = 0.0
    private var lon = 0.0

    private val requestPermissionPostNotification = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        dataStoreViewModel.saveSwitchState(stateAdzanName, isGranted)
        if (isGranted) {
            (activity as MainActivity).customSnackbar(
                state = true,
                context = requireContext(),
                view = binding.root,
                message = "${adzanLowerCase(stateAdzanName)} diaktifkan",
            )
        }
    }

    private val requestGPSPermission = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            (activity as MainActivity).customSnackbar(
                state = true,
                context = requireContext(),
                view = binding.root,
                message = "GPS diaktifkan"
            )
        } else {
            (activity as MainActivity).customSnackbar(
                state = false,
                context = requireContext(),
                view = binding.root,
                message = "GPS tidak diaktifkan",
            )
        }
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Izin lokasi diberikan",
                )
                showDialog()
            }

            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Izin lokasi diberikan",
                )
                showDialog()
            }

            else -> {
                (activity as MainActivity).customSnackbar(
                    state = false,
                    context = requireContext(),
                    view = binding.root,
                    message = "Izin lokasi ditolak",
                )
            }
        }
    }

    private fun checkLocationPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
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
        gpsStatusListener = GPSStatusListener(requireContext())
        turnOnGps = TurnOnGps(requireContext())
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.ivIconChoose.setOnClickListener {
            checkStateLocationPermission()
        }

        if (isFirstLoad) {
            setViewModel()
        }

        swipeRefresh()
        dateGregorianAndHijri()

        val dialogLayout = DialogLoadingBinding.inflate(layoutInflater)
        transparentDialog.setView(dialogLayout.root)
    }

    private fun showDialog() {
        val dialogLayout = DialogGetLocationBinding.inflate(layoutInflater)
        val tvTurnOnGPS = dialogLayout.tvTurnOnGps
        val tvChooseManual = dialogLayout.tvChooseManual
        val tvCancel = dialogLayout.tvCancel
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            show()
            tvTurnOnGPS.setOnClickListener {
                dismiss()
                var isGPSStatusChanged: Boolean? = null
                gpsStatusListener.observe(viewLifecycleOwner) { isGPSOn ->
                    if (isGPSStatusChanged == null) {
                        if (!isGPSOn) {
                            turnOnGps.startGps(requestGPSPermission)
                        } else {
                            transparentDialog.show()
                            getLiveLocation()
                        }
                        isGPSStatusChanged = isGPSOn
                    } else {
                        if (isGPSStatusChanged != isGPSOn) {
                            if (!isGPSOn) {
                                turnOnGps.startGps(requestGPSPermission)
                            } else {
                                transparentDialog.show()
                                getLiveLocation()
                            }
                            isGPSStatusChanged = isGPSOn
                        }
                    }
                }
            }
            tvChooseManual.setOnClickListener {
                dismiss()
                findNavController().navigate(R.id.action_shalatFragment_to_shalatProvinceFragment)
            }
            tvCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun checkStateLocationPermission() {
        when {
            checkLocationPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && checkLocationPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                showDialog()
            }

            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                (activity as MainActivity).customSnackbar(
                    state = false,
                    context = requireContext(),
                    view = binding.root,
                    message = "Izinkan lokasi untuk mengakses fitur ini",
                    action = true,
                    toSettings = true
                )
            }

            else -> {
                requestLocationPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getLiveLocation() {
        if (checkLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocation.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location = task.result
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude

                    getAddressGeocoder(lat, lon)
                } else {
                    requestNewLiveLocation()
                }
            }
        }
    }

    private fun requestNewLiveLocation() {
        val locRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000,
        ).apply {
            setWaitForAccurateLocation(false)
            setMinUpdateIntervalMillis(100)
            setMaxUpdateDelayMillis(5000)
        }.build()

        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (checkLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocation.requestLocationUpdates(
                locRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation!!
            lat = location.latitude
            lon = location.longitude

            getAddressGeocoder(lat, lon)
        }
    }

    private fun getAddressGeocoder(lat: Double, lon: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (addresses.isNotEmpty()) {
                        isCityChange = true
                        val city = addresses[0].locality
                        val country = addresses[0].countryName
                        transparentDialog.dismiss()
                        dataStoreViewModel.saveAreaData(city, country)
                    }
                }

                override fun onError(error: String?) {
                    super.onError(error)
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            try {
                val addresses = geocoder.getFromLocation(
                    lat, lon, 1
                ) as List<Address>
                if (addresses.isNotEmpty()) {
                    isCityChange = true
                    val city = addresses[0].locality
                    val country = addresses[0].countryName
                    transparentDialog.dismiss()
                    dataStoreViewModel.saveAreaData(city, country)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun refreshDataWhenCityChange() {
        setFragmentResultListener(ShalatCityFragment.REQUEST_CITY_KEY) { _, bundle ->
            if (bundle.getBoolean(ShalatCityFragment.BUNDLE_CITY, false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    isCityChange = true
                    setViewModel()
                }, 800)
            }
        }
    }

    private fun swipeRefresh() {
        with(binding) {
            srlShalat.apply {
                setLottieAnimation("loading.json")
                setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                    override fun onRefresh() {
                        isOnline = isOnline(requireContext())
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

    private fun setViewModel() {
        dataStoreViewModel.apply {
            getAreaData.observe(viewLifecycleOwner) { area ->
                with(binding) {
                    if (area.first.isEmpty() && area.second.isEmpty()) {
                        clInfoLocation.visibility = View.GONE
                        tvChooseLocation.text =
                            resources.getString(R.string.choose_your_location)
                    } else {
                        clInfoLocation.visibility = View.VISIBLE
                        tvYourLocation.text = area.first.uppercase()
                        tvChooseLocation.visibility = View.GONE
                    }
                }

                if (!isSuccessGetData || isCityChange) {
                    getShalatDaily(area.first, area.second)
                }
            }
        }
    }

    private fun getShalatDaily(city: String, country: String) {
        shalatViewModel.getShalatDaily(city, country).observe(viewLifecycleOwner) { result ->
            with(binding) {
                when {
                    result is Resource.Loading && result.data == null -> {
                        progressBar.visibility = View.VISIBLE
                        clNegativeCase.visibility = View.GONE
                        shalatLayout.root.visibility = View.GONE
                    }

                    result is Resource.Error && result.data == null -> {
                        progressBar.visibility = View.GONE
                        isOnline = isOnline(requireContext())
                        if (isOnline) {
                            negativeCase(false)
                        } else {
                            negativeCase(true)
                        }
                        shalatLayout.root.visibility = View.GONE
                    }

                    else -> {
                        clNegativeCase.visibility = View.GONE
                        result.data?.let { data -> getAllShalatData(data) }
                        setReminderAdzanTime()
                        isSuccessGetData = true
                        isCityChange = false
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

        val adzanNames = listAdzanTime.keys.toList()

        with(binding.shalatLayout) {
            listOfSwitch = listOf(
                swShubuh, swDzuhur, swAshar, swMaghrib, swIsya
            )

            listOfSwitch.withIndex().forEach { (index, switch) ->
                val adzanName = adzanNames[index]

                dataStoreViewModel.getSwitchState(adzanName)
                    .observe(viewLifecycleOwner) { state ->
                        if (ContextCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            switch.isChecked = false
                        } else {
                            switch.isChecked = state
                        }
                    }

                switch.setOnCheckedChangeListener { adzanSwitch, isChecked ->
                    if (Build.VERSION.SDK_INT >= 33) {
                        checkNotificationPermissionWhenLaunch(adzanName, isChecked, adzanSwitch)
                    } else {
                        stateNotifIcon(adzanName, isChecked)
                    }
                }

                listAdzanTime[adzanName]?.let { adzanTime ->
                    if (switch.isChecked) {
                        adzanReceiver.setAdzanReminder(
                            context = requireContext(),
                            adzanName = adzanName,
                            adzanTime = adzanTime,
                            adzanCode = index + 1,
                            isShubuh = adzanName == "Adzan Shubuh"
                        )
                    } else {
                        adzanReceiver.cancelAdzanReminder(
                            context = requireContext(),
                            adzanCode = index + 1
                        )
                    }
                }
            }
        }

        binding.apply {
            when {
                timeNow <= dzuhur && timeNow > shubuh -> {
                    tvTimeShalat.text = "${countDownShalat(dzuhur)} menuju dzuhur"
                    setNextPrayShalatBackground(shalatLayout.clDzuhur)
                }

                timeNow <= ashar && timeNow > dzuhur -> {
                    tvTimeShalat.text = "${countDownShalat(ashar)} menuju ashar"
                    setNextPrayShalatBackground(shalatLayout.clAshar)
                }

                timeNow <= maghrib && timeNow > ashar -> {
                    tvTimeShalat.text = "${countDownShalat(maghrib)} menuju maghrib"
                    setNextPrayShalatBackground(shalatLayout.clMaghrib)
                }

                timeNow <= isya && timeNow > maghrib -> {
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

    private fun checkNotificationPermissionWhenLaunch(
        adzanName: String,
        isChecked: Boolean,
        adzanSwitch: CompoundButton,
    ) {
        stateAdzanName = adzanName
        if (Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    stateNotifIcon(adzanName, isChecked)
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    adzanSwitch.isChecked = false
                    (activity as MainActivity).customSnackbar(
                        state = false,
                        context = requireContext(),
                        view = binding.root,
                        message = "Izinkan notifikasi untuk mengaktifkan adzan",
                        action = true
                    )
                }

                else -> {
                    adzanSwitch.isChecked = false
                    requestPermissionPostNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun stateNotifIcon(adzanName: String, isChecked: Boolean) {
        val lowerCaseAdzanName = adzanLowerCase(adzanName)
        if (isChecked) {
            dataStoreViewModel.saveSwitchState(adzanName, true)
            (activity as MainActivity).customSnackbar(
                true,
                requireContext(),
                binding.root,
                "$lowerCaseAdzanName diaktifkan",
            )
        } else {
            dataStoreViewModel.saveSwitchState(adzanName, false)
            (activity as MainActivity).customSnackbar(
                false,
                requireContext(),
                binding.root,
                "$lowerCaseAdzanName dimatikan",
            )
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

    private fun adzanLowerCase(adzanName: String): String {
        return adzanName.substring(0, 1).uppercase() + adzanName.substring(1).lowercase()
    }

    override fun onResume() {
        super.onResume()

        if (isCityChange) setViewModel()
        refreshDataWhenCityChange()
    }
}