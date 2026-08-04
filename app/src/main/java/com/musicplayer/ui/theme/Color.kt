package com.musicplayer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens for the app.
 *
 * The palette is intentionally near-black with layered surfaces so album
 * artwork is the brightest thing on screen, which is how the reference
 * players (Spotify, YouTube Music, Poweramp) handle contrast.
 */

// Backgrounds and surfaces
val Background = Color(0xFF0B0B0D)
val Surface = Color(0xFF16161A)
val SurfaceLight = Color(0xFF202027)
val SurfaceElevated = Color(0xFF2A2A33)
val Scrim = Color(0xCC000000)

// Brand
val Primary = Color(0xFF1DB954)
val PrimaryDark = Color(0xFF16994A)
val PrimarySoft = Color(0x331DB954)
val Accent = Color(0xFF5AC8FA)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB6B6C0)
val TextMuted = Color(0xFF7A7A85)

// Lines and states
val Border = Color(0xFF2C2C34)
val Divider = Color(0xFF1F1F26)
val ErrorRed = Color(0xFFFF5C5C)
val Overlay = Color(0xB3000000)

// Light theme (used when the user opts out of dark mode)
val LightBackground = Color(0xFFFBFBFD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEFEFF3)
val LightTextPrimary = Color(0xFF121216)
val LightTextSecondary = Color(0xFF5A5A66)
val LightBorder = Color(0xFFE1E1E8)
