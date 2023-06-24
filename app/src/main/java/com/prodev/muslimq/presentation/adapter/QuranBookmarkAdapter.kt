package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.databinding.ItemListSurahBinding

class QuranBookmarkAdapter(
    private val onItemClick: ((QuranDetailEntity) -> Unit)? = null
) : ListAdapter<QuranDetailEntity, QuranBookmarkAdapter.QuranBookmarkViewHolder>(DIFF_CALLBACK) {

    fun getSurahAt(position: Int): QuranDetailEntity = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranBookmarkViewHolder {
        return QuranBookmarkViewHolder(
            ItemListSurahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QuranBookmarkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuranBookmarkViewHolder(
        private val binding: ItemListSurahBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(surah: QuranDetailEntity) {
            binding.apply {
                tvSurahNumber.text = surah.surahId.toString()
                tvSurahName.text = surah.namaLatin
                tvMeaningAndAyah.text = "${surah.artiQuran} • ${surah.jumlahAyat} Ayat"
                tvSurahNameArabic.text = surah.nama

                vDivider.visibility = if (adapterPosition == itemCount - 1) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(surah)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuranDetailEntity>() {
            override fun areItemsTheSame(
                oldItem: QuranDetailEntity,
                newItem: QuranDetailEntity
            ): Boolean {
                return oldItem.surahId == newItem.surahId
            }

            override fun areContentsTheSame(
                oldItem: QuranDetailEntity,
                newItem: QuranDetailEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}