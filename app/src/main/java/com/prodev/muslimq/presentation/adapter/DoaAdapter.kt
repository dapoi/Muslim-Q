package com.prodev.muslimq.presentation.adapter

import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.source.local.model.DoaEntity
import com.prodev.muslimq.databinding.ItemListDoaBinding
import java.util.Locale

class DoaAdapter(
    private val emptyState: LinearLayout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var listDoa = ArrayList<DoaEntity>()
    private var listDoaFilter = ArrayList<DoaEntity>()

    fun setDoa(doa: List<DoaEntity>) {
        listDoa.clear()
        listDoa.addAll(doa)
        listDoaFilter = listDoa
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoaViewHolder {
        val binding = ItemListDoaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val doa = listDoaFilter[position]
        (holder as DoaViewHolder).apply {
            doaName.text = doa.title
            doaArabic.text = doa.arabic
            doaLatin.text = doa.latin
            doaTranslation.text = doa.translation

            val isExpanded = doa.isExpanded
            if (isExpanded) {
                clDoa.visibility = View.VISIBLE
                arrowDown.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_up,
                    0
                )
            } else {
                clDoa.visibility = View.GONE
                arrowDown.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_down,
                    0
                )
            }

            itemView.setOnClickListener {
                val transition = TransitionInflater.from(itemView.context)
                    .inflateTransition(android.R.transition.fade)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup, transition)

                isAnyItemExpanded(position)
                doa.isExpanded = !doa.isExpanded
                notifyItemChanged(position)
            }
        }
    }

    private fun isAnyItemExpanded(position: Int) {
        val temp = listDoaFilter.indexOfFirst { it.isExpanded }
        if (temp >= 0 && temp != position) {
            listDoaFilter[temp].isExpanded = false
            notifyItemChanged(temp)
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
                emptyState.visibility = if (listDoaFilter.isEmpty()) View.VISIBLE else View.GONE
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
    }
}