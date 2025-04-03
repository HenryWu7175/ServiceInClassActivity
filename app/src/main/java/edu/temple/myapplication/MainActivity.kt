package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    //timerBinder will be used to communicate with the service
    //this timerBinder will be null until the service is connected to the activity
    private var timerBinder: TimerService.TimerBinder? = null
    //isBound will be used to check if the service is connected to the activity
    private var isBound = false


    //this is the service connection object that will be used to connect to the service to communicate with it and update the UI
    //there will be a callback when the service is connected and disconnected from the activity
    private val serviceConnection = object : ServiceConnection {
        //this will be called when the service is connected to the activity
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //we cast the IBinder to a TimerBinder and set the timerBinder to it
            timerBinder = service as TimerService.TimerBinder
            isBound = true
            //we set the handler to the main activity and use log to check if the service is connected to the activity
            Log.d("MainActivity", "Service connected")
        }

        //this will be called when the service is disconnected from the activity
        override fun onServiceDisconnected(name: ComponentName?) {
            //we set the timerBinder to null and set the isBound to false
            timerBinder = null
            isBound = false
            //we use log to check if the service is disconnected from the activity
            Log.d("MainActivity", "Service disconnected")
        }
    }

    //this will be called when the activity is destroyed, typically by the system or by the user
    override fun onDestroy() {
        super.onDestroy()
    //we unbind the service from the activity and set the isBound to false
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //this intent will start the service
        val intent = Intent(this, TimerService::class.java)
        //this will bind the service to the activity with a connection object and a flag to check if the service is connected to the activity
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        //this will be called when the start button is clicked and logs to check if the service is connected to the activity
        findViewById<Button>(R.id.startButton).setOnClickListener {
                timerBinder?.start(10)
                Log.d("MainActivity", "Service started")

        }

        //this will be called when the stop button is clicked and logs to check if the service is connected to the activity
        findViewById<Button>(R.id.stopButton).setOnClickListener {
                timerBinder?.stop()
                Log.d("MainActivity", "Service stopped")

        }
    }
}