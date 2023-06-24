package com.prodev.muslimq.presentation.view.others.bookmark

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.DialogDeleteAllBinding
import com.prodev.muslimq.databinding.DialogInfoSurahBinding
import com.prodev.muslimq.databinding.FragmentBookmarkBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.adapter.QuranBookmarkAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.view.quran.QuranDetailFragment
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private lateinit var quranBookmarkAdapter: QuranBookmarkAdapter

    private val quranBookmarkViewModel: QuranViewModel by viewModels()
    private val curvedDialog by lazy {
        AlertDialog.Builder(requireContext(), R.style.CurvedDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initViewModel()

        binding.apply {
            ivBack.setOnClickListener { findNavController().navigateUp() }

            ivMore.setOnClickListener { showMenuOption() }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showMenuOption() {
        val popupMenu = PopupMenu(requireContext(), binding.ivMore)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        popupMenu.menuInflater.inflate(R.menu.menu_bookmark, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_info -> {
                    DialogInfoSurahBinding.inflate(layoutInflater).apply {
                        tvInfoTitle.text = "Info"
                        tvInfoMessage.text =
                            "Anda dapat menghapus tiap surah dengan cara geser kiri atau kanan"
                        with(curvedDialog.create()) {
                            setView(root)
                            tvInfoClose.setOnClickListener { dismiss() }
                            show()
                        }
                    }
                    true
                }

                R.id.action_delete_all -> {
                    DialogDeleteAllBinding.inflate(layoutInflater).apply {
                        with(curvedDialog.create()) {
                            setView(root)
                            tvConfirm.setOnClickListener {
                                quranBookmarkViewModel.deleteAllBookmark()
                                dismiss()

                                (activity as MainActivity).customSnackbar(
                                    state = true,
                                    context = requireContext(),
                                    view = binding.root,
                                    message = "Berhasil menghapus semua surah"
                                )
                            }
                            tvCancel.setOnClickListener { dismiss() }
                            show()
                        }
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun initAdapter() {
        quranBookmarkAdapter = QuranBookmarkAdapter(
            onItemClick = { surah ->
                findNavController().navigate(R.id.action_quranBookmarkFragment_to_quranDetailFragment,
                    Bundle().apply {
                        putString(QuranDetailFragment.SURAH_NAME, surah.nama)
                        putInt(QuranDetailFragment.SURAH_NUMBER, surah.surahId)
                        putString(QuranDetailFragment.SURAH_DESC, surah.deskripsi)
                    }
                )
            }
        )

        binding.rvSurah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quranBookmarkAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val surah = quranBookmarkAdapter.getSurahAt(position)
                quranBookmarkViewModel.deleteBookmark(surah.surahId)

                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Berhasil menghapus surah ${surah.namaLatin}",
                )
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvSurah)
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
                    quranBookmarkAdapter.submitList(quran)
                }
            }
        }
    }
}