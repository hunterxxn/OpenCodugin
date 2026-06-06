package com.github.hunterxxn.opencodugin.update

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import java.net.HttpURLConnection

sealed class UpdateResult {
    data class Available(val latestVersion: String, val downloadUrl: String) : UpdateResult()
    data object UpToDate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/hunterxxn/OpenCodugin/releases/latest"

    internal var mockReleaseTag: String? = null
    internal var mockReleaseUrl: String? = null

    fun checkForUpdate(): UpdateResult {
        try {
            val (tagName, htmlUrl) = if (mockReleaseTag != null) {
                mockReleaseTag!! to (mockReleaseUrl ?: "https://github.com/hunterxxn/OpenCodugin/releases")
            } else {
                val json = fetchLatestReleaseJson()
                    ?: return UpdateResult.Error("Network error, please check your connection")
                if (json.isEmpty()) return UpdateResult.UpToDate

                val tag = extractJsonString(json, "tag_name")
                    ?: return UpdateResult.Error("Cannot parse version from response")
                val url = extractJsonString(json, "html_url")
                    ?: return UpdateResult.Error("Cannot parse URL from response")
                tag to url
            }

            val currentVersion = getCurrentPluginVersion()
                ?: return UpdateResult.Error("Cannot determine current version")
            val latestVersion = tagName.removePrefix("v").removePrefix("V")

            if (isNewer(latestVersion, currentVersion)) {
                return UpdateResult.Available(latestVersion, htmlUrl)
            }
            return UpdateResult.UpToDate
        } catch (e: Exception) {
            return UpdateResult.Error(e.message ?: "Unknown error")
        }
    }

    fun getCurrentVersion(): String? = getCurrentPluginVersion()

    private fun fetchLatestReleaseJson(): String? {
        val connection = java.net.URI(GITHUB_API_URL).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == 404)
                throw java.io.IOException("No release found — ensure at least one release is published on GitHub (not draft)")
            if (responseCode != HttpURLConnection.HTTP_OK) return null
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = """"$key"\s*:\s*"((?:[^"\\]|\\.)*)"""".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.replace("\\/", "/")
    }

    private fun getCurrentPluginVersion(): String? {
        val pluginId = PluginId.getId("com.github.hunterxxn.opencodugin")
        return PluginManagerCore.getPlugin(pluginId)?.version
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: return false }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: return false }
        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
