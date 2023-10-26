package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.helper.capitalizeEachWord
import com.prodev.muslimq.databinding.ItemListAreaBinding

class CityAdapter(
    private val emptyState: LinearLayout
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>(), Filterable {

    private var listCity = ArrayList<CityResponse>()
    private var listCityFilter = ArrayList<CityResponse>()

    var onCLick: ((CityResponse) -> Unit)? = null

    fun setList(list: List<CityResponse>) {
        listCity = list as ArrayList<CityResponse>
        listCityFilter = listCity
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        return CityViewHolder(
            ItemListAreaBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(listCityFilter[position])
    }

    override fun getItemCount(): Int = listCityFilter.size

    inner class CityViewHolder(
        private val binding: ItemListAreaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CityResponse) {
            binding.tvAreaName.text = capitalizeEachWord(item.name)
        }

        init {
            binding.root.setOnClickListener {
                onCLick?.invoke(listCityFilter[adapterPosition])
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                listCityFilter = if (charSearch.isEmpty()) {
                    listCity
                } else {
                    val resultList = ArrayList<CityResponse>()
                    for (row in listCity) {
                        if (row.name.lowercase().contains(charSearch.lowercase())) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = listCityFilter
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listCityFilter = results?.values as ArrayList<CityResponse>
                emptyState.visibility = if (listCityFilter.isEmpty()) View.VISIBLE else View.GONE
                notifyDataSetChanged()
            }
        }
    }
}