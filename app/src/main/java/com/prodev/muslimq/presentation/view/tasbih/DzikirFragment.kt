package com.prodev.muslimq.presentation.view.tasbih

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.helper.capitalizeEachWord
import com.prodev.muslimq.helper.hideKeyboard
import com.prodev.muslimq.databinding.FragmentDzikirBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.DzikirAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DzikirFragment : BaseFragment<FragmentDzikirBinding>(FragmentDzikirBinding::inflate) {

    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val tasbihViewModel: TasbihViewModel by viewModels()
    private var selectedType: DzikirType? = DzikirType.DEFAULT
    private lateinit var dzikirAdapter: DzikirAdapter
    private lateinit var listOfDzikir: List<TasbihEntity>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().popBackStack() }
        binding.chipAdd.setOnClickListener {
            InputDialog().showInputDialog(
                listOfDzikir,
                layoutInflater,
                requireContext()
            ) { dzikirName, dzikirCount, oldList ->
                if (oldList != null) {
                    insertDzikir(dzikirName, dzikirCount, oldList)
                }
            }
        }

        initAdapter()
        initDzikir()
        initSpinner()
    }

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipe_dzikir,
            R.layout.item_spinner
        ).also { spinnerAdapter ->
            spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
            binding.spinnerDzikir.adapter = spinnerAdapter
        }

        dataStoreViewModel.getSelectedDzikirType.observe(viewLifecycleOwner) {
            binding.spinnerDzikir.apply {
                setSelection(it)

                viewLifecycleOwner.lifecycleScope.launch {
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            when (position) {
                                0 -> selectedType = DzikirType.DEFAULT
                                1 -> selectedType = DzikirType.PAGI
                                2 -> selectedType = DzikirType.SORE
                                3 -> selectedType = DzikirType.SHALAT
                                4 -> selectedType = DzikirType.CUSTOM
                            }
                            setFragmentResult(REQUEST_DELETE_OR_UPDATE, Bundle().apply {
                                putBoolean(BUNDLE_DELETE_OR_UPDATE, true)
                            })
                            tasbihViewModel.getDzikirByType(selectedType!!)
                            tasbihViewModel.selectedItemIndexVM = 0
                            dataStoreViewModel.saveSelectedDzikirType(selectedType!!)

                            binding.chipAdd.isVisible = selectedType == DzikirType.CUSTOM
                        }

                        override fun onNothingSelected(aprent: AdapterView<*>?) {

                        }
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        dzikirAdapter = DzikirAdapter(
            deleteListener = {
                tasbihViewModel.deleteDzikir(it.dzikirName)

                setFragmentResult(REQUEST_DELETE_OR_UPDATE, Bundle().apply {
                    putBoolean(BUNDLE_DELETE_OR_UPDATE, true)
                })

                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Dzikir berhasil dihapus"
                )
                tasbihViewModel.getDzikirByType(DzikirType.CUSTOM)
            }
        )

        binding.rvDzikir.adapter = dzikirAdapter
    }

    private fun initDzikir() {
        tasbihViewModel.getDzikirList.observe(viewLifecycleOwner) { response ->
            val filteredList = response.filter { it.dzikirType == selectedType }
            dzikirAdapter.submitList(filteredList)
            listOfDzikir = filteredList

            binding.apply {
                clNegativeCase.isVisible = filteredList.isEmpty()
                rvDzikir.isVisible = filteredList.isNotEmpty()
            }
        }
    }

    private fun insertDzikir(
        dzikir: String,
        dzikirCount: String,
        listOfDzikir: List<TasbihEntity>
    ) {
        val state = dzikir.isNotEmpty() && dzikirCount.isNotEmpty() && !listOfDzikir.any {
            it.dzikirName.equals(dzikir, true)
        }
        val messageSnackbar = when {
            dzikir.isEmpty() -> "Dzikir belum diisi"
            dzikirCount.isEmpty() || dzikirCount == "0" -> "Jumlah dzikir tidak boleh kurang dari 1"
            !state -> "Dzikir sudah ada"
            else -> "Dzikir berhasil ditambahkan"
        }

        (activity as MainActivity).customSnackbar(
            state = state,
            context = requireContext(),
            view = binding.root,
            message = messageSnackbar
        )

        if (state) {
            tasbihViewModel.insertDzikir(
                TasbihEntity(
                    capitalizeEachWord(dzikir),
                    DzikirType.CUSTOM,
                    maxCount = dzikirCount.toInt()
                )
            )
            dataStoreViewModel.saveSelectedDzikirType(DzikirType.CUSTOM)
            selectedType = DzikirType.CUSTOM
            hideKeyboard(requireActivity())
        }
    }

    companion object {
        const val REQUEST_DELETE_OR_UPDATE = "request_delete_or_update"
        const val BUNDLE_DELETE_OR_UPDATE = "bundle_delete_or_update"
    }
}