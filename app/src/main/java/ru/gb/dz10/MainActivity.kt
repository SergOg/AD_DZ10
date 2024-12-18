package ru.gb.dz10

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.gb.dz10.databinding.ActivityMainBinding

private const val TIMER_VALUE = "key"
private const val TIMER_STATUS = "status"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var handler = Handler(Looper.getMainLooper())

    private var timerValue = 15
    private var timeCounter = 15
    private var timerStatus = ""

    private lateinit var timerThread: Thread
    private var timerThreadAllow = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            timerValue = savedInstanceState.getInt(TIMER_VALUE)
            timerStatus = savedInstanceState.getString(TIMER_STATUS).toString()
            binding.button.text = timerStatus
            timeCounter = timerValue
            handler.post {
                updateTimer()
                if (binding.button.text == "Stop") {
                    handler.post {
                        binding.button.text = "Stop"
                    }
                    startTimer()
                }
            }
        }
        handler.post {
            binding.slider.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar, progress: Int, p2: Boolean) {
                        binding.timer.text = progress.toString()
                        timerValue = progress
                    }
                    override fun onStartTrackingTouch(p0: SeekBar) {
                    }
                    override fun onStopTrackingTouch(seek: SeekBar) {
                        val output = "Progress is: " + seek.progress + "%"
                        Toast.makeText(this@MainActivity, output, Toast.LENGTH_SHORT).show()
                        timeCounter = timerValue
                    }
                })
            binding.button.setOnClickListener {
                if (binding.button.text == "Start") {
                    handler.post {
                        binding.button.text = "Stop"
                    }
                    startTimer()
                } else {
                    handler.post {
                        binding.button.text = "Start"
                    }
                    stopTimerThread()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimer() {
        binding.timer.text = timeCounter.toString()
        binding.progressBar.progress = timeCounter
    }

    @SuppressLint("SetTextI18n")
    private fun startTimer() {
        timerThread = Thread {
            while (!timerThreadAllow && timeCounter > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                timeCounter--
                handler.post {
                    updateTimer()
                }
            }
            if (timeCounter == 0) {
                handler.post {
                    binding.button.text = "Start"
                }
            }
            handler.post {
                if (binding.button.text == "Start" && timeCounter == 0) {
                    Toast.makeText(this, "Ваше время истекло!", Toast.LENGTH_SHORT).show()
                }
                timerValue = binding.slider.progress
                timeCounter = timerValue
                timerThreadAllow = false
                handler.post { redraw() }
            }
        }
        binding.button.text = "Start"
        handler.post { redraw() }
        timerThread.start()
    }

    @SuppressLint("SetTextI18n")
    private fun redraw() {
        when (binding.button.text == "Stop") {
            true -> {
                binding.slider.isEnabled = false
            }
            false -> {
                binding.slider.isEnabled = true
            }
        }
        binding.progressBar.max = binding.slider.progress
        binding.progressBar.progress = timeCounter
        binding.timer.text = timerValue.toString()
    }

    private fun stopTimerThread() {
        timerThreadAllow = true
        timerThread.interrupt()
    }

    override fun onPause() {
        super.onPause()
        try {
            stopTimerThread()
        } catch (e: Exception) {
            Toast.makeText(this, "onPause: $e", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onRestart() {
        super.onRestart()
        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show()
        binding.button.text = "Start"
        binding.slider.isEnabled = true
    }

    //Сохранение перед вызовом onPause()
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(TIMER_VALUE, timeCounter)
        timerStatus = binding.button.text.toString()
        outState.putString(TIMER_STATUS, timerStatus)
        super.onSaveInstanceState(outState)
    }
}