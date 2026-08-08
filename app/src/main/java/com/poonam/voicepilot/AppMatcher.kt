package com.poonam.voicepilot

import android.content.pm.PackageManager
import android.content.Context

/**
 * A launchable app VoicePilot knows about: a friendly name, spoken aliases,
 * and the real Android package name used to launch it.
 */
data class AppEntry(
    val name: String,
    val packageName: String,
    val aliases: List<String> = emptyList()
)

/**
 * A small built-in list of common apps and their real package names.
 * This covers the apps most people ask for by name. Anything not listed
 * here is looked up live from the phone's installed apps (see
 * findInstalledAppByName), so VoicePilot isn't limited to this list.
 */
object AppDatabase {
    val apps = listOf(
        AppEntry("WhatsApp", "com.whatsapp", listOf("whats app", "watsapp")),
        AppEntry("Instagram", "com.instagram.android", listOf("insta")),
        AppEntry("Facebook", "com.facebook.katana", listOf("fb")),
        AppEntry("YouTube", "com.google.android.youtube", listOf("you tube")),
        AppEntry("Gmail", "com.google.android.gm", listOf("g mail", "email")),
        AppEntry("Chrome", "com.android.chrome", listOf("browser", "google chrome")),
        AppEntry("Maps", "com.google.android.apps.maps", listOf("google maps")),
        AppEntry("Camera", "com.android.camera", listOf()),
        AppEntry("Settings", "com.android.settings", listOf()),
        AppEntry("Phone", "com.android.dialer", listOf("dialer", "call")),
        AppEntry("Messages", "com.google.android.apps.messaging", listOf("sms", "texts")),
        AppEntry("Play Store", "com.android.vending", listOf("playstore", "google play")),
        AppEntry("Spotify", "com.spotify.music", listOf()),
        AppEntry("Twitter", "com.twitter.android", listOf("x")),
        AppEntry("Telegram", "org.telegram.messenger", listOf()),
        AppEntry("Snapchat", "com.snapchat.android", listOf("snap")),
        AppEntry("Netflix", "com.netflix.mediaclient", listOf())
    )
}

/**
 * Scores a spoken phrase against the app database (and, if nothing matches,
 * against every app actually installed on the phone) and returns the best
 * match, or null if nothing scores well enough.
 */
class AppMatcher(private val context: Context) {

    fun match(spokenPhrase: String): AppEntry? {
        val cleaned = clean(spokenPhrase)

        // 1. Try the curated database first (covers common apps reliably).
        var best: AppEntry? = null
        var bestScore = 0
        for (entry in AppDatabase.apps) {
            val score = score(cleaned, entry)
            if (score > bestScore) {
                bestScore = score
                best = entry
            }
        }
        if (best != null && bestScore >= 60) return best

        // 2. Fall back to whatever is actually installed on this phone.
        findInstalledAppByName(cleaned)?.let { return it }

        return best // may be null
    }

    private fun clean(text: String): String =
        text.lowercase()
            .replace(Regex("\\bopen\\b"), "")
            .replace(Regex("\\blaunch\\b"), "")
            .replace(Regex("\\bstart\\b"), "")
            .trim()

    private fun score(spoken: String, entry: AppEntry): Int {
        val candidates = listOf(entry.name.lowercase()) + entry.aliases.map { it.lowercase() }
        var best = 0
        for (c in candidates) {
            val s = when {
                spoken == c -> 100
                spoken.contains(c) || c.contains(spoken) -> 80
                else -> 0
            }
            if (s > best) best = s
        }
        return best
    }

    /** Searches the phone's real installed apps for a name match. */
    private fun findInstalledAppByName(spoken: String): AppEntry? {
        val pm: PackageManager = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installed) {
            val label = pm.getApplicationLabel(appInfo).toString()
            val labelLower = label.lowercase()
            if (labelLower == spoken || labelLower.contains(spoken) || spoken.contains(labelLower)) {
                return AppEntry(label, appInfo.packageName)
            }
        }
        return null
    }
}
