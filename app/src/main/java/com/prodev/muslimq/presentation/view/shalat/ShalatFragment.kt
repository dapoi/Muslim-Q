package com.prodev.muslimq.presentation.view.shalat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aminography.primecalendar.hijri.HijriCalendar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity
import com.prodev.muslimq.core.utils.AdzanConstants
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.databinding.DialogGetLocationBinding
import com.prodev.muslimq.databinding.DialogLoadingBinding
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.helper.capitalizeEachWord
import com.prodev.muslimq.helper.getMaterialTargetPrompt
import com.prodev.muslimq.helper.isOnline
import com.prodev.muslimq.helper.swipeRefresh
import com.prodev.muslimq.notification.AdzanReceiver
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
import dagger.hilt.android.AndroidEntryPoint
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ShalatFragment : BaseFragment<FragmentShalatBinding>(FragmentShalatBinding::inflate) {

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
    private var lat: Double? = null
    private var lon: Double? = null

    private val requestPermissionPostNotification = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        dataStoreViewModel.saveSwitchState(stateAdzanName, isGranted)
        if (isGranted) {
            val alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
                Intent().apply {
                    action = ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                }.also { startActivity(it) }
            } else {
                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "${adzanLowerCase(stateAdzanName)} diaktifkan"
                )
            }
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
        val resultOK = result.resultCode == RESULT_OK
        if (!resultOK) transparentDialog.dismiss()

        val message = if (resultOK) "GPS diaktifkan" else "GPS tidak diaktifkan"

        (activity as MainActivity).customSnackbar(
            state = resultOK,
            context = requireContext(),
            view = binding.root,
            message = message
        )
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionGranted = permissions.getOrDefault(
            Manifest.permission.ACCESS_FINE_LOCATION, false
        ) || permissions.getOrDefault(
            Manifest.permission.ACCESS_COARSE_LOCATION, false
        )

        if (permissionGranted) getLiveLocation()

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
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adzanReceiver = AdzanReceiver()
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
        val dialogLayout = DialogLoadingBinding.inflate(layoutInflater)
        transparentDialog.setView(dialogLayout.root)

        binding.apply {
            ivIconChoose.setOnClickListener {
                showDialogLocation()
            }

            val pm: PackageManager = requireContext().packageManager
            if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)) {
                // This device does not have a compass, turn off the compass feature
                tvQibla.isVisible = false
            }
        }

        swipeRefresh(
            { shalatViewModel.refreshShalatTime() },
            binding.srlShalat
        )
        initViewModel()
        dateGregorianAndHijri()

        // handle deeplink
        val isFromNotif = arguments?.getBoolean(AdzanConstants.FROM_NOTIFICATION)
        if (isFromNotif == true) {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
                visibility = View.INVISIBLE
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
                isOnline = isOnline(requireContext())
                if (isOnline) {
                    checkStateLocationPermission()
                } else {
                    (activity as MainActivity).customSnackbar(
                        state = false,
                        context = requireContext(),
                        view = binding.root,
                        message = "Tidak ada koneksi internet"
                    )
                }
            }
            tvChooseManual.setOnClickListener {
                dismiss()
                findNavController().navigate(
                    ShalatFragmentDirections.actionShalatFragmentToShalatProvinceFragment()
                )
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
                getLiveLocation()
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
            fusedLocation.lastLocation.addOnSuccessListener { location: Location? ->
                transparentDialog.show()
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    getAddressGeocoder(lat!!, lon!!)
                } else {
                    requestNewLiveLocation()
                }
            }
        }
    }

    private fun requestNewLiveLocation() {
        val locRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(1),
        ).apply {
            setWaitForAccurateLocation(true)
            setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(1))
        }.build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locRequest)
        val client = LocationServices.getSettingsClient(requireContext())
        client.checkLocationSettings(builder.build()).apply {
            addOnSuccessListener { getLiveLocation() }
            addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        requestGPSPermission.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(requireContext(), sendEx.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        try {
            fusedLocation.requestLocationUpdates(
                locRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                lat = location.latitude
                lon = location.longitude
                getAddressGeocoder(lat!!, lon!!)
            }
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
                        dataStoreViewModel.saveAreaData(capitalizeEachWord(city), country)
                        resetSwitch = true
                        transparentDialog.dismiss()
                    }
                }

                override fun onError(error: String?) {
                    super.onError(error)
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            try {
                val addresses = geocoder.getFromLocationName("$lat, $lon", 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality
                    val country = addresses[0].countryName
                    dataStoreViewModel.saveAreaData(capitalizeEachWord(city), country)
                    resetSwitch = true
                    transparentDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Alamat tidak ditemukan", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: IOException) {
                transparentDialog.dismiss()
                e.printStackTrace()
            }
        }
    }

    private fun navigateToQibla(latUser: Double?, location: List<String>) {
        findNavController().navigate(
            ShalatFragmentDirections.actionShalatFragmentToQiblaFragment(
                latUser!!.toFloat(), location.toTypedArray()
            )
        )

        transparentDialog.dismiss()
    }

    private fun dateGregorianAndHijri() {
        binding.apply {
            val indonesia = Locale("in", "ID")
            val simpleDateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", indonesia)
            val date = simpleDateFormat.format(Date())
            tvGregorianDate.text = date

            val hijriCalendar = HijriCalendar(locale = indonesia)
            val hijriDate =
                "${hijriCalendar.dayOfMonth} ${hijriCalendar.monthName} ${hijriCalendar.year} H"
            tvIslamicDate.text = hijriDate
        }
    }

    private fun initViewModel() {
        shalatViewModel.getShalatTime.observe(viewLifecycleOwner) { result ->
            binding.apply {
                val isLoading = result is Resource.Loading
                val isSuccess = result is Resource.Success || result.data != null && !isLoading
                val isError = result is Resource.Error && result.data == null

                progressBar.isVisible = isLoading
                clNegativeCase.isVisible = isError
                shalatLayout.root.isVisible = isSuccess
                if (isSuccess) getAllShalatData(result.data)

                tvResult.text = getString(R.string.no_internet)
                tvQibla.setOnClickListener {
                    navigateToQibla(
                        result.data?.lat,
                        listOf(result.data?.city!!, result.data?.country!!)
                    )
                }
            }
        }

        dataStoreViewModel.apply {
            getTapPromptState.observe(viewLifecycleOwner) { state ->
                if (!state) {
                    initTapPrompt()
                    dataStoreViewModel.saveTapPromptState(true)
                }
            }

            getAreaData.observe(viewLifecycleOwner) { area ->
                binding.tvYourLocation.text = area.first
                shalatViewModel.setShalatTime(area)
            }
        }
    }

    private fun initTapPrompt() {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        (activity as MainActivity).showOverlay(true)
        bottomNav.visibility = View.INVISIBLE
        val materialTapTargetSequence = MaterialTapTargetSequence()
        val targetTwo = getMaterialTargetPrompt(
            activity = requireActivity(),
            view = binding.ivIconChoose,
            title = "Ubah Lokasi",
            desc = "Klik di sini untuk mengubah lokasi",
            onClick = { prompt ->
                prompt.dismiss()
                materialTapTargetSequence.finish()
                (activity as MainActivity).showOverlay(false)
                bottomNav.visibility = View.VISIBLE
            }
        )
        val targetOne = getMaterialTargetPrompt(
            activity = requireActivity(),
            view = binding.tvYourLocation,
            title = "Lokasi Awal",
            desc = "DKI Jakarta merupakan lokasi awal yang akan ditampilkan",
            onClick = { prompt ->
                prompt.dismiss()
                targetTwo?.show()
            }
        )
        materialTapTargetSequence.addPrompt(targetOne)
        materialTapTargetSequence.show()
    }

    private fun getAllShalatData(data: ShalatEntity?) {
        // shalat time with zone
        shubuhWithZone = data?.shubuh.toString()
        dzuhurWithZone = data?.dzuhur.toString()
        asharWithZone = data?.ashar.toString()
        maghribWithZone = data?.maghrib.toString()
        isyaWithZone = data?.isya.toString()

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
            AdzanConstants.KEY_ADZAN_SHUBUH to shubuh,
            AdzanConstants.KEY_ADZAN_DZUHUR to dzuhur,
            AdzanConstants.KEY_ADZAN_ASHAR to ashar,
            AdzanConstants.KEY_ADZAN_MAGHRIB to maghrib,
            AdzanConstants.KEY_ADZAN_ISYA to isya
        )

        val adzanNames = listAdzanTime.keys.toList()

        binding.shalatLayout.apply {
            val listOfSwitch = listOf(
                swShubuh, swDzuhur, swAshar, swMaghrib, swIsya
            )

            if (resetSwitch) {
                for (i in listOfSwitch.indices) {
                    listOfSwitch[i].isChecked = false
                }

                resetSwitch = false
            }

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

                listAdzanTime[adzanName]?.let { adzanTime ->
                    if (switch.isChecked) {
                        adzanReceiver.setAdzanReminder(
                            context = requireContext(),
                            adzanName = adzanName,
                            adzanTime = adzanTime,
                            adzanCode = index + 1,
                            adzanLocation = binding.tvYourLocation.text.toString(),
                            isShubuh = adzanName.contains("Shubuh")
                        )
                    } else {
                        adzanReceiver.cancelAdzanReminder(
                            requireContext(),
                            index + 1
                        )
                    }
                }
            }
        }

        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.apply {
            if (!tvYourLocation.text.contains("DKI")) {
                // reset all backgrounds
                resetBackgrounds()

                when {
                    timeNow > shubuh && timeNow <= dzuhur -> {
                        tvTimeShalat.text = "${countDownShalat(dzuhur, timeNow)} menuju dzuhur"
                        setNextPrayShalatBackground(shalatLayout.clDzuhur)
                    }

                    timeNow > dzuhur && timeNow <= ashar -> {
                        tvTimeShalat.text = "${countDownShalat(ashar, timeNow)} menuju ashar"
                        setNextPrayShalatBackground(shalatLayout.clAshar)
                    }

                    timeNow > ashar && timeNow <= maghrib -> {
                        tvTimeShalat.text = "${countDownShalat(maghrib, timeNow)} menuju maghrib"
                        setNextPrayShalatBackground(shalatLayout.clMaghrib)
                    }

                    timeNow > maghrib && timeNow <= isya -> {
                        tvTimeShalat.text = "${countDownShalat(isya, timeNow)} menuju isya"
                        setNextPrayShalatBackground(shalatLayout.clIsya)
                    }

                    else -> {
                        tvTimeShalat.text = "${countDownShalat(shubuh, timeNow)} menuju shubuh"
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
                    adzanLocation = binding.tvYourLocation.text.toString(),
                    isShubuh = adzanName == AdzanConstants.KEY_ADZAN_SHUBUH
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
                if (resetSwitch) {
                    this.resetSwitch = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as MainActivity).customSnackbar(
                            true,
                            requireContext(),
                            binding.root,
                            "Ubah lokasi berhasil, aktifkan kembali adzan"
                        )
                    }, 600)
                } else {
                    (activity as MainActivity).customSnackbar(
                        false,
                        requireContext(),
                        binding.root,
                        "$lowerCaseAdzanName dimatikan"
                    )
                }
            }
        }
    }

    private fun resetBackgrounds() {
        binding.apply {
            shalatLayout.clDzuhur.background = null
            shalatLayout.clAshar.background = null
            shalatLayout.clMaghrib.background = null
            shalatLayout.clIsya.background = null
            shalatLayout.clShubuh.background = null
        }
    }

    private fun setNextPrayShalatBackground(clShalat: ConstraintLayout) {
        clShalat.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_item_shalat
        )
    }

    private fun countDownShalat(timeShalat: String, timeNow: String): String {
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

    private fun adzanLowerCase(adzanName: String): String {
        return adzanName.substring(0, 1).uppercase() + adzanName.substring(1).lowercase()
    }

    override fun onDestroyView() {
        fusedLocation.removeLocationUpdates(locationCallback)
        super.onDestroyView()
    }
}