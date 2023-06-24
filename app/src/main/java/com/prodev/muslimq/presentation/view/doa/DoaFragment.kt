package com.prodev.muslimq.presentation.view.doa

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.core.utils.hideKeyboard
import com.prodev.muslimq.databinding.FragmentDoaBinding
import com.prodev.muslimq.presentation.adapter.DoaAdapter
import com.prodev.muslimq.presentation.view.BaseFragment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DoaFragment : BaseFragment<FragmentDoaBinding>(FragmentDoaBinding::inflate) {

    private lateinit var doaAdapter: DoaAdapter

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.svDoa.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard(requireActivity())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                doaAdapter.filter.filter(newText)
                return true
            }
        })

        setAdapter()
    }

    private fun setAdapter() {
        doaAdapter = DoaAdapter(binding.emptyState.root)
        binding.rvDoa.apply {
            adapter = doaAdapter
            layoutManager = LinearLayoutManager(context)
            (binding.rvDoa.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            setHasFixedSize(true)
        }

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val doaJson = firebaseRemoteConfig.getString("doa")
        val doaJsonAdapter: JsonAdapter<List<DoaEntity>> = moshi.adapter(
            Types.newParameterizedType(
                List::class.java,
                DoaEntity::class.java
            )
        )
        val doaList = doaJsonAdapter.fromJson(doaJson)
        doaList?.let { doaAdapter.setDoa(it) }
    }
}