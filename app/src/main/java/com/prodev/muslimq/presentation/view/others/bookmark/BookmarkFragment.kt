package com.prodev.muslimq.presentation.view.others.bookmark

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentBookmarkBinding
import com.prodev.muslimq.presentation.adapter.QuranBookmarkAdapter
import com.prodev.muslimq.presentation.view.quran.QuranDetailFragment
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private val quranBookmarkAdapter = QuranBookmarkAdapter()
    private val quranBookmarkViewModel: QuranViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initViewModel()

        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

            ivMore.setOnClickListener { showMenuOption() }
        }
    }

    @SuppressLint("InflateParams")
    private fun showMenuOption() {
        val popupMenu = PopupMenu(requireContext(), binding.ivMore)
        popupMenu.menuInflater.inflate(R.menu.menu_bookmark, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_delete_all -> {
                    val builder = AlertDialog.Builder(requireActivity()).create()
                    val dialogLayout = layoutInflater.inflate(R.layout.dialog_delete_all, null)
                    val tvConfirm = dialogLayout.findViewById<TextView>(R.id.tv_confirm)
                    val tvCancel = dialogLayout.findViewById<TextView>(R.id.tv_cancel)
                    with(builder) {
                        setView(dialogLayout)
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
                    putInt(QuranDetailFragment.SURAH_NUMBER, surah.nomor)
                    putString(QuranDetailFragment.SURAH_DESC, surah.deskripsi)
                }
            )
        }
    }

    private fun initViewModel() {
        quranBookmarkViewModel.getBookmark().observe(viewLifecycleOwner) { quran ->
            with(binding) {
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