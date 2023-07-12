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
import com.prodev.muslimq.databinding.ItemLoadingBinding

class QuranDetailAdapter(
    private val context: Context,
    private val surahName: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var ayahs = ArrayList<Ayat>()

    private var textSize: Int = 26
    private var isLoading: Boolean = false
    private var isTagging: Boolean = false
    private var ayahPosition: Int = 0

    var taggingQuran: ((Ayat) -> Unit?)? = null
    var tafsirQuran: ((Ayat) -> Unit?)? = null
    var audioAyah: ((Ayat) -> Unit?)? = null

    fun setList(ayahs: List<Ayat>, clearData: Boolean = false) {
        if (clearData) {
            this.ayahs.clear()
        }
        this.ayahs.addAll(ayahs)
        notifyDataSetChanged()
    }

    fun showLoading() {
        isLoading = true
        notifyItemInserted(ayahs.size)
    }

    fun hideLoading() {
        isLoading = false
        notifyItemRemoved(ayahs.size)
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

    fun getLoading(): Boolean = isLoading

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> DetailViewHolder(
                ItemListAyahBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_LOADING -> LoadingViewHolder(
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (holder.itemViewType) {
            VIEW_TYPE_ITEM -> {
                val ayat = ayahs[position]
                (holder as DetailViewHolder).bind(ayat)
            }

            VIEW_TYPE_LOADING -> {
                (holder as LoadingViewHolder).pbPaging.isIndeterminate = true
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = ayahs.size + if (isLoading) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (isLoading && position == ayahs.size) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    inner class LoadingViewHolder(
        val binding: ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var pbPaging = binding.progressBar
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
                    "\n\"${ayat.ayatTerjemahan}\" \n\n Diambil dari aplikasi Muslim Q" +
                    "\n https://play.google.com/store/apps/details?id=com.prodev.muslimq"
        )
        context.startActivity(
            Intent.createChooser(
                shareIntent, "Bagikan ayat ini"
            )
        )
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }
}