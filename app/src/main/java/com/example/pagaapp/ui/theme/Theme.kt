package com.example.pagaapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}