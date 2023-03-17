package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.databinding.ItemListSurahBinding
import java.util.*

class QuranAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var surahList = ArrayList<QuranEntity>()
    private var surahListFiltered = ArrayList<QuranEntity>()

    private lateinit var onItemClickCallback: OnItemClickCallback

    internal fun setOnItemClick(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClick(surah: QuranEntity)
    }

    fun setList(list: List<QuranEntity>) {
        surahList = list as ArrayList<QuranEntity>
        surahListFiltered = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = surahListFiltered.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        (holder as QuranViewHolder).bind(surahListFiltered[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        return QuranViewHolder(
            ItemListSurahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class QuranViewHolder(
        private val binding: ItemListSurahBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(surah: QuranEntity) {
            with(binding) {
                tvSurahNumber.text = surah.nomor.toString()
                tvSurahName.text = surah.namaLatin
                tvMeaningAndAyah.text = "${surah.arti} • ${surah.jumlahAyat} Ayat"
                tvSurahNameArabic.text = surah.nama

                vDivider.visibility =
                    if (adapterPosition == surahList.size - 1) ViewGroup.GONE else ViewGroup.VISIBLE

                root.setOnClickListener {
                    onItemClickCallback.onItemClick(surah)
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                surahListFiltered = if (charSearch.isEmpty()) {
                    surahList
                } else {
                    val resultList = ArrayList<QuranEntity>()
                    for (row in surahList) {
                        if (row.namaLatin.lowercase(Locale.getDefault())
                                .contains(charSearch.lowercase(Locale.getDefault()))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = surahListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                surahListFiltered = results?.values as ArrayList<QuranEntity>
                notifyDataSetChanged()
            }
        }
    }
}