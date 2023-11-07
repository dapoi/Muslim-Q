package com.prodev.muslimq.presentation.view.quran

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.core.utils.Resource
import com.prodev.muslimq.databinding.FragmentQuranBinding
import com.prodev.muslimq.helper.InternetReceiver
import com.prodev.muslimq.helper.hideKeyboard
import com.prodev.muslimq.helper.swipeRefresh
import com.prodev.muslimq.presentation.adapter.QuranAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranFragment : BaseFragment<FragmentQuranBinding>(FragmentQuranBinding::inflate) {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var quranAdapter: QuranAdapter

    private val quranViewModel: QuranViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNav = requireActivity().findViewById(R.id.bottom_nav)

        binding.apply {
            fabBackToTop.setOnClickListener {
                // scroll to parent
                rvSurah.scrollToPosition(0)
                appBar.setExpanded(true, true)
            }
        }

        swipeRefresh(
            { InternetReceiver().onReceive(requireActivity(), Intent()) }, binding.srlSurah
        )
        initAdapter()
        initViewModel()
    }

    private fun initAdapter() {
        quranAdapter = QuranAdapter(binding.emptyState.root)
        quranAdapter.setOnItemClick(object : QuranAdapter.OnItemClickCallback {
            override fun onItemClick(surah: QuranEntity) {
                findNavController().navigate(
                    QuranFragmentDirections.actionQuranFragmentToQuranDetailFragment(surah.nomor)
                )
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
    private fun initViewModel() {
        dataStoreViewModel.apply {
            getOnboardingState.observe(viewLifecycleOwner) { state ->
                if (!state) {
                    findNavController().navigate(QuranFragmentDirections.actionQuranFragmentToOnBoardingFragment())
                    binding.root.isVisible = false
                }
            }

            mergeData.observe(viewLifecycleOwner) { data ->
                val (surahId, ayahNumber) = data.surah
                val (surahNameArabic, surahName) = data.detailSurah
                binding.apply {
                    btnContinueRead.setOnClickListener {
                        findNavController().navigate(
                            QuranFragmentDirections.actionQuranFragmentToQuranDetailFragment(
                                surahId, true, ayahNumber
                            )
                        )
                    }

                    val dataNotEmpty = surahName.isNotEmpty() || surahNameArabic.isNotEmpty()
                    btnContinueRead.isVisible = dataNotEmpty
                    tvSurahNameArabic.isVisible = dataNotEmpty
                    tvSurahNameArabic.text = surahNameArabic
                    tvSurahName.text = if (dataNotEmpty) {
                        "Q.S $surahName ayat $ayahNumber"
                    } else {
                        resources.getString(R.string.last_read_surah_empty)
                    }
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

                    val isLoading = response is Resource.Loading && response.data.isNullOrEmpty()
                    val isError = response is Resource.Error && response.data.isNullOrEmpty()
                    val isSuccess = response is Resource.Success
                    progressBar.isVisible = isLoading
                    progressHeader.isVisible = isLoading
                    tvTitle.isVisible = isSuccess
                    clSurah.isVisible = isSuccess
                    ctlHeader.isVisible = isSuccess
                    clNoInternet.isVisible = isError

                    if (isSuccess) {
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
                    } else {
                        fabBackToTop.hide()
                    }
                }
            }
        }
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