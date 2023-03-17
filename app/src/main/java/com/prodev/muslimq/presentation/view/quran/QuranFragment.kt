package com.prodev.muslimq.presentation.view.quran

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.utils.InternetReceiver
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.core.utils.isOnline
import com.prodev.muslimq.databinding.FragmentQuranBinding
import com.prodev.muslimq.presentation.adapter.QuranAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuranFragment : Fragment() {

    private lateinit var binding: FragmentQuranBinding
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var quranAdapter: QuranAdapter

    private val quranViewModel: QuranViewModel by viewModels()
    private val dataStorePreference: DataStoreViewModel by viewModels()

    private var isOnline = false
    private var isFirstLoad = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
            isFirstLoad = false
        } else {
            binding = FragmentQuranBinding.inflate(inflater, container, false)
            isFirstLoad = true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav = requireActivity().findViewById(R.id.bottom_nav)
        isOnline = isOnline(requireActivity())

        binding.apply {
            getLastReadSurah(tvSurahName, tvSurahMeaning)

            fabBackToTop.setOnClickListener {
                // scroll to parent
                rvSurah.smoothScrollToPosition(0)
                appBar.setExpanded(true, true)
            }

            tilSurah.setEndIconOnClickListener {
                etSurah.text?.clear()
                hideKeyboard(requireActivity())
                etSurah.clearFocus()
            }

            etSurah.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        quranAdapter.filter.filter(s)
                    }
                })

                setOnEditorActionListener { _, _, _ ->
                    hideKeyboard(requireActivity())
                    etSurah.clearFocus()
                    true
                }
            }
        }

        if (isFirstLoad) {
            setAdapter()
            setViewModel()
        }
    }

    private fun getLastReadSurah(tvSurahName: TextView, tvSurahMeaning: TextView) {
        dataStorePreference.getSurah.observe(viewLifecycleOwner) { data ->
            if (data.first != "" || data.second != "") {
                tvSurahName.text = data.first

                tvSurahMeaning.visibility = View.VISIBLE
                tvSurahMeaning.text = data.second
            } else {
                tvSurahName.text = resources.getString(R.string.last_read_surah_empty)
                tvSurahMeaning.visibility = View.GONE
            }
        }
    }

    private fun setAdapter() {
        quranAdapter = QuranAdapter()
        quranAdapter.setOnItemClick(object : QuranAdapter.OnItemClickCallback {
            override fun onItemClick(surah: QuranEntity) {
                findNavController().navigate(R.id.action_quranFragment_to_quranDetailFragment,
                    Bundle().apply {
                        putInt(QuranDetailFragment.SURAH_NUMBER, surah.nomor)
                        putString(QuranDetailFragment.SURAH_NAME, surah.namaLatin)
                        putString(QuranDetailFragment.SURAH_DESC, surah.deskripsi)
                    })
                hideKeyboard(requireActivity())

                viewLifecycleOwner.lifecycleScope.launch {
                    dataStorePreference.saveSurah(surah.namaLatin, surah.arti)
                }

                binding.apply {
                    getLastReadSurah(tvSurahName, tvSurahMeaning)
                }
            }
        })

        binding.rvSurah.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = quranAdapter
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    binding.apply {
                        if (dy > 50 && !fabBackToTop.isShown) {
                            fabBackToTop.show()
                        }

                        if (dy < -50 && fabBackToTop.isShown) {
                            fabBackToTop.hide()
                        }

                        if (!canScrollVertically(-1)) {
                            fabBackToTop.hide()
                        }
                    }
                }
            })
        }
    }

    private fun setViewModel() {
        quranViewModel.getSurah().observe(viewLifecycleOwner) {
            with(binding) {
                srlSurah.apply {
                    setLottieAnimation("loading.json")
                    setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                    setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                    setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                        override fun onRefresh() {
                            val handlerData = Handler(Looper.getMainLooper())
                            isOnline = isOnline(requireContext())
                            if (isOnline) {
                                handlerData.postDelayed({
                                    setRefreshing(false)
                                }, 2000)

                                handlerData.postDelayed({
                                    if (it.data.isNullOrEmpty()) {
                                        InternetReceiver().onReceive(requireActivity(), Intent())
                                    } else {
                                        rvSurah.visibility = View.VISIBLE
                                        clNoInternet.visibility = View.GONE
                                    }
                                }, 2350)
                            } else {
                                rvSurah.visibility = View.GONE
                                clNoInternet.visibility = View.VISIBLE
                                setRefreshing(false)
                            }
                        }
                    })
                }

                when {
                    it is Resource.Loading && it.data.isNullOrEmpty() -> {
                        stateLoading(true)
                        clNoInternet.visibility = View.GONE
                    }
                    it is Resource.Error && it.data.isNullOrEmpty() -> {
                        stateLoading(false)
                        stateNoInternet(ctlHeader, clNoInternet, true)
                    }
                    else -> {
                        stateLoading(false)
                        stateNoInternet(ctlHeader, clNoInternet, false)
                        quranAdapter.setList(it.data!!)
                        bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        binding.apply {
            if (state) {
                progressBar.visibility = View.VISIBLE
                progressHeader.visibility = View.VISIBLE
                tvTitle.visibility = View.GONE
                clSurah.visibility = View.GONE
                bottomNav.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                progressHeader.visibility = View.GONE
                tvTitle.visibility = View.VISIBLE
                clSurah.visibility = View.VISIBLE
            }
        }
    }

    private fun stateNoInternet(
        ctlHeader: CollapsingToolbarLayout,
        clNoInternet: ConstraintLayout,
        isDataEmpty: Boolean,
    ) {
        if (isDataEmpty) {
            ctlHeader.visibility = View.GONE
            clNoInternet.visibility = View.VISIBLE
        } else {
            ctlHeader.visibility = View.VISIBLE
            clNoInternet.visibility = View.GONE
        }
    }
}