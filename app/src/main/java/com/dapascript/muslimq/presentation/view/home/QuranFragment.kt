package com.dapascript.muslimq.presentation.view.home

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dapascript.muslimq.data.source.local.model.QuranEntity
import com.dapascript.muslimq.databinding.FragmentQuranBinding
import com.dapascript.muslimq.presentation.adapter.QuranAdapter
import com.dapascript.muslimq.presentation.viewmodel.QuranViewModel
import com.dapascript.muslimq.utils.InternetReceiver
import com.dapascript.muslimq.utils.Resource
import com.dapascript.muslimq.utils.hideKeyboard
import com.dapascript.muslimq.utils.isOnline
import com.simform.refresh.SSPullToRefreshLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranFragment : Fragment() {

    private lateinit var binding: FragmentQuranBinding
    private lateinit var quranAdapter: QuranAdapter

    private val quranViewModel: QuranViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
        } else {
            binding = FragmentQuranBinding.inflate(inflater, container, false)
            binding.apply {
                fabBackToTop.setOnClickListener {
                    rvSurah.smoothScrollToPosition(0)
                }
            }

            setAdapter()
            setViewModel()

            quranAdapter.setOnItemClick(object : QuranAdapter.OnItemClickCallback {
                override fun onItemClick(surah: QuranEntity) {
                    hideKeyboard(requireActivity())
                    deleteLastReadSurah(requireActivity())
                    saveLastReadSurah(requireActivity(), surah)
                    getLastReadSurah(requireActivity())
                }
            })

            getLastReadSurah(requireActivity())

            binding.svSurah.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        return binding.root
    }

    private fun deleteLastReadSurah(requireActivity: FragmentActivity) {
        val sharedPref = requireActivity.getSharedPreferences("Quran", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("name")
        editor.remove("meaning")
        editor.apply()
    }

    private fun saveLastReadSurah(requireActivity: FragmentActivity, data: QuranEntity) {
        val sharedPref = requireActivity.getSharedPreferences("Quran", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("name", data.namaLatin)
        editor.putString("meaning", data.arti)
        editor.apply()
    }

    private fun getLastReadSurah(requireActivity: FragmentActivity) {
        val sharedPref = requireActivity.getSharedPreferences("Quran", MODE_PRIVATE)
        val name = sharedPref.getString("name", "")
        val meaning = sharedPref.getString("meaning", "")
        binding.tvSurahName.text = name
        binding.tvSurahMeaning.text = meaning
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

            if (it is Resource.Loading && it.data.isNullOrEmpty()) {
                stateLoading(true)
            } else if (it is Resource.Error && it.data.isNullOrEmpty()) {
                stateLoading(false)
                binding.clNoInternet.visibility = View.VISIBLE
                Log.e("TAG", it.error?.localizedMessage.toString())
            } else {
                stateLoading(false)
                quranAdapter.setList(it.data!!)
            }
        }
    }

    private fun setAdapter() {
        quranAdapter = QuranAdapter()
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