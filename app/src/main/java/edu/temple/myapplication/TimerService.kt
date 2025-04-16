package edu.temple.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    lateinit var t: TimerThread

    private var paused: Boolean = false

    private lateinit var sharedPreferences: android.content.SharedPreferences

    private var currentCountdownValue: Int = 0 // Initialize with a default value

    private val PREFERENCES_KEY = "countdown_value"

    inner class TimerBinder : Binder() {


        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is paused
        val paused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int){

            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                // If timer is paused, resume it
                pause()
            }
        }

        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning || paused) {
                t.interrupt()
                this@TimerService.paused = false //reset paused flag
                saveState(0)
            }
        }

        // Pause a running timer
        fun pause() {
                this@TimerService.pause()
        }
        fun getCurrentValue(): Int {
            return currentCountdownValue
        }

        fun resetSavedValue() {
            this@TimerService.resetSavedValue()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        currentCountdownValue = sharedPreferences.getInt(PREFERENCES_KEY, 0)
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause () {
        if (::t.isInitialized && isRunning) {
            paused = !paused
            isRunning = !paused
            if (paused) {
                saveState(currentCountdownValue)
            }
        }
    }

    private fun saveState(value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREFERENCES_KEY, value)
        editor.apply()
        Log.d("TimerService status", "Saved state $value")


    }

    private fun resetSavedValue() {
        val editor = sharedPreferences.edit()
        editor.remove(PREFERENCES_KEY)
        editor.apply()
        Log.d("TimerService", "Saved countdown value reset")
    }


    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1)  {
                    Log.d("Countdown", i.toString())

                    currentCountdownValue = i

                    timerHandler?.sendEmptyMessage(i)

                    while (paused);
                    sleep(1000)

                }
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("TimerService status", "Destroyed")
    }


}