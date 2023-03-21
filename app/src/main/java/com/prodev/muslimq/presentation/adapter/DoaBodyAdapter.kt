package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.DoaBodyEntity
import com.prodev.muslimq.databinding.ItemListDoaBodyBinding
import java.util.ArrayList

class DoaBodyAdapter : RecyclerView.Adapter<DoaBodyAdapter.DoaBodyViewHolder>() {
    private var listDoa = ArrayList<DoaBodyEntity>()
    private var listDoaFilter = ArrayList<DoaBodyEntity>()

    fun setDoa(doa: List<DoaBodyEntity>) {
        listDoa = doa as ArrayList<DoaBodyEntity>
        listDoaFilter = listDoa
        notifyDataSetChanged()
    }

    inner class DoaBodyViewHolder(binding: ItemListDoaBodyBinding) : RecyclerView.ViewHolder(binding.root) {
        var doaArabic = binding.tvDoaArabic
        var doaLatin = binding.tvDoaLatin
        var doaTranslation = binding.tvDoaMeaning
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoaBodyViewHolder {
        val binding = ItemListDoaBodyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoaBodyViewHolder(binding)
    }

    override fun getItemCount(): Int = listDoaFilter.size

    override fun onBindViewHolder(holder: DoaBodyViewHolder, position: Int) {
        val doa = listDoaFilter[position]
        holder.apply {
            doaArabic.text = doa.arab
            doaLatin.text = doa.latin
            doaTranslation.text = doa.translate
        }
    }
}