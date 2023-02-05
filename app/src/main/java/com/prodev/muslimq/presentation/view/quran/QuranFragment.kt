package com.prodev.muslimq.presentation.view.quran

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.data.source.local.model.QuranEntity
import com.prodev.muslimq.databinding.FragmentQuranBinding
import com.prodev.muslimq.presentation.adapter.QuranAdapter
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import com.prodev.muslimq.utils.InternetReceiver
import com.prodev.muslimq.utils.Resource
import com.prodev.muslimq.utils.hideKeyboard
import com.prodev.muslimq.utils.isOnline
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuranFragment : Fragment() {

    private lateinit var binding: FragmentQuranBinding

    private val quranAdapter = QuranAdapter()
    private val quranViewModel: QuranViewModel by viewModels()
    private val dataStorePreference: DataStoreViewModel by viewModels()

    private var mRootView: ViewGroup? = null
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
//        binding = FragmentQuranBinding.inflate(inflater, container, false)
//        if (mRootView == null) {
//            mRootView = binding.root
//            isFirstLoad = true
//        } else {
//            isFirstLoad = false
//        }
//        return mRootView as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quranAdapter.setOnItemClick(object : QuranAdapter.OnItemClickCallback {
            override fun onItemClick(surah: QuranEntity) {
                findNavController().navigate(R.id.action_quranFragment_to_quranDetailFragment,
                    Bundle().apply {
                        putInt("surahNumber", surah.nomor)
                        putString("surahName", surah.namaLatin)
                        putString("surahDesc", surah.deskripsi)
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


        binding.apply {
            getLastReadSurah(tvSurahName, tvSurahMeaning)

            fabBackToTop.setOnClickListener {
                rvSurah.smoothScrollToPosition(0)
            }

            svSurah.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard(requireActivity())
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { quranAdapter.filter.filter(it) }
                    return false
                }
            })
        }

        if (isFirstLoad) {
            setViewModel()
            setAdapter()
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

    private fun setViewModel() {
        quranViewModel.getSurah.observe(viewLifecycleOwner) {
            with(binding) {
                srlSurah.apply {
                    setLottieAnimation("loading.json")
                    setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
                    setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
                    setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
                        override fun onRefresh() {
                            val handlerData = Handler(Looper.getMainLooper())
                            val check = isOnline(requireContext())
                            if (check) {
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
            }

            when {
                it is Resource.Loading && it.data.isNullOrEmpty() -> {
                    stateLoading(true)
                }
                it is Resource.Error && it.data.isNullOrEmpty() -> {
                    stateLoading(false)
                    binding.clNoInternet.visibility = View.VISIBLE
                    Log.e("Quran Fragment", it.error?.localizedMessage.toString())
                }
                else -> {
                    stateLoading(false)
                    quranAdapter.setList(it.data!!)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {
        binding.rvSurah.apply {
            layoutManager = LinearLayoutManager(context)
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

    private fun stateLoading(state: Boolean) {
        binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }
}