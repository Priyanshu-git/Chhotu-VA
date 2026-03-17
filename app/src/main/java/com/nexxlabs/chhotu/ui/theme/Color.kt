package com.nexxlabs.chhotu.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors - Modern teal/cyan theme (refined)
val PrimaryLight = Color(0xFF00838F)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFB2EBF2)
val OnPrimaryContainerLight = Color(0xFF001F24)

val PrimaryDark = Color(0xFF4DD0E1)
val OnPrimaryDark = Color(0xFF00363D)
val PrimaryContainerDark = Color(0xFF004F58)
val OnPrimaryContainerDark = Color(0xFFB2EBF2)

// Secondary colors
val SecondaryLight = Color(0xFF4A6267)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCDE7EC)
val OnSecondaryContainerLight = Color(0xFF051F23)

val SecondaryDark = Color(0xFFB1CBD0)
val OnSecondaryDark = Color(0xFF1C3438)
val SecondaryContainerDark = Color(0xFF334B4F)
val OnSecondaryContainerDark = Color(0xFFCDE7EC)

// Tertiary colors - Accent violet
val TertiaryLight = Color(0xFF525E7D)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFDAE2FF)
val OnTertiaryContainerLight = Color(0xFF0E1B37)

val TertiaryDark = Color(0xFFBBC6E9)
val OnTertiaryDark = Color(0xFF24304D)
val TertiaryContainerDark = Color(0xFF3B4664)
val OnTertiaryContainerDark = Color(0xFFDAE2FF)

// Background colors
val BackgroundLight = Color(0xFFFAFDFD)
val OnBackgroundLight = Color(0xFF191C1D)
val SurfaceLight = Color(0xFFFAFDFD)
val OnSurfaceLight = Color(0xFF191C1D)

// Teal-tinted dark backgrounds for immersive feel
val BackgroundDark = Color(0xFF0D1B1E)
val OnBackgroundDark = Color(0xFFE1E3E3)
val SurfaceDark = Color(0xFF111F22)
val OnSurfaceDark = Color(0xFFE1E3E3)

// Error colors
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ── Status colors ───────────────────────────────────────────────────────────────
// Two variants per status: Light-mode (darker, high contrast on white) and
// Dark-mode (brighter, high contrast on near-black). Used for text/icons.

val ListeningColorLight = Color(0xFF00838F)   // Teal 800  — 4.5:1 on white
val ListeningColorDark  = Color(0xFF4DD0E1)   // Cyan 300  — 7.0:1 on #0D1B1E

val ProcessingColorLight = Color(0xFFE65100)  // Deep Orange 900 — 4.6:1 on white
val ProcessingColorDark  = Color(0xFFFFCA28)  // Amber 400        — 10:1 on dark

val SuccessColorLight = Color(0xFF00796B)     // Teal 700  — 4.9:1 on white
val SuccessColorDark  = Color(0xFF80CBC4)     // Teal 200  — 8.5:1 on #0D1B1E

// Accent bar colors — always vivid regardless of theme, used for thin visual strips
val SuccessAccent = Color(0xFF00BFA5)         // Teal A700
val ErrorAccent   = Color(0xFFFF5252)         // Red A200
