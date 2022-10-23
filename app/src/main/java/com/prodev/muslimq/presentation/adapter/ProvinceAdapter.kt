package com.prodev.muslimq.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.databinding.ItemListAreaBinding
import com.prodev.muslimq.utils.capitalizeEachWord

class ProvinceAdapter : RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder>(), Filterable {

    private var listProvince = ArrayList<ProvinceResponse>()
    private var listProvinceFiltered = ArrayList<ProvinceResponse>()

    var onClick: ((ProvinceResponse) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setList(province: List<ProvinceResponse>) {
        listProvince.clear()
        listProvince.addAll(province)
        listProvinceFiltered.clear()
        listProvinceFiltered.addAll(province)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinceViewHolder {
        return ProvinceViewHolder(
            ItemListAreaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProvinceViewHolder, position: Int) {
        holder.bind(listProvinceFiltered[position])
    }

    override fun getItemCount(): Int = listProvinceFiltered.size

    inner class ProvinceViewHolder(
        private val binding: ItemListAreaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ProvinceResponse) {
            binding.tvAreaName.text = when (data.name) {
                "DKI JAKARTA" -> {
                    "DKI Jakarta"
                }
                "DI YOGYAKARTA" -> {
                    "DI Yogyakarta"
                }
                else -> {
                    capitalizeEachWord(data.name)
                }
            }
        }

        init {
            binding.root.setOnClickListener {
                onClick?.invoke(listProvinceFiltered[adapterPosition])
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                listProvinceFiltered = if (charSearch.isEmpty()) {
                    listProvince
                } else {
                    val resultList = ArrayList<ProvinceResponse>()
                    for (row in listProvince) {
                        if (row.name.lowercase().contains(charSearch.lowercase())) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = listProvinceFiltered
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listProvinceFiltered = results?.values as ArrayList<ProvinceResponse>
                notifyDataSetChanged()
            }
        }
    }
}