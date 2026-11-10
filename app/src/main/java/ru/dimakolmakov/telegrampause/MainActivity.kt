package ru.dimakolmakov.telegrampause

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var selectedMinutes = 15
    private var endTimeMillis = 0L
    private var running = false

    private lateinit var timerText: TextView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return

            val remaining = endTimeMillis - System.currentTimeMillis()
            if (remaining <= 0L) {
                running = false
                timerText.text = "00:00"
                statusText.text = "Пауза завершена. Можно продолжать!"
                startButton.text = "Начать паузу снова"
                Toast.makeText(this@MainActivity, "Время паузы закончилось", Toast.LENGTH_SHORT).show()
                return
            }

            val totalSeconds = remaining / 1000L
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            timerText.text = String.format("%02d:%02d", minutes, seconds)
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(Color.rgb(12, 18, 34))
        }

        val title = TextView(this).apply {
            text = "TELEGRAM PAUSE"
            textSize = 27f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWidth())

        val subtitle = TextView(this).apply {
            text = "Небольшой перерыв от телефона"
            textSize = 16f
            setTextColor(Color.rgb(170, 190, 220))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }
        root.addView(subtitle, matchWidth())

        timerText = TextView(this).apply {
            text = "15:00"
            textSize = 56f
            setTextColor(Color.rgb(91, 190, 255))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }
        root.addView(timerText, matchWidth())

        statusText = TextView(this).apply {
            text = "Выбери длительность и нажми «Начать паузу»."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }
        root.addView(statusText, matchWidth())

        val choices = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(5, 15, 30).forEach { minutes ->
            val button = Button(this).apply {
                text = "$minutes мин"
                isAllCaps = false
                setOnClickListener {
                    if (!running) {
                        selectedMinutes = minutes
                        timerText.text = String.format("%02d:00", minutes)
                        statusText.text = "Выбрано: $minutes минут"
                    }
                }
            }
            choices.addView(button, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 0, 4, 0)
            })
        }
        root.addView(choices, matchWidth())

        startButton = Button(this).apply {
            text = "Начать паузу"
            isAllCaps = false
            setOnClickListener { startPause() }
        }
        root.addView(startButton, matchWidth().apply {
            topMargin = 18
        })

        val resetButton = Button(this).apply {
            text = "Сбросить"
            isAllCaps = false
            setOnClickListener { resetPause() }
        }
        root.addView(resetButton, matchWidth().apply {
            topMargin = 8
        })

        val note = TextView(this).apply {
            text = "Важно: приложение не блокирует Telegram и не меняет настройки телефона. Это добровольный таймер-памятка."
            textSize = 13f
            setTextColor(Color.rgb(150, 165, 190))
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 0)
        }
        root.addView(note, matchWidth())

        setContentView(root)
    }

    private fun startPause() {
        if (running) {
            handler.removeCallbacks(ticker)
            running = false
            statusText.text = "Пауза остановлена."
            startButton.text = "Продолжить паузу"
            return
        }

        endTimeMillis = System.currentTimeMillis() + selectedMinutes * 60_000L
        running = true
        statusText.text = "Идёт пауза. Можно свернуть приложение."
        startButton.text = "Приостановить"
        handler.removeCallbacks(ticker)
        handler.post(ticker)
    }

    private fun resetPause() {
        handler.removeCallbacks(ticker)
        running = false
        timerText.text = String.format("%02d:00", selectedMinutes)
        statusText.text = "Таймер сброшен."
        startButton.text = "Начать паузу"
    }

    private fun matchWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }
}
