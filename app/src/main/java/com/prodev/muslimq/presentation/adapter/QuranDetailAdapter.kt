package com.prodev.muslimq.presentation.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.databinding.ItemListAyahBinding

class QuranDetailAdapter(
    private val context: Context,
    private val surahName: String
) : ListAdapter<Ayat, RecyclerView.ViewHolder>(QuranDetailAdapter) {

    private var getTextSize: Int = 34
    private var getTagging: Boolean = false
    private var ayahPosition: Int = 0

    var taggingClick: ((Ayat) -> Unit?)? = null
    var tafsirClick: ((Ayat) -> Unit?)? = null
    var audioAyahClick: ((Ayat) -> Unit?)? = null

    fun setFontSize(textSize: Int) {
        this.getTextSize = textSize
        notifyItemRangeChanged(0, itemCount)
    }

    fun setTagging(isTagging: Boolean, position: Int) {
        this.getTagging = isTagging
        this.ayahPosition = position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DetailViewHolder(
            ItemListAyahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DetailViewHolder).bind(getItem(position))
    }

    inner class DetailViewHolder(val binding: ItemListAyahBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ayat: Ayat) {
            with(binding) {
                tvAyahArabic.text = ayat.ayatArab
                tvAyahLatin.text = ayat.ayatLatin
                tvAyahMeaning.text = ayat.ayatTerjemahan
                tvAyahNumber.text = ayat.ayatNumber.toString()
                tvAyahArabic.textSize = getTextSize.toFloat()

                if (getTagging && ayahPosition == adapterPosition) {
                    cvAyah.startAnimation(
                        AnimationUtils.loadAnimation(
                            context, R.anim.anim_tagging
                        )
                    )
                    getTagging = false
                }
            }
        }

        init {
            with(binding) {
                ivTag.setOnClickListener {
                    taggingClick?.invoke(getItem(adapterPosition))
                }

                ivTafsir.setOnClickListener {
                    tafsirClick?.invoke(getItem(adapterPosition))
                }

                ivShare.setOnClickListener {
                    shareIntent(getItem(adapterPosition))
                }

                ivPlayAyah.setOnClickListener {
                    audioAyahClick?.invoke(getItem(adapterPosition))
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
                    "\n\"${ayat.ayatTerjemahan}\" \n\n Diambil dari aplikasi Muslim Q" +
                    "\n https://play.google.com/store/apps/details?id=com.prodev.muslimq"
        )
        context.startActivity(
            Intent.createChooser(
                shareIntent, "Bagikan ayat ini"
            )
        )
    }

    companion object : DiffUtil.ItemCallback<Ayat>() {
        override fun areItemsTheSame(oldItem: Ayat, newItem: Ayat): Boolean {
            return oldItem.ayatNumber == newItem.ayatNumber
        }

        override fun areContentsTheSame(oldItem: Ayat, newItem: Ayat): Boolean {
            return oldItem == newItem
        }
    }
}