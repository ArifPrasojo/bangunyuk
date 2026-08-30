package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Exact 5-Color Gundam Palette from User Image:
// 1. Titanium White (#FFFFFF) - Canvas & Card Backgrounds
// 2. Beam Yellow (#FFF04B) - Highlight & Accents
// 3. Core Red (#F02D3A) - Hazard & Action Accents
// 4. Federation Royal Blue (#2353B2) - Primary Brand & Buttons
// 5. Inner-Frame Slate Gray (#505364) - Borders, Secondary Details

val GundamWhite = Color(0xFFFFFFFF)
val GundamCanvasBg = Color(0xFFFFFFFF)
val GundamCardBg = Color(0xFFF8FAFC)
val GundamCardBgAlt = Color(0xFFF1F5F9)
val GundamOffWhite = Color(0xFFF1F5F9)

val GundamYellow = Color(0xFFFFF04B)
val GundamGold = Color(0xFFEAB308) // Warmer yellow for text readability on white
val GundamRed = Color(0xFFF02D3A)
val GundamBlue = Color(0xFF2353B2)
val GundamBlueLight = Color(0xFF3B82F6)
val GundamBlueDark = Color(0xFF1E3A8A)
val GundamBlueSubtle = Color(0xFFEFF6FF)

// Slate & Border Tones (derived from #505364)
val GundamSlate = Color(0xFF505364)
val GundamTextPrimary = Color(0xFF0F172A)
val GundamTextSecondary = Color(0xFF64748B)
val GundamTextMuted = Color(0xFF94A3B8)
val GundamBorder = Color(0xFFE2E8F0)
val GundamBorderStrong = Color(0xFFCBD5E1)

// Backward-compatible aliases
val GundamDarkArmor = GundamCanvasBg
val GundamArmorPlate = GundamCardBg
val GundamNavy = GundamBlueSubtle
val GundamChassis = GundamCardBgAlt
val GundamSilver = GundamTextSecondary
val GundamPanelLine = GundamBorder
val GundamPanelLineBright = GundamBorderStrong
val GundamCyan = GundamBlueLight
val GundamGreen = GundamGold

// Immersive Theme Mappings (Clean Light Theme)
val ImmersiveBg = GundamCanvasBg
val ImmersiveDeepPurple = GundamBlueDark
val ImmersiveSurface = GundamCardBg
val ImmersiveSurfaceVariant = GundamCardBgAlt
val ImmersivePrimary = GundamBlue
val ImmersiveOnPrimary = GundamWhite
val ImmersivePrimaryContainer = GundamBlueSubtle
val ImmersiveOnPrimaryContainer = GundamBlueDark
val ImmersiveSecondary = GundamSlate
val ImmersiveOnSecondary = GundamWhite
val ImmersiveSecondaryContainer = GundamOffWhite
val ImmersiveOnSecondaryContainer = GundamTextPrimary
val ImmersiveTertiary = GundamGold
val ImmersiveOnTertiary = GundamTextPrimary
val ImmersiveOutline = GundamBorder
val ImmersiveOutlineVariant = GundamBorderStrong
val ImmersiveOnSurface = GundamTextPrimary
val ImmersiveOnSurfaceVariant = GundamTextSecondary

// Mission Badges
val PhotoMissionColor = GundamBlue
val MathMissionColor = GundamGold
val ShakeMissionColor = GundamRed
val StepsMissionColor = GundamBlue
val MemoryMissionColor = GundamGold
val TypingMissionColor = GundamSlate





