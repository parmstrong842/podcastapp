package com.example.podcastapp.data.remote

import com.example.podcastapp.BuildConfig
import java.security.MessageDigest

object AuthManager {

    fun generateAuthHeaders(): Map<String, String> {
        val apiKey = BuildConfig.PODCAST_API_KEY
        val apiSecret = BuildConfig.PODCAST_API_SECRET
        val currentUnixTime = (System.currentTimeMillis() / 1000L).toString()
        val authorization = sha1Hash(apiKey + apiSecret + currentUnixTime)

        return mapOf(
            "X-Auth-Date" to currentUnixTime,
            "Authorization" to authorization
        )
    }

    private fun sha1Hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
