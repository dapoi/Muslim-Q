package com.prodev.muslimq.presentation.view.others

import com.prodev.muslimq.R

object OthersObject {

    private val menuImage = intArrayOf(
        R.drawable.ic_collection,
        R.drawable.ic_dark_mode,
        R.drawable.ic_asmaul_husna,
        R.drawable.ic_tasbih,
        R.drawable.ic_notif_setting,
        R.drawable.ic_hearing,
        R.drawable.ic_about,
        R.drawable.ic_about
    )

    private val menuTitle = listOf(
        R.string.menu_read_later,
        R.string.menu_dark_mode,
        R.string.menu_asmaul_husna,
        R.string.menu_digital_tasbih,
        R.string.menu_notification_settings,
        R.string.menu_choose_muezzin,
        R.string.menu_app_info,
        R.string.menu_languages,
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
    var title: Int = 0,
    var image: Int = 0
)