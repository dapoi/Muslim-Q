package com.prodev.muslimq.presentation.view.shalat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.apachat.primecalendar.core.hijri.HijriCalendar
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentShalatBinding
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.ShalatViewModel
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
        binding = FragmentShalatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewModel()

        binding.apply {

            ivIconChoose.setOnClickListener {
                findNavController().navigate(R.id.shalatProvinceFragment)
            }

            dateGregorianAndHijri()
        }
    }

    private fun setViewModel() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        dataStoreViewModel.getCityData.observe(viewLifecycleOwner) { cityData ->
            binding.tvChooseLocation.text = cityData
//            shalatViewModel.getShalatDailyByCity(
//                cityData.second, currentYear, currentMonth + 1, currentDay
//            ).observe(viewLifecycleOwner) {
//                when {
//                    it is Resource.Loading && it.data == null -> binding.progressBar.visibility =
//                        View.VISIBLE
//                    it is Resource.Error -> {
//                        binding.progressBar.visibility = View.GONE
//                        Log.e("ShalatFragment", it.error.toString())
//                    }
//                    else -> {
//                        Log.d("ShalatFragment", it.data.toString())
//                        binding.apply {
//                            shalatLayout.root.visibility = View.VISIBLE
//                            shalatLayout.tvShubuhTime.text = it.data?.subuh
//                            shalatLayout.tvDzuhurTime.text = it.data?.dzuhur
//                            shalatLayout.tvAsharTime.text = it.data?.ashar
//                            shalatLayout.tvMaghribTime.text = it.data?.maghrib
//                            shalatLayout.tvIsyaTime.text = it.data?.isya
//                        }
//                    }
//                }
//            }
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