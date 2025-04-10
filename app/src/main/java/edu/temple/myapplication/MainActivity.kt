package edu.temple.myapplication

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private var isBound = false
    private lateinit var countdownTextView: TextView
    private lateinit var handler: Handler
    private lateinit var startButton: Button
    private lateinit var stopButton: Button // Initialize stopButton

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            isBound = true
            timerBinder?.setHandler(handler)
            Log.d("TimerService status", "Connected")
            updateButtonState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
            isBound = false
            Log.d("TimerService status", "Disconnected")
            updateButtonState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdownTextView = findViewById(R.id.textView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton) // Initialize

        handler = Handler(Looper.getMainLooper()) { msg ->
            countdownTextView.text = msg.what.toString()
            Log.d("Countdown", msg.what.toString())
            true
        }

        val intent = Intent(this, TimerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        startButton.setOnClickListener {
            if (isBound && timerBinder != null) {
                if (!timerBinder!!.isRunning) {
                    Log.d("StartButton", "Starting timer")
                    timerBinder?.start(10)
                    updateButtonState()
                } else {
                    Log.d("StartButton", "Pausing timer")
                    timerBinder?.pause()
                    updateButtonState()
                }
            } else {
                Log.d("StartButton", "Service not bound")
            }
        }

        stopButton.setOnClickListener {
            Log.d("StopButton", "Stopping timer")
            timerBinder?.stop()
            countdownTextView.text = "10"
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        //this checks if the timer is running or paused and updates the button text accordingly
        if (isBound && timerBinder != null) { //checjs if the service is bound and the binder is not null
            Log.d("ButtonState", "isRunning: ${timerBinder?.isRunning}")

            if (timerBinder?.isRunning == true) {
                startButton.text = "Pause"
            } else {
                startButton.text = "Start" // Default state when not running or paused
            }
        } else {
            startButton.text = "Start" // Default text when not bound
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_start -> {
                // You might want to trigger the start action here as well if using menu
                if (isBound && timerBinder != null) {
                    if (!timerBinder!!.isRunning == false && !timerBinder!!.isPaused) {
                        timerBinder?.start(10)
                        updateButtonState()
                    } else {
                        timerBinder?.pause()
                        updateButtonState()
                    }
                }
                return true
            }
            R.id.action_stop -> {
                // You might want to trigger the stop action here as well if using menu
                timerBinder?.stop()
                countdownTextView.text = "10"
                updateButtonState()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}