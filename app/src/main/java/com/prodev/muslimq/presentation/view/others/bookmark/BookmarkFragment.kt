package com.prodev.muslimq.presentation.view.others.bookmark

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.DialogDeleteAllBinding
import com.prodev.muslimq.databinding.FragmentBookmarkBinding
import com.prodev.muslimq.presentation.adapter.QuranBookmarkAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.view.quran.QuranDetailFragment
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private val quranBookmarkAdapter = QuranBookmarkAdapter()
    private val quranBookmarkViewModel: QuranViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initViewModel()

        binding.apply {
            ivBack.setOnClickListener { findNavController().navigateUp() }

            ivMore.setOnClickListener { showMenuOption() }
        }
    }

    private fun showMenuOption() {
        val popupMenu = PopupMenu(requireContext(), binding.ivMore)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.menuInflater.inflate(R.menu.menu_bookmark, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_delete_all -> {
                    val builder = AlertDialog.Builder(
                        requireContext(),
                        R.style.CurvedDialog,
                    ).create()
                    val dialogLayout = DialogDeleteAllBinding.inflate(layoutInflater)
                    val tvConfirm = dialogLayout.tvConfirm
                    val tvCancel = dialogLayout.tvCancel
                    with(builder) {
                        setView(dialogLayout.root)
                        tvConfirm.setOnClickListener {
                            quranBookmarkViewModel.deleteAllBookmark()
                            dismiss()
                        }
                        tvCancel.setOnClickListener { dismiss() }
                        setCanceledOnTouchOutside(false)
                        show()
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun initAdapter() {
        binding.rvSurah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quranBookmarkAdapter
        }
        quranBookmarkAdapter.onItemClick = { surah ->
            findNavController().navigate(R.id.action_quranBookmarkFragment_to_quranDetailFragment,
                Bundle().apply {
                    putString(QuranDetailFragment.SURAH_NAME, surah.nama)
                    putInt(QuranDetailFragment.SURAH_NUMBER, surah.surahId)
                    putString(QuranDetailFragment.SURAH_DESC, surah.deskripsi)
                }
            )
        }
    }

    private fun initViewModel() {
        quranBookmarkViewModel.getBookmark().observe(viewLifecycleOwner) { quran ->
            binding.apply {
                if (quran.isNullOrEmpty()) {
                    clNegativeCase.visibility = View.VISIBLE
                    rvSurah.visibility = View.GONE
                    ivMore.visibility = View.GONE
                } else {
                    clNegativeCase.visibility = View.GONE
                    rvSurah.visibility = View.VISIBLE
                    ivMore.visibility = View.VISIBLE
                    quranBookmarkAdapter.setList(quran)
                }
            }
        }
    }
}