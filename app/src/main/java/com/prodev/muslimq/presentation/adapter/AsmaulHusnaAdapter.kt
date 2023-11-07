package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.AsmaulHusnaEntity
import com.prodev.muslimq.databinding.ItemListAsmaulHusnaBinding
import com.prodev.muslimq.presentation.adapter.AsmaulHusnaAdapter.AsmaulHusnaViewHolder

class AsmaulHusnaAdapter :
    ListAdapter<AsmaulHusnaEntity, AsmaulHusnaViewHolder>(AsmaulHusnaAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsmaulHusnaViewHolder {
        return AsmaulHusnaViewHolder(
            ItemListAsmaulHusnaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AsmaulHusnaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AsmaulHusnaViewHolder(
        private val binding: ItemListAsmaulHusnaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AsmaulHusnaEntity) {
            binding.apply {
                tvAsmaNumber.text = data.urutan.toString()
                tvAsmaNameArabic.text = data.arab
                tvAsmaNameLatin.text = data.latin
                tvAsmaNameMeaning.text = data.arti
            }
        }
    }

    companion object : DiffUtil.ItemCallback<AsmaulHusnaEntity>() {
        override fun areItemsTheSame(
            oldItem: AsmaulHusnaEntity, newItem: AsmaulHusnaEntity
        ): Boolean = oldItem.urutan == newItem.urutan

        override fun areContentsTheSame(
            oldItem: AsmaulHusnaEntity, newItem: AsmaulHusnaEntity
        ): Boolean = oldItem == newItem
    }
}