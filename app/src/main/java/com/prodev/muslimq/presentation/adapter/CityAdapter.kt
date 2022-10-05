package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.data.source.remote.model.CityResponse
import com.prodev.muslimq.databinding.ItemListCityBinding
import java.util.*

class CityAdapter(private var listCity: ArrayList<CityResponse>) :
    RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    fun setList(list: List<CityResponse>) {
        val diffCallback = CityDiffCallback(this.listCity, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listCity.clear()
        this.listCity.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        return CityViewHolder(
            ItemListCityBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(listCity[position])
    }

    override fun getItemCount(): Int = listCity.size

    inner class CityViewHolder(
        private val binding: ItemListCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CityResponse) {
            binding.tvCityName.text = capitalizeEachWord(item.lokasi)
        }
    }

    private fun capitalizeEachWord(str: String): String {
        val words = str.split(" ")
        val capitalizeWordList: ArrayList<String> = ArrayList()
        for (word in words) {
            val first = word.substring(0, 1)
            val afterFirst = word.substring(1)
            capitalizeWordList.add(
                first.uppercase(Locale.getDefault()) + afterFirst.lowercase(Locale.getDefault())
            )
        }
        return capitalizeWordList.joinToString(" ")
    }

    inner class CityDiffCallback(
        private val newList: List<CityResponse>,
        private val oldList: List<CityResponse>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}