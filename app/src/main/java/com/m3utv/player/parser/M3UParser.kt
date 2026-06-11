package com.m3utv.player.parser

import com.m3utv.player.model.Channel
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Pattern

/**
 * Parses M3U / M3U8 playlist text into a list of Channel objects.
 *
 * Standard M3U format:
 *   #EXTM3U
 *   #EXTINF:-1 tvg-id="" tvg-name="CCTV1" group-title="央视",CCTV1
 *   http://example.com/stream.ts
 */
object M3UParser {

    // Match: #EXTINF:-1  optional-attributes ,display-name
    private val EXTINF_REGEX = Regex("""#EXTINF:-1\s*(.*?),(.+)""")

    // Match: key="value" attributes
    private val ATTR_REGEX = Regex("""(\w[\w-]*)\s*=\s*"([^"]*)"""")

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))

        var line: String?
        var currentExtInf: String? = null

        while (reader.readLine().also { line = it } != null) {
            val l = line?.trim() ?: continue

            when {
                l.startsWith("#EXTM3U") -> {
                    // header, skip
                }
                l.startsWith("#EXTINF:") -> {
                    currentExtInf = l
                }
                l.startsWith("#") -> {
                    // other comments / kodi extras, skip
                }
                l.isNotEmpty() && currentExtInf != null -> {
                    // URL following an #EXTINF line
                    val channel = parseExtInf(currentExtInf, l)
                    if (channel != null) {
                        channels.add(channel)
                    }
                    currentExtInf = null
                }
                l.isNotEmpty() -> {
                    // URL without #EXTINF — extract name from URL
                    val name = l.substringAfterLast("/")
                        .substringAfterLast("\\")
                        .takeIf { it.isNotEmpty() }
                        ?.substringBefore("?")
                        ?.substringBefore("#")
                        ?: "Channel ${channels.size + 1}"
                    channels.add(Channel(name = name, url = l))
                }
            }
        }

        return channels
    }

    private fun parseExtInf(extInf: String, url: String): Channel? {
        if (url.isBlank()) return null

        val match = EXTINF_REGEX.find(extInf) ?: return null
        val attrPart = match.groupValues.getOrElse(1) { "" }.trim()
        val displayName = match.groupValues.getOrElse(2) { "Unknown" }.trim()

        // Parse key="value" attributes
        val attrs = mutableMapOf<String, String>()
        for (attrMatch in ATTR_REGEX.findAll(attrPart)) {
            val key = attrMatch.groupValues.getOrElse(1) { "" }
            val value = attrMatch.groupValues.getOrElse(2) { "" }
            if (key.isNotEmpty()) {
                attrs[key] = value
            }
        }

        return Channel(
            name = displayName,
            url = url,
            logoUrl = attrs["tvg-logo"],
            groupTitle = attrs["group-title"],
            tvgId = attrs["tvg-id"],
            tvgName = attrs["tvg-name"]
        )
    }
}
