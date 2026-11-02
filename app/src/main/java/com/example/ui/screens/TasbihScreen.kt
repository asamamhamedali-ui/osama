package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val presetDhikr = listOf(
        "سبحان الله",
        "الحمد لله",
        "الله أكبر",
        "لا إله إلا الله",
        "أستغفر الله وأتوب إليه",
        "اللهم صل وسلم على نبينا محمد",
        "اللهم اغفر لجدتي وارحمها"
    )

    var selectedDhikrIndex by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(27) }
    var lap by remember { mutableIntStateOf(1) }
    var isPressed by remember { mutableStateOf(false) }

    fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(35)
        }
    }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        label = "bead_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الأذكار والمسبحة",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PetrolDarkNavy
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PetrolDarkNavy,
                            PetrolMidNavy,
                            SoftAppBackground
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        ) {
            // Quick Adhkar Cards at the top (matching screenshot 3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "الأذكار",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Morning Adhkar card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldenBellContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GoldenBellIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "أذكار الصباح",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Text(
                                text = "أذكار الحفظ والبركة في الصباح",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedNavyText
                            )
                        }
                    }
                }

                // Evening Adhkar card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldenBellContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GoldenBellIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "أذكار المساء",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Text(
                                text = "أذكار الطمأنينة والسكينة في المساء",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedNavyText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // White Rounded Bottom Sheet for Tasbih (Matching screenshot 3)
            Card(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Drag handle pill
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SubtitleGray.copy(alpha = 0.5f))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "مسبحة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MutedNavyText
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bead string row with 3D Golden Bead in the center (Matching screenshot 3)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left background beads
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(GoldenDomeHighlight.copy(alpha = 0.4f))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // Center 3D Golden Bead Sphere (Tap Target)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(scaleAnim)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        count++
                                        triggerVibration()
                                        if (count >= 33) {
                                            count = 0
                                            lap++
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val r = size.minDimension / 2f
                                    val center = Offset(size.width / 2f, size.height / 2f)

                                    // Outer golden soft ambient glow
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                AmbientGoldGlow.copy(alpha = 0.5f),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = r * 1.25f,
                                        center = center
                                    )

                                    // 3D Sphere shading
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                GoldenDomeHighlight,
                                                GoldenDomePrimary,
                                                GoldenDomeDark
                                            ),
                                            center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                                            radius = r * 0.95f
                                        ),
                                        radius = r * 0.9f,
                                        center = center
                                    )
                                }
                            }

                            // Right background beads
                            repeat(3) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(GoldenDomeHighlight.copy(alpha = 0.4f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dhikr Title: "سبحان الله"
                        Text(
                            text = presetDhikr[selectedDhikrIndex],
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = DarkNavyText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                selectedDhikrIndex = (selectedDhikrIndex + 1) % presetDhikr.size
                                count = 0
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bold Number: "27"
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 44.sp
                            ),
                            color = DarkNavyText
                        )
                    }

                    // Bottom "إعادة ضبط" button (Matching screenshot 3)
                    Button(
                        onClick = {
                            count = 0
                            lap = 1
                            triggerVibration()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PetrolDarkNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "إعادة ضبط",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
