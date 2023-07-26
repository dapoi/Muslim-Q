package com.prodev.muslimq.core.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity
import com.simform.refresh.SSPullToRefreshLayout

fun capitalizeEachWord(str: String, delimiter: String = " ", separator: String = " "): String {
    return str.split(delimiter).joinToString(separator) {
        it.lowercase().replaceFirstChar { char -> char.titlecase() }
    }
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
}

fun isOnline(context: Context): Boolean {
    var result = false
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.run {
        cm.getNetworkCapabilities(cm.activeNetwork)?.run {
            result = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
    return result
}

fun swipeRefresh(
    context: Context,
    method: () -> Unit,
    srl: SSPullToRefreshLayout,
    clNoInternet: ConstraintLayout,
    rv: RecyclerView = RecyclerView(context),
) {
    srl.apply {
        setLottieAnimation("loading.json")
        setRepeatMode(SSPullToRefreshLayout.RepeatMode.REPEAT)
        setRepeatCount(SSPullToRefreshLayout.RepeatCount.INFINITE)
        setOnRefreshListener(object : SSPullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                val handlerData = Handler(Looper.getMainLooper())
                val check = isOnline(context)
                if (check) {
                    handlerData.postDelayed({
                        setRefreshing(false)
                    }, 2000)

                    handlerData.postDelayed({
                        clNoInternet.visibility = View.GONE
                        method()
                    }, 2350)
                } else {
                    rv.visibility = View.GONE
                    clNoInternet.visibility = View.VISIBLE
                    setRefreshing(false)
                }
            }
        })
    }
}

class InternetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = isOnline(context)
        if (status) {
            (context as Activity).runOnUiThread {
                recreateActivity(context)
            }
        }
    }

    private fun recreateActivity(activity: Activity) {
        activity.intent.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity.finish()
            activity.overridePendingTransition(0, 0)
            activity.startActivity(it)
            activity.overridePendingTransition(0, 0)
        }
    }
}

fun defaultDzikir(): List<TasbihEntity> = listOf(
    TasbihEntity("Subhanallah", DzikirType.DEFAULT, "سُبْحَانَ ٱللَّٰهِ", "Maha suci Allah"),
    TasbihEntity("Alhamdulillah", DzikirType.DEFAULT, "الْحَمْدُ للَّهِِ", "Segala Puji Bagi Allah"),
    TasbihEntity("Allahu Akbar", DzikirType.DEFAULT, "ٱللَّٰهُ أَكْبَرُِ", " Allah Maha Besar")
)

fun defaultDzikirPagi(): List<TasbihEntity> = listOf(
    TasbihEntity("Taawudz", DzikirType.PAGI, "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ", "Aku berlindung kepada Allah dari godaan syaitan yang terkutuk."),
    TasbihEntity("Ayat Kursi", DzikirType.PAGI, "اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ، لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ، لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ، مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ، يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ، وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلاَّ بِمَا شَاءَ، وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ، وَلَا يَئُودُهُ حِفْظُهُمَا، وَهُوَ الْعَلِيُّ الْعَظِيمُ", "Allah, tidak ada ilah (yang berhak disembah) melainkan Dia, yang hidup kekal lagi terus menerus mengurus (makhluk-Nya). Dia tidak mengantuk dan tidak tidur. Kepunyaan-Nya apa yang di langit dan di bumi. Tiada yang dapat memberi syafa’at di sisi-Nya tanpa seizin-Nya. Dia mengetahui apa-apa yang di hadapan mereka dan di belakang mereka. Mereka tidak mengetahui apa-apa dari ilmu Allah melainkan apa yang dikehendaki-Nya. Kursi Allah meliputi langit dan bumi. Dia tidak merasa berat memelihara keduanya. Dan Dia Maha Tinggi lagi Maha besar"),
    TasbihEntity("An-Ikhlas", DzikirType.PAGI, "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n" +
            "قُلْ هُوَ اللَّهُ أَحَدٌ اللَّهُ الصَّمَدُ لَمْ يَلِدْ وَلَمْ يُولَدْ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Katakanlah: Dialah Allah, Yang Maha Esa. Allah adalah ilah yang bergantung kepada-Nya segala urusan. Dia tidak beranak dan tiada pula diperanakkan, dan tidak ada seorang pun yang setara dengan Dia"),
    TasbihEntity("Al-Falaq", DzikirType.PAGI, "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n" +
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ مِن شَرِّ مَا خَلَقَ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ  وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Katakanlah: Aku berlindung kepada Rabb yang menguasai Shubuh, dari kejahatan makhluk-Nya, dan dari kejahatan malam apabila telah gelap gulita, dan dari kejahatan-kejahatan wanita tukang sihir yang menghembus pada buhul-buhul, dan dari kejahatan orang yang dengki apabila ia dengki."),
    TasbihEntity("An-Nass", DzikirType.PAGI, "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n" +
            "قُلْ أَعُوذُ بِرَبِّ النَّاسِ مَلِكِ النَّاسِ إِلَهِ النَّاسِ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ مِنَ الْجِنَّةِ وَ النَّاسِ", "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang. Katakanlah: Aku berlindung kepada Rabb manusia. Raja manusia. Sembahan manusia, dari kejahatan (bisikan) syaitan yang biasa bersembunyi, yang membisikkan (kejahatan) ke dalam dada manusia, dari jin dan manusia.")
)
fun defaultDzikirSore(): List<String> = listOf(
    "Subhanallah - Sore",
    "Alhamdulillah - Sore",
    "Allahu Akbar - Sore"
)
fun defaultDzikirShalat(): List<String> = listOf(
    "Subhanallah - Shalat",
    "Alhamdulillah - Shalat",
    "Allahu Akbar - Shalat"
)