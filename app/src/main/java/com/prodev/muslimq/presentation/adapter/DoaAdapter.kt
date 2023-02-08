package com.prodev.muslimq.presentation.adapter

import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.databinding.ItemListDoaBinding
import java.util.*

class DoaAdapter : RecyclerView.Adapter<DoaAdapter.DoaViewHolder>(), Filterable {

    private var listDoa = ArrayList<DoaEntity>()
    private var listDoaFilter = ArrayList<DoaEntity>()

    fun setDoa(doa: List<DoaEntity>) {
        listDoa = doa as ArrayList<DoaEntity>
        listDoaFilter = listDoa
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoaViewHolder {
        val binding = ItemListDoaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoaViewHolder, position: Int) {
        val doa = listDoaFilter[position]
        holder.apply {
            doaName.text = doa.title
            doaArabic.text = doa.arab
            doaLatin.text = doa.latin
            doaTranslation.text = doa.translate

            itemView.setOnClickListener {
                val transition = TransitionInflater.from(itemView.context)
                    .inflateTransition(android.R.transition.fade)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup, transition)
                if (isExpanded) {
                    clDoa.visibility = View.GONE
                    arrowDown.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_down,
                        0
                    )
                    isExpanded = false
                } else {
                    clDoa.visibility = View.VISIBLE
                    arrowDown.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_arrow_up,
                        0
                    )
                    isExpanded = true
                }
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = listDoaFilter.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                listDoaFilter = if (charSearch.isEmpty()) {
                    listDoa
                } else {
                    val resultList = ArrayList<DoaEntity>()
                    for (row in listDoa) {
                        if (row.title.lowercase(Locale.getDefault()).contains(
                                charSearch.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = listDoaFilter
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listDoaFilter = results?.values as ArrayList<DoaEntity>
                notifyDataSetChanged()
            }
        }
    }

    inner class DoaViewHolder(binding: ItemListDoaBinding) : RecyclerView.ViewHolder(binding.root) {
        var doaName = binding.tvDoaName
        var arrowDown = binding.tvArrow
        var clDoa = binding.clDoa
        var doaArabic = binding.tvDoaArabic
        var doaLatin = binding.tvDoaLatin
        var doaTranslation = binding.tvDoaMeaning
        var isExpanded = false
    }
}