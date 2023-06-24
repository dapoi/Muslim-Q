package com.prodev.muslimq.presentation.view.shalat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Px
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.ContextCompat
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.DialogGetLocationBinding
import com.prodev.muslimq.databinding.DialogLoadingBinding
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import com.prodev.muslimq.service.location.GPSStatusListener
import com.prodev.muslimq.service.location.TurnOnGps
import com.prodev.muslimq.service.notification.AdzanReceiver
import com.prodev.muslimq.service.notification.AdzanService
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShalatFragment : BaseFragment<FragmentShalatBinding>(FragmentShalatBinding::inflate) {

    private lateinit var listOfSwitch: List<SwitchCompat>
    private lateinit var gpsStatusListener: GPSStatusListener
    private lateinit var turnOnGps: TurnOnGps
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var adzanReceiver: AdzanReceiver

    private val shalatViewModel: ShalatViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

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
    private var shubuh: String = ""
    private var dzuhur: String = ""
    private var ashar: String = ""
    private var maghrib: String = ""
    private var isya: String = ""

    private var isOnline = false
    private var resetSwitch = false
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
                message = "${adzanLowerCase(stateAdzanName)} diaktifkan"
            )
        } else {
            (activity as MainActivity).customSnackbar(
                state = false,
                context = requireContext(),
                view = binding.root,
                message = "Perizinan notifikasi ditolak"
            )
        }
    }

    private val requestGPSPermission = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val message =
            if (result.resultCode == RESULT_OK) "GPS diaktifkan" else "GPS tidak diaktifkan"
        (activity as MainActivity).customSnackbar(
            state = result.resultCode == RESULT_OK,
            context = requireContext(),
            view = binding.root,
            message = message
        )
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (permissionGranted) {
            getGPSStatusObserver()
        }

        val message = if (permissionGranted) "Izin lokasi diberikan" else "Izin lokasi ditolak"
        (activity as MainActivity).customSnackbar(
            state = permissionGranted,
            context = requireContext(),
            view = binding.root,
            message = message
        )
    }

    private fun checkLocationPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshDataWhenCityChange()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isOnline = isOnline(requireContext())
        adzanReceiver = AdzanReceiver()
        gpsStatusListener = GPSStatusListener(requireContext())
        turnOnGps = TurnOnGps(requireContext())
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.ivIconChoose.setOnClickListener { showDialogLocation() }

        setViewModel()
        swipeRefresh()
        dateGregorianAndHijri()

        val dialogLayout = DialogLoadingBinding.inflate(layoutInflater)
        transparentDialog.setView(dialogLayout.root)

        arguments?.getBoolean(AdzanReceiver.FROM_NOTIFICATION, false)?.let { isFromNotif ->
            if (isFromNotif) {
                requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
                    visibility = View.INVISIBLE
                }
                Intent(requireContext(), AdzanService::class.java).also {
                    requireContext().stopService(it)
                }
            }
        }
    }

    private fun showDialogLocation() {
        val dialogLayout = DialogGetLocationBinding.inflate(layoutInflater)
        val tvTurnOnGPS = dialogLayout.tvTurnOnGps
        val tvChooseManual = dialogLayout.tvChooseManual
        val tvCancel = dialogLayout.tvCancel
        with(curvedDialog.create()) {
            setView(dialogLayout.root)
            show()
            tvTurnOnGPS.setOnClickListener {
                dismiss()
                checkStateLocationPermission()
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
                getGPSStatusObserver()
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

    private fun getGPSStatusObserver() {
        var isGPSStatusChanged: Boolean? = null
        gpsStatusListener.observe(viewLifecycleOwner) { isGPSOn ->
            if (isGPSStatusChanged == null) {
                if (!isGPSOn) {
                    turnOnGps.startGps(requestGPSPermission)
                } else {
                    resetSwitch = true
                    transparentDialog.show()
                    getLiveLocation()
                }
                isGPSStatusChanged = isGPSOn
            } else {
                if (isGPSStatusChanged != isGPSOn) {
                    if (!isGPSOn) {
                        turnOnGps.startGps(requestGPSPermission)
                    } else {
                        resetSwitch = true
                        transparentDialog.show()
                        getLiveLocation()
                    }
                    isGPSStatusChanged = isGPSOn
                }
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
                        val city = addresses[0].locality
                        val country = addresses[0].countryName
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
                    val city = addresses[0].locality
                    val country = addresses[0].countryName
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
                    resetSwitch = true
                }, 800)
            }
        }
    }

    private fun swipeRefresh() {
        binding.apply {
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
                                shalatViewModel.refreshShalatTime()
                            }, 2350)
                        } else {
                            negativeCase()
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
        dataStoreViewModel.getAreaData.observe(viewLifecycleOwner) { area ->
            shalatViewModel.getShalatTime(area)

            binding.tvYourLocation.text = area.first
        }

        initUIResult()
    }

    private fun initUIResult() {
        shalatViewModel.getTimeShalat.observe(viewLifecycleOwner) { result ->
            binding.apply {
                when {
                    result is Resource.Loading && result.data == null -> {
                        transparentDialog.dismiss()
                        progressBar.visibility = View.VISIBLE
                        clNegativeCase.visibility = View.GONE
                        shalatLayout.root.visibility = View.GONE
                    }

                    result is Resource.Error && result.data == null -> {
                        progressBar.visibility = View.GONE
                        shalatLayout.root.visibility = View.GONE
                        negativeCase()
                    }

                    else -> {
                        clNegativeCase.visibility = View.GONE
                        result?.data?.let { data -> getAllShalatData(data) }
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

        try {  // shalat time without zone
            shubuh = shubuhWithZone.substring(0, shubuhWithZone.indexOf(" "))
            dzuhur = dzuhurWithZone.substring(0, dzuhurWithZone.indexOf(" "))
            ashar = asharWithZone.substring(0, asharWithZone.indexOf(" "))
            maghrib = maghribWithZone.substring(0, maghribWithZone.indexOf(" "))
            isya = isyaWithZone.substring(0, isyaWithZone.indexOf(" "))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.apply {
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

        setReminderAdzanTime(shubuh, dzuhur, ashar, maghrib, isya)
    }

    @SuppressLint("SetTextI18n")
    private fun setReminderAdzanTime(
        shubuh: String,
        dzuhur: String,
        ashar: String,
        maghrib: String,
        isya: String
    ) {

        val listAdzanTime = mapOf(
            "Adzan Shubuh" to shubuh,
            "Adzan Dzuhur" to dzuhur,
            "Adzan Ashar" to ashar,
            "Adzan Maghrib" to maghrib,
            "Adzan Isya" to isya
        )

        val adzanNames = listAdzanTime.keys.toList()

        binding.shalatLayout.apply {
            listOfSwitch = listOf(
                swShubuh, swDzuhur, swAshar, swMaghrib, swIsya
            )

            listOfSwitch.withIndex().forEach { (index, switch) ->
                val adzanName = adzanNames[index]

                if (!resetSwitch) {
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
                }

                switch.setOnCheckedChangeListener { adzanSwitch, isChecked ->
                    if (Build.VERSION.SDK_INT >= 33) {
                        checkNotificationPermissionWhenLaunch(
                            listAdzanTime,
                            adzanName,
                            isChecked,
                            adzanSwitch,
                            index,
                            resetSwitch
                        )
                    } else {
                        stateNotifIcon(listAdzanTime, adzanName, isChecked, index, resetSwitch)
                    }
                }
            }

            if (resetSwitch) {
                for (i in listOfSwitch.indices) {
                    listOfSwitch[i].isChecked = false
                }

                resetSwitch = false
            }
        }

        timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        binding.apply {
            if (!tvYourLocation.text.contains("DKI")) {
                when {
                    timeNow > shubuh && timeNow <= dzuhur -> {
                        tvTimeShalat.text = "${countDownShalat(dzuhur)} menuju dzuhur"
                        setNextPrayShalatBackground(shalatLayout.clDzuhur)
                    }

                    timeNow > dzuhur && timeNow <= ashar -> {
                        tvTimeShalat.text = "${countDownShalat(ashar)} menuju ashar"
                        setNextPrayShalatBackground(shalatLayout.clAshar)
                    }

                    timeNow > ashar && timeNow <= maghrib -> {
                        tvTimeShalat.text = "${countDownShalat(maghrib)} menuju maghrib"
                        setNextPrayShalatBackground(shalatLayout.clMaghrib)
                    }

                    timeNow > maghrib && timeNow <= isya -> {
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
    }

    private fun checkNotificationPermissionWhenLaunch(
        listAdzanTime: Map<String, String>,
        adzanName: String,
        isChecked: Boolean,
        adzanSwitch: CompoundButton,
        index: Int,
        resetSwitch: Boolean
    ) {
        stateAdzanName = adzanName
        if (Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    stateNotifIcon(listAdzanTime, adzanName, isChecked, index, resetSwitch)
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

    private fun stateNotifIcon(
        listAdzanTime: Map<String, String>,
        adzanName: String,
        isChecked: Boolean,
        index: Int,
        resetSwitch: Boolean
    ) {
        val lowerCaseAdzanName = adzanLowerCase(adzanName)
        listAdzanTime[adzanName]?.let { adzanTime ->
            if (isChecked) {
                adzanReceiver.setAdzanReminder(
                    context = requireContext(),
                    adzanName = adzanName,
                    adzanTime = adzanTime,
                    adzanCode = index + 1,
                    isShubuh = adzanName == "Adzan Shubuh"
                )
                dataStoreViewModel.saveSwitchState(adzanName, true)
                (activity as MainActivity).customSnackbar(
                    true,
                    requireContext(),
                    binding.root,
                    "$lowerCaseAdzanName diaktifkan"
                )
            } else {
                adzanReceiver.cancelAdzanReminder(
                    context = requireContext(),
                    adzanCode = index + 1
                )
                dataStoreViewModel.saveSwitchState(adzanName, false)
                val message = if (resetSwitch) {
                    "Ubah lokasi berhasil, aktifkan kembali adzan"
                } else {
                    "$lowerCaseAdzanName dimatikan"
                }
                (activity as MainActivity).customSnackbar(
                    resetSwitch,
                    requireContext(),
                    binding.root,
                    message
                )
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

    private fun negativeCase() {
        binding.apply {
            clNegativeCase.visibility = View.VISIBLE
            shalatLayout.root.visibility = View.GONE
            lottieNoLocation.visibility = View.GONE
            lottieNoInternet.visibility = View.VISIBLE
            tvResult.text = getString(R.string.no_internet)
            clNegativeCase.applyConstraint {
                topToBottom(tvResult, lottieNoInternet, 16)
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
}