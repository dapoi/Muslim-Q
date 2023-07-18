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

fun defaultDzikir(): List<String> = listOf(
    "Subhanallah",
    "Alhamdulillah",
    "Allahu Akbar"
)