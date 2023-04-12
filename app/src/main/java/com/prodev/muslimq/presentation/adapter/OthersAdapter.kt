package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.presentation.view.others.Others
import com.prodev.muslimq.databinding.ItemListOtherBinding

class OthersAdapter : RecyclerView.Adapter<OthersAdapter.OthersViewHolder>() {

    var onClick: ((Int) -> Unit)? = null

    private var listOthers = ArrayList<Others>()

    fun setList(list: List<Others>) {
        listOthers.clear()
        listOthers.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OthersViewHolder {
        return OthersViewHolder(
            ItemListOtherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OthersViewHolder, position: Int) {
        holder.bind(listOthers[position])
    }

    override fun getItemCount(): Int = listOthers.size

    inner class OthersViewHolder(
        private val binding: ItemListOtherBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(others: Others) {
            with(binding) {
                tvMenuOther.text = others.title
                ivMenuOther.setImageResource(others.image)

                vDivider.visibility = if (adapterPosition == listOthers.size - 1) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            itemView.setOnClickListener {
                onClick?.invoke(adapterPosition)
            }
        }
    }
}