package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.calculation.QiblaCalculator
import com.example.ui.theme.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    userLat: Double,
    userLng: Double,
    cityName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val qiblaResult = remember(userLat, userLng) {
        QiblaCalculator.calculate(userLat, userLng)
    }

    var currentAzimuth by remember { mutableStateOf(0f) }
    var sensorAccuracy by remember { mutableStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasVibratedOnAlign by remember { mutableStateOf(false) }

    // Register sensor listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    var azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    azimuthDeg = (azimuthDeg + 360f) % 360f

                    // Low-pass filter for smooth needle movement
                    val alpha = 0.15f
                    currentAzimuth += alpha * (azimuthDeg - currentAzimuth)
                    sensorAccuracy = event.accuracy
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Relative angle between device heading and Kaaba
    val relativeAngle = (qiblaResult.bearingDegrees - currentAzimuth + 360f) % 360f
    val isAligned = abs(relativeAngle) < 3.5f || abs(relativeAngle - 360f) < 3.5f

    // Trigger haptic feedback once on alignment
    LaunchedEffect(isAligned) {
        if (isAligned && !hasVibratedOnAlign) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
            hasVibratedOnAlign = true
        } else if (!isAligned) {
            hasVibratedOnAlign = false
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -currentAzimuth,
        animationSpec = tween(120),
        label = "compass_rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.qibla_title), fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location and Target Bearing Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = cityName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = stringResource(R.string.qibla_bearing, qiblaResult.bearingDegrees.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.qibla_distance, qiblaResult.distanceKm),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = IslamicEmerald
                        )
                    }
                }
            }

            // Alignment Status Alert Banner
            val bannerBg by animateColorAsState(
                targetValue = if (isAligned) IslamicEmerald else IslamicEmerald.copy(alpha = 0.08f),
                label = "banner_bg"
            )
            val bannerTextColor by animateColorAsState(
                targetValue = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurface,
                label = "banner_text"
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bannerBg,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAligned) IslamicGold else IslamicGold.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = if (isAligned) IslamicGoldLight else IslamicEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAligned) stringResource(R.string.qibla_aligned)
                        else "قم بتدوير الهاتف حتى يتطابق المؤشر الذهبي مع الكعبة",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = bannerTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compass Dial Canvas
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(3.dp, if (isAligned) IslamicGold else IslamicGold.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Rotating dial with North/South/East/West markings
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedRotation)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 20f

                    // Cardinal tick lines
                    for (i in 0 until 360 step 30) {
                        val angleRad = Math.toRadians((i - 90).toDouble())
                        val isCardinal = i % 90 == 0
                        val tickLen = if (isCardinal) 24f else 12f
                        val start = Offset(
                            center.x + (radius - tickLen) * kotlin.math.cos(angleRad).toFloat(),
                            center.y + (radius - tickLen) * kotlin.math.sin(angleRad).toFloat()
                        )
                        val end = Offset(
                            center.x + radius * kotlin.math.cos(angleRad).toFloat(),
                            center.y + radius * kotlin.math.sin(angleRad).toFloat()
                        )
                        drawLine(
                            color = if (i == 0) Color.Red else Color.Gray.copy(alpha = 0.5f),
                            start = start,
                            end = end,
                            strokeWidth = if (isCardinal) 4f else 2f
                        )
                    }
                }

                // Kaaba Direction Needle (Points towards Makkah relative to phone heading)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedRotation + qiblaResult.bearingDegrees)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val needleLen = size.minDimension / 2 - 35f

                    // Golden Arrow pointing to Kaaba
                    val path = Path().apply {
                        moveTo(center.x, center.y - needleLen)
                        lineTo(center.x - 14f, center.y - 30f)
                        lineTo(center.x + 14f, center.y - 30f)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = IslamicGold
                    )

                    // Needle stem
                    drawLine(
                        color = IslamicGold,
                        start = center,
                        end = Offset(center.x, center.y - needleLen),
                        strokeWidth = 6f
                    )
                }

                // Center Kaaba Emblem
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isAligned) IslamicGold else IslamicEmerald)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = "Kaaba",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calibration Note
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = IslamicGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.qibla_calibrate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
