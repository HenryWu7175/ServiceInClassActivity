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

/*
When the application is counting down, if the user "Pauses"
* the countdown, it should save current countdown value to persistent
* storage.
Having paused and saved the current countdown, if the user closes,
* reopens, and then tries to start a new countdown, the timer should
* continue from the previously Paused value
If the user does not pause the timer before exiting the app, restart
* the app and starting a timer should force it to begin from the initial
*  countdown value (100 or some other default).
*/
class MainActivity : AppCompatActivity() {



    private var timerBinder: TimerService.TimerBinder? = null
    private var isBound = false
    private lateinit var countdownTextView: TextView
    private lateinit var handler: Handler
    private lateinit var startButton: Button
    private lateinit var stopButton: Button // Initialize stopButton
    private var hasResumed = false
    private var defaultCountdownValue = 10

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            isBound = true
            timerBinder?.setHandler(handler)
            Log.d("TimerService status", "Connected")
            val savedValue = timerBinder?.getCurrentValue() ?: 0
            if (savedValue > 0 && timerBinder?.isRunning == false && timerBinder?.paused == true) {
                countdownTextView.text = savedValue.toString()
                startButton.text = "Resume"
            } else {
                countdownTextView.text = defaultCountdownValue.toString()
                startButton.text = "Start"
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
            isBound = false
            Log.d("TimerService status", "Disconnected")
            startButton.text = "Start"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            timerBinder = null
            Log.d("TimerService status", "Unbound")
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
                val savedValue = timerBinder?.getCurrentValue() ?: 0
                if (timerBinder!!.isRunning == false) {
                    if (timerBinder!!.paused && savedValue > 0 && startButton.text == "Resume") {
                        timerBinder?.pause() // Resume
                        startButton.text = "Pause"
                        hasResumed = true
                    } else {
                        // Start a new countdown with the default value
                        timerBinder?.start(defaultCountdownValue)
                        startButton.text = "Pause"
                        hasResumed = false
                        timerBinder?.resetSavedValue() // Ensure saved value is cleared
                        countdownTextView.text = defaultCountdownValue.toString() // Update UI immediately
                    }
                } else if (timerBinder!!.isRunning == true) {
                    timerBinder?.pause()
                    startButton.text = "Resume"
                }
            }
        }

        stopButton.setOnClickListener {
            Log.d("StopButton", "Stopping timer")
            timerBinder?.stop()
            countdownTextView.text = defaultCountdownValue.toString()
            startButton.text = "Start"
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
                    val savedValue = timerBinder?.getCurrentValue() ?: 0
                    if (timerBinder!!.isRunning == false) {
                        if (timerBinder!!.paused && savedValue > 0 && startButton.text == "Resume") {
                            timerBinder?.pause() // Resume
                            startButton.text = "Pause"
                            hasResumed = true
                        } else {
                            // Start a new countdown with the default value
                            timerBinder?.start(defaultCountdownValue)
                            startButton.text = "Pause"
                            hasResumed = false
                            timerBinder?.resetSavedValue() // Ensure saved value is cleared
                            countdownTextView.text = defaultCountdownValue.toString() // Update UI immediately
                        }
                    } else if (timerBinder!!.isRunning == true) {
                        timerBinder?.pause()
                        startButton.text = "Resume"
                    }
                }
                return true
            }
            R.id.action_stop -> {
                // You might want to trigger the stop action here as well if using menu
                timerBinder?.stop()
                countdownTextView.text = defaultCountdownValue.toString()
                startButton.text = "Start"
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}