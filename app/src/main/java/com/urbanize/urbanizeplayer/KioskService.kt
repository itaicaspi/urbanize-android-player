package com.urbanize.urbanizeplayer

import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.startActivity
import android.content.ComponentName
import android.app.ActivityManager
import android.app.Service
import android.content.Context.ACTIVITY_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS


class KioskService : Service() {

    private var t: Thread? = null
    private var ctx: Context? = null
    private var running = false

    private val isInBackground: Boolean
        get() {
            val am = ctx!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo.get(0).topActivity
            return !ctx!!.getApplicationContext().getPackageName().equals(componentInfo?.getPackageName())
        }

    override fun onDestroy() {
        Log.i(TAG, "Stopping service 'KioskService'")
        running = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Starting service 'KioskService'")
        running = true
        ctx = this

        // start a thread that periodically checks if your app is in the foreground
        t = Thread(Runnable {
            do {
                handleKioskMode()
                try {
                    Thread.sleep(INTERVAL)
                } catch (e: InterruptedException) {
                    Log.i(TAG, "Thread interrupted: 'KioskService'")
                }

            } while (running)
            stopSelf()
        })

        t!!.start()
        return Service.START_NOT_STICKY
    }

    private fun handleKioskMode() {
        // is Kiosk Mode active?
        if (isKioskModeActive(this)) {
            // is App in background?
            if (isInBackground) {
                restoreApp() // restore!
            }
        }
    }

    private fun restoreApp() {
        // Restart activity
        val i = Intent(ctx, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx!!.startActivity(i)
    }

    fun isKioskModeActive(context: Context): Boolean {
        val sp = getDefaultSharedPreferences(context)
        return sp.getBoolean(PREF_KIOSK_MODE, false)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {

        private val INTERVAL = TimeUnit.SECONDS.toMillis(2) // periodic interval to check in seconds -> 2 seconds
        private val TAG = KioskService::class.java.simpleName
        private val PREF_KIOSK_MODE = "pref_kiosk_mode"
    }
}