package com.prodev.muslimq.presentation.view.quran

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
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
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranFragment : BaseFragment<FragmentQuranBinding>(FragmentQuranBinding::inflate) {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var quranAdapter: QuranAdapter

    private val quranViewModel: QuranViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private var isOnline = false

    private var surahId: Int? = null
    private var ayahNumber: Int? = null
    private var surahDesc: String? = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav = requireActivity().findViewById(R.id.bottom_nav)
        isOnline = isOnline(requireActivity())

        binding.apply {
            fabBackToTop.setOnClickListener {
                // scroll to parent
                rvSurah.scrollToPosition(0)
                appBar.setExpanded(true, true)
            }
        }

        setAdapter()
        setViewModel()
    }

    private fun setAdapter() {
        quranAdapter = QuranAdapter(binding.emptyState.root)
        quranAdapter.setOnItemClick(object : QuranAdapter.OnItemClickCallback {
            override fun onItemClick(surah: QuranEntity) {
                findNavController().navigate(
                    R.id.action_quranFragment_to_quranDetailFragment,
                    Bundle().apply {
                        putString(QuranDetailFragment.SURAH_NAME, surah.namaLatin)
                        putInt(QuranDetailFragment.SURAH_NUMBER, surah.nomor)
                        putString(QuranDetailFragment.SURAH_DESC, surah.deskripsi)
                    })
                hideKeyboard(requireActivity())
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

    @SuppressLint("SetTextI18n")
    private fun setViewModel() {
        dataStoreViewModel.apply {
            getOnboardingState.observe(viewLifecycleOwner) { state ->
                if (!state) {
                    findNavController().navigate(R.id.action_quranFragment_to_onBoardingFragment)
                    binding.root.isVisible = false
                }
            }

            getDetailSurahAyah.observe(viewLifecycleOwner) { data ->
                surahId = data.first
                ayahNumber = data.second
            }

            binding.apply {
                getSurah.observe(viewLifecycleOwner) { data ->
                    val surahNameArabic = data.first
                    val surahName = data.second
                    if (surahName != "" || surahNameArabic != "") {
                        btnContinueRead.visibility = View.VISIBLE
                        tvSurahNameArabic.visibility = View.VISIBLE
                        tvSurahNameArabic.text = surahNameArabic
                        tvSurahName.text = "Q.S $surahName ayat $ayahNumber"
                    } else {
                        btnContinueRead.visibility = View.GONE
                        tvSurahNameArabic.visibility = View.GONE
                        tvSurahName.text = resources.getString(R.string.last_read_surah_empty)
                    }

                    surahDesc = data.third
                }

                btnContinueRead.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_quranFragment_to_quranDetailFragment,
                        Bundle().apply {
                            putString(
                                QuranDetailFragment.SURAH_NAME, binding.tvSurahName.text.toString()
                            )
                            putInt(QuranDetailFragment.SURAH_NUMBER, surahId!!)
                            putInt(QuranDetailFragment.AYAH_NUMBER, ayahNumber!!)
                            putString(QuranDetailFragment.SURAH_DESC, surahDesc)
                            putBoolean(QuranDetailFragment.IS_FROM_LAST_READ, true)
                        })
                }
            }
        }

        quranViewModel.apply {
            isCollapse.observe(viewLifecycleOwner) { yes ->
                if (yes) {
                    binding.appBar.setExpanded(false, false)
                    binding.fabBackToTop.show()
                }
            }

            getListQuran.observe(viewLifecycleOwner) { response ->
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
                                        if (response.data.isNullOrEmpty()) {
                                            InternetReceiver().onReceive(
                                                requireActivity(), Intent()
                                            )
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

                    etSurah.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {}

                            override fun beforeTextChanged(
                                s: CharSequence?, start: Int, count: Int, after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?, start: Int, before: Int, count: Int
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

                    when {
                        response is Resource.Loading && response.data.isNullOrEmpty() -> {
                            stateLoading(true)
                            clNoInternet.visibility = View.GONE
                        }

                        response is Resource.Error && response.data.isNullOrEmpty() -> {
                            stateLoading(false)
                            stateNoInternet(ctlHeader, clNoInternet, true)
                            fabBackToTop.hide()
                        }

                        else -> {
                            stateLoading(false)
                            stateNoInternet(ctlHeader, clNoInternet, false)

                            val listToSet = when {
                                searchQuery.isNotEmpty() && filteredData.isNotEmpty() -> filteredData
                                searchQuery.isNotEmpty() && filteredData.isEmpty() -> ArrayList()
                                else -> response.data!!
                            }
                            quranAdapter.setList(listToSet)

                            if (searchQuery.isNotEmpty() && filteredData.isEmpty()) {
                                emptyState.root.visibility = View.VISIBLE
                            } else {
                                emptyState.root.visibility = View.GONE
                            }

                            rvSurah.visibility = View.VISIBLE
                            bottomNav.visibility = View.VISIBLE

                            tilSurah.setEndIconOnClickListener {
                                quranAdapter.setList(response.data!!)
                                hideKeyboard(requireActivity())
                                etSurah.text?.clear()
                                etSurah.clearFocus()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stateLoading(state: Boolean) {
        binding.apply {
            progressBar.isVisible = state
            progressHeader.isVisible = state
            tvTitle.isVisible = !state
            clSurah.isVisible = !state
        }
    }

    private fun stateNoInternet(
        ctlHeader: CollapsingToolbarLayout,
        clNoInternet: ConstraintLayout,
        isDataEmpty: Boolean,
    ) {
        ctlHeader.isVisible = !isDataEmpty
        clNoInternet.isVisible = isDataEmpty
    }

    private fun isAppBarCollapse(): Boolean {
        return binding.appBar.height + binding.appBar.y == binding.toolbar.height.toFloat()
    }

    override fun onPause() {
        super.onPause()

        quranViewModel.apply {
            setCollapseAppbar(isAppBarCollapse())
            searchQuery = binding.etSurah.text.toString()
            filteredData = quranAdapter.getList()
        }
    }
}