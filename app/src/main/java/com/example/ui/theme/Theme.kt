package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = OnForestPrimary,
    primaryContainer = SproutContainer,
    onPrimaryContainer = OnSproutContainer,
    secondary = SlateSecondary,
    onSecondary = OnSlateSecondary,
    secondaryContainer = MistContainer,
    onSecondaryContainer = OnMistContainer,
    tertiary = HarvestTertiary,
    onTertiary = OnHarvestTertiary,
    tertiaryContainer = WheatContainer,
    onTertiaryContainer = OnWheatContainer,
    background = AlabasterBackground,
    onBackground = OnAlabaster,
    surface = LinenSurface,
    onSurface = OnAlabaster,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = OnSageSurfaceVariant,
    outline = SageOutline,
    outlineVariant = SageOutlineVariant,
    error = TerracottaError,
    errorContainer = TerracottaContainer,
    onErrorContainer = OnTerracottaContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = MeadowPrimaryDark,
    onPrimary = OnMeadowDark,
    primaryContainer = ForestContainerDark,
    onPrimaryContainer = OnForestContainerDark,
    secondary = CoolSlateDark,
    onSecondary = OnCoolSlateDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = OnSlateContainerDark,
    tertiary = GoldenWheatDark,
    onTertiary = OnGoldenWheatDark,
    tertiaryContainer = WheatContainerDark,
    onTertiaryContainer = OnWheatContainerDark,
    background = CharcoalBackgroundDark,
    onBackground = OnCharcoalDark,
    surface = CharcoalSurfaceDark,
    onSurface = OnCharcoalDark,
    surfaceVariant = CharcoalSurfaceVariantDark,
    onSurfaceVariant = OnCharcoalVariantDark,
    outline = SageOutline,
    outlineVariant = CharcoalSurfaceVariantDark
)

val CultiveqShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CultiveqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CultiveqTypography,
        shapes = CultiveqShapes,
        content = content
    )
}
