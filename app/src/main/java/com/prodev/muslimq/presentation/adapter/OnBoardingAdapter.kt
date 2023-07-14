package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prodev.muslimq.databinding.ItemOnBoardingBinding

class OnBoardingAdapter : RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder>() {

    private var getOnBoardingItem = ArrayList<OnBoardingItem>()

    fun setOnBoardingItem(onBoardingItem: List<OnBoardingItem>) {
        this.getOnBoardingItem.clear()
        this.getOnBoardingItem.addAll(onBoardingItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingViewHolder {
        val binding = ItemOnBoardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnBoardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {
        val onBoarding = getOnBoardingItem[position]
        holder.bind(onBoarding)
    }

    override fun getItemCount(): Int = getOnBoardingItem.size

    inner class OnBoardingViewHolder(private val binding: ItemOnBoardingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(onBoarding: OnBoardingItem) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(onBoarding.image)
                    .into(ivOnBoarding)

                tvOnBoardingTitle.text = onBoarding.title
                tvOnBoardingDescription.text = onBoarding.description

                // check if device is 720x1280
                if (itemView.context.resources.displayMetrics.heightPixels <= 1280) {
                    // set margin top for onBoarding title
                    val params = tvOnBoardingTitle.layoutParams as ViewGroup.MarginLayoutParams
                    params.setMargins(0, 0, 0, 0)
                    tvOnBoardingTitle.layoutParams = params
                    tvOnBoardingTitle.textSize = 14f
                    tvOnBoardingDescription.textSize = 12f
                }
            }
        }
    }

    data class OnBoardingItem(
        val image: Int,
        val title: String,
        val description: String
    )
}