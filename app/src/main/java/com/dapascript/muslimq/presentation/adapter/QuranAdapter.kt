package com.dapascript.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dapascript.muslimq.data.source.local.model.QuranEntity
import com.dapascript.muslimq.databinding.ItemListSurahBinding

class QuranAdapter : RecyclerView.Adapter<QuranAdapter.QuranViewHolder>(), Filterable {

    private var surahList: ArrayList<QuranEntity> = ArrayList()
    private var surahListFiltered: ArrayList<QuranEntity> = ArrayList()

    private lateinit var onItemClickCallback: OnItemClickCallback

    internal fun setOnItemClick(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClick(surah: QuranEntity)
    }

    fun setList(list: List<QuranEntity>) {
        val diffResult = DiffUtil.calculateDiff(QuranDiffCallback(surahList, list))
        surahList.clear()
        surahList.addAll(list)
        surahListFiltered.clear()
        surahListFiltered.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = surahListFiltered.size

    override fun onBindViewHolder(holder: QuranViewHolder, position: Int) =
        holder.bind(surahListFiltered[position])

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

    inner class QuranDiffCallback(
        private val oldList: List<QuranEntity>,
        private val newList: List<QuranEntity>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].nomor == newList[newItemPosition].nomor

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                surahListFiltered = if (charString.isEmpty()) {
                    surahList
                } else {
                    val filteredList = ArrayList<QuranEntity>()
                    surahList.filter {
                        (it.namaLatin.lowercase().contains(charString.lowercase()))
                    }.forEach { filteredList.add(it) }
                    filteredList
                }
                return FilterResults().apply {
                    values = surahListFiltered
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraints: CharSequence?, result: FilterResults?) {
                surahListFiltered = if (result?.values == null) {
                    ArrayList()
                } else {
                    result.values as ArrayList<QuranEntity>
                }
                notifyDataSetChanged()
            }

        }
    }
}