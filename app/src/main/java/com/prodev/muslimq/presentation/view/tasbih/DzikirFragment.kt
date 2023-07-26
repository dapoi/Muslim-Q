package com.prodev.muslimq.presentation.view.tasbih

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.core.utils.capitalizeEachWord
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.databinding.FragmentDzikirBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.DzikirAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.TasbihViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DzikirFragment : BaseFragment<FragmentDzikirBinding>(FragmentDzikirBinding::inflate) {
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val tasbihViewModel: TasbihViewModel by viewModels()
    private lateinit var dzikirAdapter: DzikirAdapter
    private lateinit var selectedType: DzikirType
    private lateinit var listOfDzikir: List<TasbihEntity>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.chipAdd?.setOnClickListener {
            InputDialog().showInputDialog(true, 0, listOfDzikir, layoutInflater, requireContext()){new, oldList->
                if (oldList != null){
                    insertDzikir(new, oldList)
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
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinnerDzikir?.adapter = adapter
        }
        dataStoreViewModel.getSelectedDzikirType.observe(viewLifecycleOwner){
            binding.spinnerDzikir?.setSelection(it)
            binding.spinnerDzikir?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when(position){
                        0 -> selectedType = DzikirType.DEFAULT
                        1 -> selectedType = DzikirType.PAGI
                        2 -> selectedType = DzikirType.SORE
                        3 -> selectedType = DzikirType.SHALAT
                        4 -> selectedType = DzikirType.CUSTOM
                    }
                    tasbihViewModel.getDzikirByType(selectedType)
                    dataStoreViewModel.saveSelectedDzikirType(selectedType)
                    initDzikir()
                }

                override fun onNothingSelected(aprent: AdapterView<*>?) {

                }

            }
        }
    }

    private fun initAdapter() {
        dzikirAdapter = DzikirAdapter(
            deleteListener = {
                tasbihViewModel.deleteDzikir(it.dzikirName)

                setFragmentResult(REQUEST_DELETE, Bundle().apply {
                    putBoolean(BUNDLE_DELETE, true)
                })

                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Dzikir berhasil dihapus"
                )
                tasbihViewModel.getDzikirByType(DzikirType.CUSTOM)
                initDzikir()
            }
        )

        binding.rvDzikir.adapter = dzikirAdapter
    }

    private fun initDzikir() {
        tasbihViewModel.getDzikirList.observe(viewLifecycleOwner) { response ->
            binding.apply {
                if (response.isNullOrEmpty()){
                    clNegativeCase?.visibility = View.VISIBLE
                    rvDzikir.visibility = View.GONE
                } else {
                    clNegativeCase?.visibility = View.GONE
                    rvDzikir.visibility = View.VISIBLE
                    dzikirAdapter.submitList(response)
                    listOfDzikir = response
                }
            }
        }
    }

    private fun insertDzikir(dzikir: String, listOfDzikir: List<TasbihEntity>) {
        val state = dzikir.isNotEmpty() && !listOfDzikir.contains(
            TasbihEntity(capitalizeEachWord(dzikir))
        )
        val messageSnackbar = when {
            dzikir.isEmpty() -> "Dzikir belum diisi"
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
            tasbihViewModel.insertDzikir(TasbihEntity(capitalizeEachWord(dzikir), DzikirType.CUSTOM))
            dataStoreViewModel.saveSelectedDzikirType(DzikirType.CUSTOM)
            selectedType = DzikirType.CUSTOM
            hideKeyboard(requireActivity())
        }
    }

    companion object {
        const val REQUEST_DELETE = "request_delete"
        const val BUNDLE_DELETE = "bundle_delete"
    }
}