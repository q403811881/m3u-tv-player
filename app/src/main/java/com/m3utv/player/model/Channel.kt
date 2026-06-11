package com.m3utv.player.model

/**
 * Represents a single channel parsed from an M3U playlist.
 */
data class Channel(
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null
)
