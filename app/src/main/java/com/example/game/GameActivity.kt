package com.example.game

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class GameActivity : Activity() {

    private val emojis = listOf(
        "🐶", "🐱", "🦊", "🐼", "🐸", "🐵",
        "🦁", "🐯", "🐰", "🐻", "🐨", "🐷",
        "🐙", "🦋", "🐢", "🐬", "🦄", "🐳"
    )

    private lateinit var cards: MutableList<String>
    private lateinit var matched: BooleanArray
    private lateinit var opened: BooleanArray
    private lateinit var adapter: CardAdapter

    private var firstCard = -1
    private var moves = 0
    private var seconds = 0
    private var size = 4
    private var locked = false
    private var finished = false

    private lateinit var tvMoves: TextView
    private lateinit var tvTimer: TextView
    private lateinit var recycler: RecyclerView

    private val handler = Handler(Looper.getMainLooper())

    private val timerTask = object : Runnable {
        override fun run() {
            if (!finished) {
                seconds++
                updateCounters()
                saveProgress()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvMoves = findViewById(R.id.tvMoves)
        tvTimer = findViewById(R.id.tvTimer)
        recycler = findViewById(R.id.rvCards)

        findViewById<Button>(R.id.btnMenu).setOnClickListener {
            saveProgress()
            finish()
        }

        val saved = if (
            intent.getBooleanExtra("continue", false)
        ) AppStorage.loadGame(this) else null

        if (saved != null) {
            restoreGame(saved)
        } else {
            size = intent.getIntExtra("size", 4)
                .let { if (it == 6) 6 else 4 }

            newGame()
        }

        recycler.layoutManager = GridLayoutManager(this, size)

        adapter = CardAdapter(cards, opened, matched) {
            onCardClicked(it)
        }

        recycler.adapter = adapter

        updateCounters()
        handler.postDelayed(timerTask, 1000)
    }

    private fun newGame() {
        cards = emojis.take((size * size) / 2)
            .flatMap { listOf(it, it) }
            .shuffled()
            .toMutableList()

        matched = BooleanArray(cards.size)
        opened = BooleanArray(cards.size)
        moves = 0
        seconds = 0
        firstCard = -1
        locked = false
        finished = false

        saveProgress()
    }

    private fun restoreGame(data: JSONObject) {
        size = data.optInt("size", 4)
            .let { if (it == 6) 6 else 4 }

        val array = data.optJSONArray("cards") ?: JSONArray()
        cards = MutableList(array.length()) {
            array.getString(it)
        }

        if (cards.size != size * size) {
            newGame()
            return
        }

        val matches = data.optJSONArray("matched") ?: JSONArray()
        matched = BooleanArray(cards.size) {
            matches.optBoolean(it, false)
        }

        // Незавершённые перевороты после загрузки закрываем.
        opened = BooleanArray(cards.size)

        moves = data.optInt("moves", 0)
        seconds = data.optInt("seconds", 0)
        firstCard = -1
        locked = false
        finished = false
    }

    private fun onCardClicked(position: Int) {
        if (locked || finished) return
        if (matched[position] || opened[position]) return

        opened[position] = true
        adapter.notifyItemChanged(position)

        if (firstCard == -1) {
            firstCard = position
            saveProgress()
            return
        }

        moves++
        val first = firstCard
        val second = position
        firstCard = -1

        updateCounters()

        if (cards[first] == cards[second]) {
            matched[first] = true
            matched[second] = true

            adapter.notifyItemChanged(first)
            adapter.notifyItemChanged(second)

            saveProgress()

            if (matched.all { it }) {
                finishGame()
            }
        } else {
            locked = true

            handler.postDelayed({
                opened[first] = false
                opened[second] = false

                adapter.notifyItemChanged(first)
                adapter.notifyItemChanged(second)

                locked = false
                saveProgress()
            }, 800)

            saveProgress()
        }
    }

    private fun updateCounters() {
        tvMoves.text = "Ходы: $moves"
        tvTimer.text = "Время: %02d:%02d".format(
            seconds / 60,
            seconds % 60
        )
    }

    private fun saveProgress() {
        if (!::cards.isInitialized || finished) return

        val data = JSONObject().apply {
            put("size", size)
            put("moves", moves)
            put("seconds", seconds)

            put("cards", JSONArray().apply {
                cards.forEach { put(it) }
            })

            put("matched", JSONArray().apply {
                matched.forEach { put(it) }
            })
        }

        AppStorage.saveGame(this, data)
    }

    private fun finishGame() {
        finished = true
        handler.removeCallbacks(timerTask)
        AppStorage.clearGame(this)

        val input = EditText(this).apply {
            hint = "Имя игрока"
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
        }

        AlertDialog.Builder(this)
            .setTitle("Победа! 🎉")
            .setMessage(
                "Ходы: $moves\n" +
                        "Время: %02d:%02d\n\nВведите имя для рекорда:"
                            .format(seconds / 60, seconds % 60)
            )
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = input.text.toString()
                    .trim()
                    .ifBlank { "Игрок" }

                AppStorage.saveRecord(
                    this, name, moves, seconds, size
                )

                Toast.makeText(
                    this,
                    "Результат сохранён!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .setNegativeButton("Не сохранять") { _, _ ->
                finish()
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerTask)
        saveProgress()
    }

    override fun onResume() {
        super.onResume()

        if (::cards.isInitialized && !finished) {
            handler.removeCallbacks(timerTask)
            handler.postDelayed(timerTask, 1000)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
