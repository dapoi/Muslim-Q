package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.BookmarkEntity
import com.prodev.muslimq.databinding.ItemListSurahBinding

class QuranBookmarkAdapter(
    private val onItemClick: ((BookmarkEntity) -> Unit)? = null
) : ListAdapter<BookmarkEntity, QuranBookmarkAdapter.QuranBookmarkViewHolder>(DIFF_CALLBACK) {

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
        fun bind(surah: BookmarkEntity) {
            binding.apply {
                tvSurahNumber.text = surah.surahId.toString()
                tvSurahName.text = surah.namaLatin
                tvMeaningAndAyah.text = "${surah.artiQuran} • ${surah.jumlahAyat} Ayat"
                tvSurahNameArabic.text = surah.nama

                vDivider.isVisible = adapterPosition != itemCount - 1
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(surah)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookmarkEntity>() {
            override fun areItemsTheSame(
                oldItem: BookmarkEntity,
                newItem: BookmarkEntity
            ): Boolean {
                return oldItem.surahId == newItem.surahId
            }

            override fun areContentsTheSame(
                oldItem: BookmarkEntity,
                newItem: BookmarkEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}