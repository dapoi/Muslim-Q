package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.QuranEntity
import com.prodev.muslimq.databinding.ItemListSurahBinding
import java.util.*

class QuranAdapter(
    private val emptyState: LinearLayout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var listSurah = ArrayList<QuranEntity>()
    private var listSurahFilter = ArrayList<QuranEntity>()

    private lateinit var onItemClickCallback: OnItemClickCallback

    internal fun setOnItemClick(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClick(surah: QuranEntity)
    }

    fun setList(list: List<QuranEntity>) {
        listSurah = list as ArrayList<QuranEntity>
        listSurahFilter = list
        notifyDataSetChanged()
    }

    fun getList(): List<QuranEntity> = listSurahFilter

    override fun getItemCount(): Int = listSurahFilter.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        (holder as QuranViewHolder).bind(listSurahFilter[position])

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

                vDivider.visibility = if (adapterPosition == listSurah.size - 1) {
                    ViewGroup.INVISIBLE
                } else {
                    ViewGroup.VISIBLE
                }

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
                listSurahFilter = if (charSearch.isEmpty()) {
                    listSurah
                } else {
                    val resultList = ArrayList<QuranEntity>()
                    for (row in listSurah) {
                        if (row.namaLatin.lowercase(Locale.getDefault())
                                .contains(charSearch.lowercase(Locale.getDefault()))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = listSurahFilter
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listSurahFilter = results?.values as ArrayList<QuranEntity>
                emptyState.visibility = if (listSurahFilter.isEmpty()) View.VISIBLE else View.GONE
                notifyDataSetChanged()
            }
        }
    }
}