package com.prodev.muslimq.presentation.view.others.bookmark

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
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
import com.prodev.muslimq.presentation.viewmodel.BookmarkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>(FragmentBookmarkBinding::inflate) {

    private lateinit var quranBookmarkAdapter: QuranBookmarkAdapter

    private val quranBookmarkViewModel: BookmarkViewModel by viewModels()
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
                            "Anda dapat menghapus tiap surah dengan cara geser ke kanan atau kiri"
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
                                    message = "Berhasil menghapus semua surah",
                                    toOtherFragment = true
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
                findNavController().navigate(
                    BookmarkFragmentDirections.actionQuranBookmarkFragmentToQuranDetailFragment(
                        surah.surahId
                    )
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
                val surah = quranBookmarkAdapter.currentList[position]
                quranBookmarkViewModel.deleteBookmark(surah)

                (activity as MainActivity).customSnackbar(
                    state = true,
                    context = requireContext(),
                    view = binding.root,
                    message = "Berhasil menghapus surah ${surah.namaLatin}",
                    toOtherFragment = true
                )
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvSurah)
    }

    private fun initViewModel() {
        quranBookmarkViewModel.getBookmark().observe(viewLifecycleOwner) { quran ->
            binding.apply {
                clNegativeCase.isVisible = quran.isNullOrEmpty()
                rvSurah.isVisible = quran.isNotEmpty()
                ivMore.isVisible = quran.isNotEmpty()

                quranBookmarkAdapter.submitList(quran)
            }
        }
    }
}