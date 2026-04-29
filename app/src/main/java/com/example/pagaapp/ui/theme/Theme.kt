package com.example.pagaapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = IncomeGreen,
    tertiary = ExpenseRed,

    background = AppBackgroundDark,
    surface = CardBackgroundDark,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = IncomeGreen,
    tertiary = ExpenseRed,

    background = AppBackground,
    surface = CardBackground,

    onPrimary = CardBackground,
    onSecondary = CardBackground,
    onTertiary = CardBackground,

    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ExpenseRed,
    onError = CardBackground
)

@Composable
fun PagaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
