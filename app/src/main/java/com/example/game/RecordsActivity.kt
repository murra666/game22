package com.example.game

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class RecordsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        val tvRecords = findViewById<TextView>(R.id.tvRecords)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val records = AppStorage.loadRecords(this)
        val text = StringBuilder()

        if (records.length() == 0) {
            text.append("Рекордов пока нет.\nСыграй первую партию!")
        } else {
            for (i in 0 until records.length()) {
                val r = records.getJSONObject(i)

                val name = r.optString("name", "Игрок")
                val moves = r.optInt("moves", 0)
                val seconds = r.optInt("seconds", 0)
                val size = r.optInt("size", 4)

                text.append("${i + 1}. $name\n")
                text.append("Поле: ${size}×$size\n")
                text.append("Ходы: $moves\n")
                text.append(
                    "Время: %02d:%02d".format(
                        seconds / 60, seconds % 60
                    )
                )
                text.append("\n\n")
            }
        }

        tvRecords.text = text.toString()

        btnBack.setOnClickListener {
            finish()
        }
    }
}
