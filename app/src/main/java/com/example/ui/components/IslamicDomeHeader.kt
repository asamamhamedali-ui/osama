package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DayPrayerSchedule
import com.example.ui.theme.*

@Composable
fun IslamicDomeHeader(
    schedule: DayPrayerSchedule,
    timeRemainingMillis: Long,
    onOpenSettings: () -> Unit,
    onOpenDedication: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextPrayer = schedule.nextPrayer
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("h:mm") }
    val amPmFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("a", java.util.Locale("ar")) }

    val prayerTimeString = nextPrayer.time.format(timeFormatter)
    val prayerAmPm = nextPrayer.time.format(amPmFormatter)

    val hours = (timeRemainingMillis / (1000 * 60 * 60)) % 24
    val minutes = (timeRemainingMillis / (1000 * 60)) % 60
    val seconds = (timeRemainingMillis / 1000) % 60

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(390.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PetrolHeaderGradStart,
                        PetrolMidNavy,
                        PetrolDarkNavy
                    )
                )
            )
    ) {
        // Islamic geometric stars watermark & ambient golden radial glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center radial ambient glow behind the prayer circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmbientGoldGlow.copy(alpha = 0.22f),
                        AmbientGoldGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h * 0.36f),
                    radius = w * 0.45f
                ),
                center = Offset(w / 2f, h * 0.36f),
                radius = w * 0.45f
            )

            // Draw subtle Islamic geometric octagonal stars in the background
            val starColor = Color(0x1872A4BA)
            fun drawIslamicStar(cx: Float, cy: Float, radius: Float) {
                val p1 = Path().apply {
                    for (i in 0 until 8) {
                        val angle = Math.toRadians((i * 45).toDouble())
                        val r = if (i % 2 == 0) radius else radius * 0.72f
                        val x = cx + r * kotlin.math.cos(angle).toFloat()
                        val y = cy + r * kotlin.math.sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(p1, starColor, style = Stroke(width = 1.2f))
            }

            drawIslamicStar(w * 0.12f, h * 0.18f, 36f)
            drawIslamicStar(w * 0.88f, h * 0.18f, 42f)
            drawIslamicStar(w * 0.08f, h * 0.42f, 28f)
            drawIslamicStar(w * 0.92f, h * 0.45f, 32f)

            // Bottom White Concave Arch framing the lower section
            val archPath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h - 35f)
                cubicTo(
                    w * 0.25f, h - 55f,
                    w * 0.75f, h - 55f,
                    w, h - 35f
                )
                lineTo(w, h)
                close()
            }
            drawPath(archPath, SoftAppBackground)

            // Delicate golden border along the concave curve
            val borderPath = Path().apply {
                moveTo(0f, h - 35f)
                cubicTo(
                    w * 0.25f, h - 55f,
                    w * 0.75f, h - 55f,
                    w, h - 35f
                )
            }
            drawPath(
                borderPath,
                Brush.horizontalGradient(
                    listOf(
                        GoldenDomePrimary.copy(alpha = 0.3f),
                        GoldenDomeHighlight,
                        GoldenDomePrimary.copy(alpha = 0.3f)
                    )
                ),
                style = Stroke(width = 2.5f)
            )

            // Golden Mosque Dome Silhouette in the center
            val domeWidth = w * 0.36f
            val domeCx = w / 2f
            val domeBaseY = h - 18f
            val domePeakY = h - 110f

            val domePath = Path().apply {
                moveTo(domeCx - domeWidth / 2f, domeBaseY)
                // Left dome curve to peak
                cubicTo(
                    domeCx - domeWidth * 0.48f, domeBaseY - 45f,
                    domeCx - domeWidth * 0.22f, domePeakY + 15f,
                    domeCx, domePeakY
                )
                // Right dome curve to base
                cubicTo(
                    domeCx + domeWidth * 0.22f, domePeakY + 15f,
                    domeCx + domeWidth * 0.48f, domeBaseY - 45f,
                    domeCx + domeWidth / 2f, domeBaseY
                )
                close()
            }

            drawPath(
                domePath,
                Brush.verticalGradient(
                    colors = listOf(
                        GoldenDomeHighlight,
                        GoldenDomePrimary,
                        GoldenDomeDark
                    ),
                    startY = domePeakY,
                    endY = domeBaseY
                )
            )

            // Crescent Spire at the top of the dome
            drawLine(
                color = GoldenDomeHighlight,
                start = Offset(domeCx, domePeakY),
                end = Offset(domeCx, domePeakY - 18f),
                strokeWidth = 3f
            )
            drawCircle(
                color = GoldenDomeHighlight,
                radius = 3.5f,
                center = Offset(domeCx, domePeakY - 18f)
            )
        }

        // Top Controls: Settings & Dedication
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDedication,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Dedication",
                    tint = GoldenDomeHighlight,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dedication pill label
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { onOpenDedication() }
            ) {
                Text(
                    text = "صدقة جارية لروح جدتي رحمها الله",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = GoldenDomeHighlight,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Content Column inside header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Golden Dial (Matching screenshot exactly)
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PetrolMidNavy.copy(alpha = 0.9f),
                                PetrolDarkNavy
                            )
                        )
                    )
                    .border(
                        width = 3.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                GoldenDomeHighlight,
                                GoldenDomePrimary,
                                GoldenDomeDark,
                                GoldenDomeHighlight
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الحين الأذان",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = GoldenDomeHighlight
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = prayerTimeString,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp
                        ),
                        color = Color.White
                    )

                    Text(
                        text = prayerAmPm,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text: الصلوات القادمة / المغرب: 6:45 مساءً
            Text(
                text = "الصلوات القادمة",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${nextPrayer.type.displayNameAr}: $prayerTimeString $prayerAmPm (باقي ${String.format("%02d:%02d:%02d", hours, minutes, seconds)})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sleek Golden Progress Bar (matching screenshot)
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    GoldenDomePrimary,
                                    GoldenDomeHighlight
                                )
                            )
                        )
                )
            }
        }
    }
}
