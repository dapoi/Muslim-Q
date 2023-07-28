package com.prodev.muslimq.core.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
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

fun vibrateApp(context: Context): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(
            Context.VIBRATOR_MANAGER_SERVICE
        ) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION") context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
    }
}

class Compass(context: Context) : SensorEventListener {
    interface CompassListener {
        fun onNewAzimuth(azimuth: Float)
        fun onAccuracyChanged(accuracy: Int)
    }

    private var listener: CompassListener? = null
    private val sensorManager: SensorManager
    private val gSensor: Sensor
    private val mSensor: Sensor
    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val r = FloatArray(9)
    private val i = FloatArray(9)
    private var azimuth = 0f
    private var azimuthFix = 0f
    fun start() {
        sensorManager.registerListener(
            this, gSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this, mSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setListener(l: CompassListener?) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2]
            }
            val success = SensorManager.getRotationMatrix(
                r, i, mGravity,
                mGeomagnetic
            )
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + azimuthFix + 360) % 360
                listener?.onNewAzimuth(azimuth)
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            listener?.onAccuracyChanged(accuracy)
        }
    }

    init {
        sensorManager = context
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
}

/**
 * SOTW is short Side Of The World
 *
 * The class helps to convert azimuth degrees to human readable
 * label like "242° SW" or "0° N"
 *
 * This is a task of finding the closest element in the array.
 * Binary search is used to save some CPU.
 *
 * Copied with modifications from
 * https://www.geeksforgeeks.org/find-closest-number-array/
 */
class SOTWFormatter {
    init {
        initLocalizedNames()
    }

    fun format(azimuth: Float): String {
        val iAzimuth = azimuth.toInt()
        val index = findClosestIndex(iAzimuth)
        return iAzimuth.toString() + "° " + names!![index]
    }

    private fun initLocalizedNames() {
        // it will be localized version of
        // {"Utara", "Timur Laut", "Timur", "Tenggara", "Selatan", "Barat Daya", "Barat", "Barat Laut", "Utara"}
        // yes, Utara is twice, for 0 and for 360
        if (names == null) {
            names = arrayOf(
                "Utara",
                "Timur Laut",
                "Timur",
                "Tenggara",
                "Selatan",
                "Barat Daya",
                "Barat",
                "Barat Laut",
                "Utara"
            )
        }
    }

    companion object {
        private val sides = intArrayOf(0, 45, 90, 135, 180, 225, 270, 315, 360)
        private var names: Array<String>? = null

        /**
         * Finds index of the closest element to identify Side Of The World label
         * @param target
         * @return index of the closest element
         */
        private fun findClosestIndex(target: Int): Int {
            // in the original binary search https://www.geeksforgeeks.org/find-closest-number-array/
            // you will see more steps to reduce the time
            // in in this particular case the corner conditions are never true
            // e.g. azimuth is never negative, so there is no point to check
            // these conditions. Also we don't check if target is equal to element of array,
            // because most of the time it's not.

            // and the main difference is it finds the index, not the value

            // Doing binary search
            var i = 0
            var j = sides.size
            var mid = 0
            while (i < j) {
                mid = (i + j) / 2

                /* If target is less than array element,
               then search in left */
                if (target < sides[mid]) {

                    // If target is greater than previous
                    // to mid, return closest of two
                    if (mid > 0 && target > sides[mid - 1]) {
                        return getClosest(mid - 1, mid, target)
                    }

                    /* Repeat for left half */
                    j = mid
                } else {
                    if (mid < sides.size - 1 && target < sides[mid + 1]) {
                        return getClosest(mid, mid + 1, target)
                    }

                    i = mid + 1 // update i
                }
            }

            // Only single element left after search
            return mid
        }

        // Method to compare which one is the more close
        // We find the closest by taking the difference
        // between the target and both values. It assumes
        // that val2 is greater than val1 and target lies
        // between these two.
        private fun getClosest(index1: Int, index2: Int, target: Int): Int {
            return if (target - sides[index1] >= sides[index2] - target) {
                index2
            } else index1
        }
    }
}

fun getChannelId(adzanCode: Int): String {
    return when (adzanCode) {
        1 -> Constant.CHANNEL_ID_SHUBUH
        2 -> Constant.CHANNEL_ID_DZUHUR
        3 -> Constant.CHANNEL_ID_ASHAR
        4 -> Constant.CHANNEL_ID_MAGHRIB
        5 -> Constant.CHANNEL_ID_ISYA
        else -> throw IllegalArgumentException("Unknown adzan code")
    }
}

fun getChannelName(adzanCode: Int): String {
    return when (adzanCode) {
        1 -> Constant.CHANNEL_NAME_SHUBUH
        2 -> Constant.CHANNEL_NAME_DZUHUR
        3 -> Constant.CHANNEL_NAME_ASHAR
        4 -> Constant.CHANNEL_NAME_MAGHRIB
        5 -> Constant.CHANNEL_NAME_ISYA
        else -> throw IllegalArgumentException("Unknown adzan code")
    }
}