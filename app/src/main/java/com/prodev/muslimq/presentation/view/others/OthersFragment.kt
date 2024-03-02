package com.prodev.muslimq.presentation.view.others

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.UITheme
import com.prodev.muslimq.databinding.DialogSettingNotificationBinding
import com.prodev.muslimq.databinding.FragmentOthersBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.OthersAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.view.others.BottomSheetMuadzinFragment.BottomSheetMuadzinCallback
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OthersFragment : BaseFragment<FragmentOthersBinding>(FragmentOthersBinding::inflate) {

    private lateinit var othersAdapter: OthersAdapter

    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
    }

    private fun setRecyclerView() {
        val othersData = OthersObject.listData
        val othersItem = ArrayList<Others>()
        othersData.let { othersItem.addAll(it) }

        othersAdapter = OthersAdapter()
        othersAdapter.apply {
            setList(othersItem)

            with(binding.rvOthers) {
                adapter = this@apply
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }

            dataStoreViewModel.getSwitchDarkMode.observe(viewLifecycleOwner) { uiTheme ->
                setSwitchState(uiTheme)
            }

            onSwitch = { state ->
                when (state) {
                    true -> {
                        dataStoreViewModel.saveSwitchDarkMode(UITheme.DARK)
                    }

                    false -> {
                        dataStoreViewModel.saveSwitchDarkMode(UITheme.LIGHT)
                    }
                }
            }

            onClick = { navigateBasedOnTitle(it.title) }
        }
    }

    private fun navigateBasedOnTitle(title: String) {
        when {
            title.contains("Baca") -> {
                navigate(OthersFragmentDirections.actionOthersFragmentToQuranBookmarkFragment())
            }

            title.contains("Husna") -> {
                navigate(OthersFragmentDirections.actionOthersFragmentToAsmaulHusnaFragment())
            }

            title.contains("Tasbih") -> {
                navigate(OthersFragmentDirections.actionOthersFragmentToTasbihFragment())
            }

            title.contains("Notifikasi") -> {
                showDialogNotifSettings()
            }

            title.contains("Muadzin") -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    dataStoreViewModel.getMuadzin.first().let { data ->
                        val muadzinRegular = data.first
                        val muadzinShubuh = data.second
                        showBottomSheet(muadzinRegular, muadzinShubuh)
                    }
                }
            }

            title.contains("Info") -> {
                navigate(OthersFragmentDirections.actionOthersFragmentToAboutAppFragment())
            }
        }
    }

    private fun navigate(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun showDialogNotifSettings() {
        val notifDialog = DialogSettingNotificationBinding.inflate(layoutInflater)
        notifDialog.btnSave.isVisible = false
        val radioAdzanNotif = notifDialog.rbSoundNotif
        val radioOnlyNotif = notifDialog.rbOnlyNotif
        with(curvedDialog.create()) {
            setView(notifDialog.root)
            dataStoreViewModel.getAdzanSoundState.observe(viewLifecycleOwner) { data ->
                val isSoundActive = data.first
                if (isSoundActive) radioAdzanNotif.isChecked = true
                else radioOnlyNotif.isChecked = true
            }
            radioAdzanNotif.setOnClickListener {
                saveAdzanSettingState(true)
                dismiss()
            }
            radioOnlyNotif.setOnClickListener {
                saveAdzanSettingState(false)
                dismiss()
            }
            setOnDismissListener {
                dataStoreViewModel.getAdzanSoundState.removeObservers(viewLifecycleOwner)
            }
            show()
        }
    }

    private fun saveAdzanSettingState(isSoundActive: Boolean) {
        val message = if (isSoundActive) "Suara adzan diaktifkan" else "Suara adzan dimatikan"
        dataStoreViewModel.saveAdzanSoundState(isSoundActive)
        (activity as MainActivity).customSnackbar(
            state = isSoundActive,
            message = message,
            context = requireContext(),
            view = binding.root
        )
    }

    private fun showBottomSheet(muadzinRegular: String, muadzinShubuh: String) {
        val listMuadzinRegular = listOf(
            "Ali Ahmad Mullah", "Hafiz Mustafa Özcan", "Mishary Rashid Alafasy"
        )
        val listMuadzinFajr = listOf(
            "Abu Hazim", "Salah Mansoor Az-Zahrani"
        )
        val listMuadzinSelected = listOf(
            muadzinRegular, muadzinShubuh
        )
        val bundle = Bundle().apply {
            putStringArray(
                BottomSheetMuadzinFragment.KEY_MUADZIN_REGULAR,
                listMuadzinRegular.toTypedArray()
            )
            putStringArray(
                BottomSheetMuadzinFragment.KEY_MUADZIN_FAJR,
                listMuadzinFajr.toTypedArray()
            )
            putStringArray(
                BottomSheetMuadzinFragment.KEY_MUADZIN_SELECTED,
                listMuadzinSelected.toTypedArray()
            )
        }

        BottomSheetMuadzinFragment().apply {
            arguments = bundle
            setBottomSheetMuadzinCallback(object : BottomSheetMuadzinCallback {
                override fun onMuadzinSelected(muadzinRegular: String, muadzinShubuh: String) {
                    dataStoreViewModel.saveMuadzin(muadzinRegular, muadzinShubuh)
                    (activity as MainActivity).customSnackbar(
                        state = true,
                        message = "Muadzin berhasil diubah",
                        context = requireContext(),
                        view = binding.root
                    )
                }
            })
        }.show(childFragmentManager, "BottomSheetMuadzinFragment")
    }
}