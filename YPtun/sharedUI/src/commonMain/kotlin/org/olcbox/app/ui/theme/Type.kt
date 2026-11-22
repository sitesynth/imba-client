package org.olcbox.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import multiplatform_app.sharedui.generated.resources.Res
import multiplatform_app.sharedui.generated.resources.imba_archivoblack_regular
import multiplatform_app.sharedui.generated.resources.imba_manrope_extrabold
import multiplatform_app.sharedui.generated.resources.imba_manrope_medium
import multiplatform_app.sharedui.generated.resources.imba_manrope_regular
import multiplatform_app.sharedui.generated.resources.imba_manrope_semibold
import multiplatform_app.sharedui.generated.resources.imba_unbounded_black
import multiplatform_app.sharedui.generated.resources.imba_unbounded_bold
import org.jetbrains.compose.resources.Font

/** IMBA cabinet type: Manrope for text, Unbounded for display headings, Archivo Black for the wordmark. */
/** Unbounded Black, the site's display face (the launch marquee, big headings). */
@Composable
fun imbaDisplayFamily(): FontFamily = FontFamily(Font(Res.font.imba_unbounded_black, weight = FontWeight.Black))

@Composable
fun imbaWordmarkFamily(): FontFamily = FontFamily(Font(Res.font.imba_archivoblack_regular, weight = FontWeight.Normal))

@Composable
fun getAppTypography(): Typography {
    val manrope = FontFamily(
        Font(Res.font.imba_manrope_regular, weight = FontWeight.Normal),
        Font(Res.font.imba_manrope_medium, weight = FontWeight.Medium),
        Font(Res.font.imba_manrope_semibold, weight = FontWeight.SemiBold),
        Font(Res.font.imba_manrope_extrabold, weight = FontWeight.ExtraBold),
        Font(Res.font.imba_manrope_extrabold, weight = FontWeight.Bold)
    )
    val unbounded = FontFamily(
        Font(Res.font.imba_unbounded_bold, weight = FontWeight.Bold),
        Font(Res.font.imba_unbounded_black, weight = FontWeight.Black),
        Font(Res.font.imba_unbounded_black, weight = FontWeight.ExtraBold),
        Font(Res.font.imba_unbounded_bold, weight = FontWeight.Normal),
        Font(Res.font.imba_unbounded_bold, weight = FontWeight.Medium),
        Font(Res.font.imba_unbounded_bold, weight = FontWeight.SemiBold)
    )
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.copy(fontFamily = unbounded, fontWeight = FontWeight.Black),
        displayMedium = d.displayMedium.copy(fontFamily = unbounded, fontWeight = FontWeight.Black),
        displaySmall = d.displaySmall.copy(fontFamily = unbounded, fontWeight = FontWeight.Black),
        headlineLarge = d.headlineLarge.copy(fontFamily = unbounded, fontWeight = FontWeight.Black),
        headlineMedium = d.headlineMedium.copy(fontFamily = unbounded, fontWeight = FontWeight.Black),
        headlineSmall = d.headlineSmall.copy(fontFamily = unbounded, fontWeight = FontWeight.Bold),
        titleLarge = d.titleLarge.copy(fontFamily = unbounded, fontWeight = FontWeight.Bold),
        titleMedium = d.titleMedium.copy(fontFamily = manrope, fontWeight = FontWeight.ExtraBold),
        titleSmall = d.titleSmall.copy(fontFamily = manrope, fontWeight = FontWeight.ExtraBold),
        bodyLarge = d.bodyLarge.copy(fontFamily = manrope, fontWeight = FontWeight.Medium),
        bodyMedium = d.bodyMedium.copy(fontFamily = manrope, fontWeight = FontWeight.Medium),
        bodySmall = d.bodySmall.copy(fontFamily = manrope, fontWeight = FontWeight.Medium),
        labelLarge = d.labelLarge.copy(fontFamily = manrope, fontWeight = FontWeight.ExtraBold),
        labelMedium = d.labelMedium.copy(fontFamily = manrope, fontWeight = FontWeight.ExtraBold),
        labelSmall = d.labelSmall.copy(fontFamily = manrope, fontWeight = FontWeight.Bold)
    )
}
