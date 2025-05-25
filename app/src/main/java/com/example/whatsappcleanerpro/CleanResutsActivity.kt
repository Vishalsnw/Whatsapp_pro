package com.example.whatsappcleanerpro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CleanResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clean_results)

        val freedSpace = intent.getStringExtra("FREED_SPACE") ?: "0 MB"
        val freedSpaceText = findViewById<TextView>(R.id.freedSpaceText)
        freedSpaceText.text = "$freedSpace Freed"

        val backBtn = findViewById<Button>(R.id.backToHomeBtn)
        backBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
