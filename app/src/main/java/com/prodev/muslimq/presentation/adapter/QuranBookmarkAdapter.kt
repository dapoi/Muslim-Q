package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.databinding.ItemListSurahBinding

class QuranBookmarkAdapter : RecyclerView.Adapter<QuranBookmarkAdapter.QuranBookmarkViewHolder>() {

    private var surahList: ArrayList<QuranDetailEntity> = ArrayList()

    fun setList(list: List<QuranDetailEntity>) {
        surahList.clear()
        surahList.addAll(list)
        notifyDataSetChanged()
    }

    var onItemClick: ((QuranDetailEntity) -> Unit)? = null

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
        holder.bind(surahList[position])
    }

    override fun getItemCount(): Int = surahList.size

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

                vDivider.visibility = if (adapterPosition == surahList.size - 1) {
                    ViewGroup.GONE
                } else {
                    ViewGroup.VISIBLE
                }
            }

            itemView.setOnClickListener {
                onItemClick?.invoke(surah)
            }
        }
    }
}