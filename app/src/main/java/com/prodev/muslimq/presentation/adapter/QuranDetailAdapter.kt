package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.data.source.local.model.Ayat
import com.prodev.muslimq.databinding.ItemListAyahBinding

class QuranDetailAdapter : RecyclerView.Adapter<QuranDetailAdapter.DetailViewHolder>() {

    private var ayahs = ArrayList<Ayat>()
    private var textSize: Int = 24

    fun setList(ayahs: List<Ayat>) {
        this.ayahs.clear()
        this.ayahs.addAll(ayahs)
        notifyDataSetChanged()
    }

    fun setFontSize(textSize: Int) {
        this.textSize = textSize
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        return DetailViewHolder(
            ItemListAyahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = ayahs.size

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(ayahs[position])
    }

    inner class DetailViewHolder(private val binding: ItemListAyahBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ayah: Ayat) {
            with(binding) {
                tvAyahArabic.text = ayah.ayatArab
                tvAyahLatin.text = ayah.ayatLatin
                tvAyahMeaning.text = ayah.ayatTerjemahan
                tvAyahNumber.text = ayah.ayatId.toString()

                tvAyahArabic.textSize = textSize.toFloat()
            }
        }
    }

}