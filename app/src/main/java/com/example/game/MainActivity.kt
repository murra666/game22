
package com.example.game

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.RadioButton

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rbEasy = findViewById<RadioButton>(R.id.rbEasy)
        val rbHard = findViewById<RadioButton>(R.id.rbHard)
        val btnNew = findViewById<Button>(R.id.btnNewGame)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnRecords = findViewById<Button>(R.id.btnRecords)

        fun updateContinueButton() {
            btnContinue.isEnabled = AppStorage.hasGame(this)
            btnContinue.alpha =
                if (btnContinue.isEnabled) 1f else 0.45f
        }

        updateContinueButton()

        btnNew.setOnClickListener {
            val size = if (rbHard.isChecked) 6 else 4

            AppStorage.clearGame(this)

            startActivity(
                Intent(this, GameActivity::class.java)
                    .putExtra("size", size)
            )
        }

        btnContinue.setOnClickListener {
            if (AppStorage.hasGame(this)) {
                startActivity(
                    Intent(this, GameActivity::class.java)
                        .putExtra("continue", true)
                )
            }
        }

        btnRecords.setOnClickListener {
            startActivity(
                Intent(this, RecordsActivity::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()

        val btn = findViewById<Button>(R.id.btnContinue)
        btn.isEnabled = AppStorage.hasGame(this)
        btn.alpha = if (btn.isEnabled) 1f else 0.45f
    }
}
