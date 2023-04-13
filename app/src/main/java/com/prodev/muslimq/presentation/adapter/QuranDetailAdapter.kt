package com.prodev.muslimq.presentation.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.databinding.ItemListAyahBinding

class QuranDetailAdapter(
    private val context: Context,
    private val surahName: String
) : RecyclerView.Adapter<QuranDetailAdapter.DetailViewHolder>() {

    private var ayahs = ArrayList<Ayat>()

    private var textSize: Int = 26
    private var isTagging: Boolean = false
    private var ayahPosition: Int = 0

    fun setList(ayahs: List<Ayat>) {
        this.ayahs.clear()
        this.ayahs.addAll(ayahs)
        notifyDataSetChanged()
    }

    fun getAyahs(): List<Ayat> = ayahs

    fun setFontSize(textSize: Int) {
        this.textSize = textSize
        notifyDataSetChanged()
    }

    fun setAnimItem(isTagging: Boolean, position: Int) {
        this.isTagging = isTagging
        this.ayahPosition = position
        notifyDataSetChanged()
    }

    var taggingQuran: ((Ayat) -> Unit?)? = null
    var tafsirQuran: ((Ayat) -> Unit?)? = null
    var audioAyah: ((Ayat) -> Unit?)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        return DetailViewHolder(
            ItemListAyahBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = ayahs.size

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(ayahs[position])
    }

    inner class DetailViewHolder(val binding: ItemListAyahBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ayat: Ayat) {
            with(binding) {
                tvAyahArabic.text = ayat.ayatArab
                tvAyahLatin.text = ayat.ayatLatin
                tvAyahMeaning.text = ayat.ayatTerjemahan
                tvAyahNumber.text = ayat.ayatNumber.toString()
                tvAyahArabic.textSize = textSize.toFloat()

                if (isTagging && ayahPosition == adapterPosition) {
                    cvAyah.startAnimation(
                        AnimationUtils.loadAnimation(
                            context, R.anim.anim_tagging
                        )
                    )
                    isTagging = false
                }
            }
        }

        init {
            with(binding) {
                ivTag.setOnClickListener {
                    taggingQuran?.invoke(ayahs[adapterPosition])
                }

                ivTafsir.setOnClickListener {
                    tafsirQuran?.invoke(ayahs[adapterPosition])
                }

                ivShare.setOnClickListener {
                    shareIntent(ayahs[adapterPosition])
                }

                ivPlayAyah.setOnClickListener {
                    audioAyah?.invoke(ayahs[adapterPosition])
                }
            }
        }
    }

    private fun shareIntent(ayat: Ayat) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Q.S. $surahName Ayat ${ayat.ayatNumber} \n\n${ayat.ayatArab} \nArtinya: " +
                    "\n\"${ayat.ayatTerjemahan}\" \n\n Diambil dari aplikasi Muslim Q"
        )
        context.startActivity(
            Intent.createChooser(
                shareIntent, "Bagikan ayat ini"
            )
        )
    }
}