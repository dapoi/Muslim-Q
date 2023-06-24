package com.prodev.muslimq.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
                lottieAnimationView.setAnimation(onBoarding.image)

                tvOnBoardingTitle.text = onBoarding.title
                tvOnBoardingDescription.text = onBoarding.description
            }
        }
    }

    data class OnBoardingItem(
        val image: Int,
        val title: String,
        val description: String
    )
}