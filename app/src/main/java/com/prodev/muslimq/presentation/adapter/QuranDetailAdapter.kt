package com.prodev.muslimq.presentation.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.Ayat
import com.prodev.muslimq.databinding.ItemListAyahBinding

class QuranDetailAdapter(
    private val context: Context,
    private val taggingQuran: (Ayat) -> Unit,
) : RecyclerView.Adapter<QuranDetailAdapter.DetailViewHolder>() {

    private var ayahs = ArrayList<Ayat>()
    private var textSize: Int = 24
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

        val ivMore = holder.binding.ivMore
        ivMore.setOnClickListener {
            PopupMenu(context, ivMore).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setForceShowIcon(true)
                }
                menuInflater.inflate(R.menu.menu_tagging, menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_tagging -> {
                            AlertDialog.Builder(context)
                                .setTitle("Tandai ayat?")
                                .setMessage("Apakah Anda ingin menandai ayat ini sebagai ayat yang terakhir dibaca?")
                                .setPositiveButton("Ya") { dialog, _ ->
                                    taggingQuran(ayahs[position])
                                    Toast.makeText(
                                        context,
                                        "Ayat ${ayahs[position].ayatId} ditandai",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Tidak") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }

        val cvAyah = holder.binding.cvAyah
        if (isTagging && ayahPosition == position) {
            cvAyah.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.anim_tagging
                )
            )
            isTagging = false
        }
    }

    inner class DetailViewHolder(val binding: ItemListAyahBinding) :
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