package com.example.avitotask.ui



import androidx.compose.ui.graphics.Color

// --- Палитра из Liquid Lava (остается для Темной темы) ---
val LiquidLava = Color(0xFFF56E0F)
val DarkVoid = Color(0xFF151419)
val GluonGrey = Color(0xFF1B1B1E)
val SlateGrey = Color(0xFF262626)
val DustyGrey = Color(0xFF878787)
val Snow = Color(0xFFFBFBFB)

// --- НОВЫЕ Цвета из палитры Meta (для Светлой темы) ---
val MetaPrimary = Color(0xFF0082FB) // Яркий синий - новый акцент
val MetaPrimaryDark = Color(0xFF0064E0) // Темный синий
val MetaBackgroundLight = Color(0xFFF1F5F8) // Почти белый фон
val MetaBackgroundDark = Color(0xFF1C2B33) // Очень темный фон/поверхность

// --- Цвета для ТЕМНОЙ СХЕМЫ (Используется Liquid Lava) ---
val DarkPrimary = LiquidLava
val DarkOnPrimary = Snow
val DarkSecondary = DustyGrey
val DarkBackground = DarkVoid
val DarkSurface = GluonGrey
val DarkOnBackground = Snow
val DarkOnSurface = Snow
val DarkError = Color(0xFFFFB4AB)

// --- Цвета для СВЕТЛОЙ СХЕМЫ (Используется Meta Palette) ---
val LightPrimary = MetaPrimary // <-- ИСПОЛЬЗУЕМ ЯРКИЙ СИНИЙ
val LightOnPrimary = Color.White // Текст на синем фоне
val LightSecondary = MetaPrimaryDark // Темный синий для вторичных элементов
val LightBackground = MetaBackgroundLight // Почти белый
val LightSurface = Color.White // Чистый белый для контраста
val LightOnBackground = MetaBackgroundDark // Темный текст на светлом фоне
val LightOnSurface = MetaBackgroundDark
val LightError = Color(0xFFBA1A1A)