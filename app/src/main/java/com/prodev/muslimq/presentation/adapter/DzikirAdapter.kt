package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.prodev.muslimq.core.utils.DzikirType
import com.prodev.muslimq.databinding.ItemListDzikirBinding

class DzikirAdapter(
    private val deleteListener: ((TasbihEntity) -> Unit)? = null
) : ListAdapter<TasbihEntity, DzikirAdapter.DzikirViewHolder>(DzikirAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DzikirViewHolder {
        return DzikirViewHolder(
            ItemListDzikirBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DzikirViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DzikirViewHolder(
        private val binding: ItemListDzikirBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dzikir: TasbihEntity) {
            binding.apply {
                tvDzikir.text = dzikir.dzikirName
                ivDelete.isVisible = dzikir.dzikirType == DzikirType.CUSTOM
            }
        }

        init {
            binding.ivDelete.setOnClickListener {
                deleteListener?.invoke(getItem(adapterPosition))
            }
        }
    }

    companion object : DiffUtil.ItemCallback<TasbihEntity>() {
        override fun areItemsTheSame(oldItem: TasbihEntity, newItem: TasbihEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TasbihEntity, newItem: TasbihEntity): Boolean {
            return oldItem == newItem
        }
    }
}