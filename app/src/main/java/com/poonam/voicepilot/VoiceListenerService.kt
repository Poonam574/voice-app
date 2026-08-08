package com.poonam.voicepilot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat

/**
 * Runs continuously in the background so the person never has to open
 * VoicePilot's screen again after the first setup. Android requires a
 * visible notification for any app that listens in the background --
 * this is a privacy protection, not something VoicePilot can hide.
 */
class VoiceListenerService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var appMatcher: AppMatcher
    private val channelId = "voicepilot_listening"

    override fun onCreate() {
        super.onCreate()
        appMatcher = AppMatcher(this)
        startForeground(1, buildNotification("Listening for a command..."))
        startListeningLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Android restarts the service if it gets killed
    }

    private fun startListeningLoop() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val heard = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()

                if (heard.isNotBlank()) {
                    val match = appMatcher.match(heard)
                    if (match != null) {
                        val launchIntent = packageManager.getLaunchIntentForPackage(match.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(launchIntent)
                            updateNotification("Opened ${match.name}")
                        }
                    }
                }
                restartListening()
            }

            override fun onError(error: Int) {
                restartListening() // Keep going even after silence/timeout/errors
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun restartListening() {
        speechRecognizer?.destroy()
        // Small restart loop so it keeps listening indefinitely.
        android.os.Handler(mainLooper).postDelayed({ startListeningLoop() }, 300)
    }

    private fun buildNotification(text: String): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId, "VoicePilot listening", NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VoicePilot")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, buildNotification(text))
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
