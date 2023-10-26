package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.remote.model.ProvinceResponse
import com.prodev.muslimq.helper.capitalizeEachWord
import com.prodev.muslimq.databinding.ItemListAreaBinding

class ProvinceAdapter(
    private val emptyState: LinearLayout
) : RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder>(), Filterable {

    private var listProvince = ArrayList<ProvinceResponse>()
    private var listProvinceFilter = ArrayList<ProvinceResponse>()

    var onClick: ((ProvinceResponse) -> Unit)? = null

    fun setList(province: List<ProvinceResponse>) {
        listProvince = province as ArrayList<ProvinceResponse>
        listProvinceFilter = listProvince
        notifyDataSetChanged()
    }

    fun getList(): List<ProvinceResponse> = listProvinceFilter

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
        holder.bind(listProvinceFilter[position])
    }

    override fun getItemCount(): Int = listProvinceFilter.size

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
                onClick?.invoke(listProvinceFilter[adapterPosition])
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                listProvinceFilter = if (charSearch.isEmpty()) {
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
                filterResults.values = listProvinceFilter
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listProvinceFilter = results?.values as ArrayList<ProvinceResponse>
                emptyState.visibility =
                    if (listProvinceFilter.isEmpty()) View.VISIBLE else View.GONE
                notifyDataSetChanged()
            }
        }
    }
}