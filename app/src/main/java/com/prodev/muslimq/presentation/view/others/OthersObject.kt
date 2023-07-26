package com.prodev.muslimq.presentation.view.others

import com.prodev.muslimq.R

object OthersObject {

    private val menuImage = intArrayOf(
        R.drawable.ic_collection,
        R.drawable.ic_dark_mode,
        R.drawable.ic_notif_setting,
        R.drawable.ic_support,
        R.drawable.ic_about
    )

    private val menuTitle = listOf(
        "Baca Nanti",
        "Mode Gelap",
        "Pengaturan Notifikasi",
        "Kirim Masukan",
        "Info Aplikasi"
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
    var title: String = "",
    var image: Int = 0
)