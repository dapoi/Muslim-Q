package com.prodev.muslimq.utils

import com.prodev.muslimq.R

object OthersObject {

    private val menuImage = intArrayOf(
        R.drawable.ic_collection,
        R.drawable.ic_support,
        R.drawable.ic_ads,
        R.drawable.ic_about
    )

    private val menuTitle = arrayOf(
        "Tersimpan",
        "Kirim Masukan",
        "Dukung Kami",
        "Tentang"
    )

    val listData: ArrayList<Others>
        get() {
            val list = arrayListOf<Others>()
            for (position in menuTitle.indices) {
                val others = Others()
                others.title = menuTitle[position]
                others.image = menuImage[position]
                list.add(others)
            }
            return list
        }
}

data class Others(
    var title: String? = null,
    var image: Int = 0
)