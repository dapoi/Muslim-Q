package com.prodev.muslimq.presentation.view.others

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.uitheme.UITheme
import com.prodev.muslimq.databinding.DialogSettingNotificationBinding
import com.prodev.muslimq.databinding.FragmentOthersBinding
import com.prodev.muslimq.presentation.adapter.OthersAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint

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

            setList(othersItem)

            onClick = { item ->
                when (item.title) {
                    "Baca Nanti" -> {
                        findNavController().navigate(
                            R.id.action_othersFragment_to_quranBookmarkFragment
                        )
                    }

                    "Pengaturan Notifikasi" -> {
                        showDialogNotifSettings()
                    }

                    "Kirim Masukan" -> {
                        val email = "luthfidaffaprabowo@gmail.com"
                        val subject = "Feedback Aplikasi Muslim Q"
                        val body = "Silahkan tulis pesan Anda di sini"

                        // intent to send email
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        startActivity(Intent.createChooser(intent, "Pilih aplikasi email"))
                    }

                    "Info Aplikasi" -> {
                        findNavController().navigate(R.id.action_othersFragment_to_aboutAppFragment)
                    }
                }
            }
        }
    }

    private fun showDialogNotifSettings() {
        val notifDialog = DialogSettingNotificationBinding.inflate(layoutInflater)
        val radioAdzanNotif = notifDialog.rbSoundNotif
        val radioOnlyNotif = notifDialog.rbOnlyNotif
        val btnSave = notifDialog.btnSave
        with(curvedDialog.create()) {
            setView(notifDialog.root)
            dataStoreViewModel.getAdzanSoundState.observe(viewLifecycleOwner) { isSoundActive ->
                if (isSoundActive) radioAdzanNotif.isChecked = true
                else radioOnlyNotif.isChecked = true
            }
            radioAdzanNotif.setOnClickListener {
                radioOnlyNotif.isChecked = false
            }
            radioOnlyNotif.setOnClickListener {
                radioAdzanNotif.isChecked = false
            }
            btnSave.setOnClickListener {
                val isAdzanNotifChecked = radioAdzanNotif.isChecked
                dataStoreViewModel.saveAdzanSoundState(isAdzanNotifChecked)
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            setOnDismissListener {
                dataStoreViewModel.getAdzanSoundState.removeObservers(viewLifecycleOwner)
            }
            show()
        }
    }
}