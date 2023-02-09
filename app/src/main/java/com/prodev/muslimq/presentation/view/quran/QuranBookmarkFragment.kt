package com.prodev.muslimq.presentation.view.quran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentQuranBookmarkBinding
import com.prodev.muslimq.presentation.adapter.QuranBookmarkAdapter
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranBookmarkFragment : Fragment() {

    private var _binding: FragmentQuranBookmarkBinding? = null
    private val binding get() = _binding!!
    private val quranBookmarkAdapter = QuranBookmarkAdapter()
    private val quranBookmarkViewModel: QuranViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initViewModel()

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    private fun initAdapter() {
        binding.rvSurah.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quranBookmarkAdapter
        }
        quranBookmarkAdapter.onItemClick = { surah ->
            findNavController().navigate(R.id.action_quranBookmarkFragment_to_quranDetailFragment,
                Bundle().apply {
                    putInt(QuranDetailFragment.SURAH_NUMBER, surah.nomor)
                    putString(QuranDetailFragment.SURAH_NAME, surah.namaLatin)
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
                } else {
                    clNegativeCase.visibility = View.GONE
                    rvSurah.visibility = View.VISIBLE
                    quranBookmarkAdapter.setList(quran)
                }
            }
        }
    }
}