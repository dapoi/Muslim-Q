package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.utils.UITheme
import com.prodev.muslimq.databinding.ItemListOtherBinding
import com.prodev.muslimq.presentation.view.others.Others

class OthersAdapter : RecyclerView.Adapter<OthersAdapter.OthersViewHolder>() {

    var onClick: ((Others) -> Unit)? = null
    var onSwitch: ((Boolean) -> Unit)? = null

    private var listOthers = ArrayList<Others>()
    private var switchState = false

    fun setList(list: List<Others>) {
        listOthers.clear()
        listOthers.addAll(list)
        notifyDataSetChanged()
    }

    fun setSwitchState(uiTheme: UITheme) {
        switchState = uiTheme == UITheme.DARK
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

                vDivider.isVisible = adapterPosition != listOthers.size - 1

                swDarkMode.apply {

                    // Set visibility of switch
                    swDarkMode.visibility = if (others.title.contains("Gelap")) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }

                    // Set switch state
                    isChecked = switchState

                    // Set switch listener
                    setOnCheckedChangeListener { _, isChecked ->
                        onSwitch?.invoke(isChecked)
                    }
                }
            }

            itemView.setOnClickListener {
                onClick?.invoke(listOthers[adapterPosition])
            }
        }
    }
}