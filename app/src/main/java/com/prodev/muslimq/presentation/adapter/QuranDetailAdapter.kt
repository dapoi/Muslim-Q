package com.prodev.muslimq.presentation.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.databinding.ItemListAyahBinding

class QuranDetailAdapter(
    private val context: Context,
    private val surahName: String,
    private val taggingQuran: (Ayat) -> Unit,
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

    fun setFontSize(textSize: Int) {
        this.textSize = textSize
        notifyDataSetChanged()
    }

    fun setTagging(isTagging: Boolean, position: Int) {
        this.isTagging = isTagging
        this.ayahPosition = position
        notifyDataSetChanged()
    }

    var tafsirQuran: ((Ayat) -> Unit?)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        return DetailViewHolder(
            ItemListAyahBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = ayahs.size

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.apply {
            val ayah = ayahs[position]
            tvAyahArabic.text = ayah.ayatArab
            tvAyahLatin.text = ayah.ayatLatin
            tvAyahMeaning.text = ayah.ayatTerjemahan
            tvAyahNumber.text = ayah.ayatNumber.toString()
            tvAyahArabic.textSize = textSize.toFloat()

            ivTag.setOnClickListener {
                AlertDialog.Builder(context).setTitle("Tandai ayat?")
                    .setMessage("Apakah Anda ingin menandai ayat ini sebagai ayat yang terakhir dibaca?")
                    .setPositiveButton("Ya") { dialog, _ ->
                        taggingQuran.invoke(ayah)
                        dialog.dismiss()
                        Toast.makeText(
                            context,
                            "Ayat ${ayahs[position].ayatNumber} berhasil ditandai",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }

            ivTafsir.setOnClickListener {
                tafsirQuran?.invoke(ayahs[position])
            }

            ivShare.setOnClickListener {
                shareIntent(ayahs[position])
            }

            if (isTagging && ayahPosition == position) {
                cvAyah.startAnimation(
                    AnimationUtils.loadAnimation(
                        context, R.anim.anim_tagging
                    )
                )
                isTagging = false
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

    inner class DetailViewHolder(val binding: ItemListAyahBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val ivTag = binding.ivTag
        val ivTafsir = binding.ivTafsir
        val ivShare = binding.ivShare
        val cvAyah = binding.cvAyah
        val tvAyahArabic = binding.tvAyahArabic
        val tvAyahLatin = binding.tvAyahLatin
        val tvAyahMeaning = binding.tvAyahMeaning
        val tvAyahNumber = binding.tvAyahNumber
    }
}