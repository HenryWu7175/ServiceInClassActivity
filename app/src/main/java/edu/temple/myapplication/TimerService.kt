package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false
        private set // Make isRunning settable only within the Service

    private var timerHandler: Handler? = null

    private var timerThread: TimerThread? = null // Use nullable TimerThread

    private var paused = false
        private set // Make paused settable only within the Service

    inner class TimerBinder : Binder() {

        // Check if Timer is currently running (read-only)
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is currently paused (read-only)
        val isPaused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int) {
            if (!this@TimerService.paused) {
                if (!this@TimerService.isRunning) {
                    stopInternalTimer() // Ensure any existing timer is stopped
                    this@TimerService.startInternalTimer(startValue)
                } else {
                    Log.d("TimerAction", "Timer already running")
                }
            } else {
                pauseInternalTimer() // Resume if paused
            }
        }

        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            this@TimerService.stopInternalTimer()
        }

        // Pause or resume a running timer
        fun pause() {
            this@TimerService.pauseInternalTimer()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    private fun startInternalTimer(startValue: Int) {
        isRunning = true
        paused = false
        timerThread = TimerThread(startValue)
        timerThread?.start()
    }

    private fun pauseInternalTimer() {
        if (isRunning) {
            paused = !paused
        } else {
            Log.d("TimerAction", "Cannot pause, timer is not running")
        }
    }

    private fun stopInternalTimer() {
        timerThread?.interrupt()
        timerThread = null
        isRunning = false
        paused = false
    }

    inner class TimerThread(private val startValue: Int) : Thread() {
        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1) {
                    Log.d("Countdown", i.toString())
                    timerHandler?.sendEmptyMessage(i)
                    while (paused) {
                        sleep(100) // Small delay while paused
                    }
                    sleep(1000)
                }
                isRunning = false
                timerHandler?.sendEmptyMessage(0) // Send 0 when finished
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
                timerHandler?.sendEmptyMessage(-1) // Indicate interruption
            } finally {
                isRunning = false // Ensure isRunning is false when thread ends
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopInternalTimer()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TimerService status", "Destroyed")
    }
}