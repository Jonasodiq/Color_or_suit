package com.example.color_or_suit

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try { // link-> https://www.w3schools.com/java/java_try_catch.asp

            val playButton = findViewById<MaterialButton>(R.id.playButton)
            val settingsButton = findViewById<Button>(R.id.settingsButton)
            val aboutButton = findViewById<Button>(R.id.aboutButton)

            // ClickListener
            playButton.setOnClickListener  { runGameActivity() }
            settingsButton.setOnClickListener { openSettings() }
            aboutButton.setOnClickListener { showAboutDialog() }

        } catch (e:Exception) {
            Log.e(TAG, "Error during initialization: ${e.message}", e)
        }
    }

    private fun runGameActivity() {

            val intent = Intent(this, ActivityGame::class.java)
            startActivity(intent)
    }

    private fun openSettings() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.i(TAG, "Could not start SettingsActivity.")
        }
    }

    // Display dialog about the Game
    private fun showAboutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_about, null)
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}