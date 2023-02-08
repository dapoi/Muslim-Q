package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.remote.model.CityResponse
import com.prodev.muslimq.core.utils.capitalizeEachWord
import com.prodev.muslimq.databinding.ItemListAreaBinding

class CityAdapter : RecyclerView.Adapter<CityAdapter.CityViewHolder>(), Filterable {

    private var listCity = ArrayList<CityResponse>()
    private var listCityFiltered = ArrayList<CityResponse>()

    var onCLick: ((CityResponse) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<CityResponse>) {
        listCity = list as ArrayList<CityResponse>
        listCityFiltered = listCity
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
        holder.bind(listCityFiltered[position])
    }

    override fun getItemCount(): Int = listCityFiltered.size

    inner class CityViewHolder(
        private val binding: ItemListAreaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CityResponse) {
            binding.tvAreaName.text = capitalizeEachWord(item.name)
        }

        init {
            binding.root.setOnClickListener {
                onCLick?.invoke(listCityFiltered[adapterPosition])
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                listCityFiltered = if (charSearch.isEmpty()) {
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
                filterResults.values = listCityFiltered
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listCityFiltered = results?.values as ArrayList<CityResponse>
                notifyDataSetChanged()
            }
        }
    }
}