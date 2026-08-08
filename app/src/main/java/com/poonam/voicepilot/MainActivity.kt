package com.poonam.voicepilot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.poonam.voicepilot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appMatcher: AppMatcher

    private val micPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appMatcher = AppMatcher(this)

        binding.statusText.text = "Tap once to turn on always-listening mode"

        binding.micButton.setOnClickListener {
            if (hasMicPermission() && hasNotificationPermission()) {
                startAlwaysListening()
            } else {
                requestMicPermission()
            }
        }
    }

    private fun startAlwaysListening() {
        val serviceIntent = android.content.Intent(this, VoiceListenerService::class.java)
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)
        binding.statusText.text =
            "Always listening is ON.\nJust say an app name anytime — you can close this screen now."
        Toast.makeText(
            this,
            "VoicePilot is now listening in the background. You won't need to open this app again.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun hasNotificationPermission(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < 33) return true // not required before Android 13
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun requestMicPermission() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), micPermissionCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == micPermissionCode &&
            grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            startAlwaysListening()
        } else {
            Toast.makeText(this, "VoicePilot needs microphone and notification access to listen in the background", Toast.LENGTH_LONG).show()
        }
    }

}
