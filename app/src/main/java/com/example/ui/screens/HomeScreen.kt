package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerType
import com.example.data.model.SinglePrayerTime
import com.example.ui.components.DuaDedicationBanner
import com.example.ui.components.IslamicDomeHeader
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    schedule: DayPrayerSchedule,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToTasbih: () -> Unit,
    onNavigateToAdhkar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenDedication: () -> Unit,
    onTogglePrayerNotification: (PrayerType, Boolean) -> Unit,
    isNotificationEnabled: (PrayerType) -> Boolean
) {
    var currentTimeRemaining by remember(schedule) { mutableStateOf(schedule.timeRemainingMillis) }

    LaunchedEffect(schedule) {
        while (currentTimeRemaining > 0) {
            kotlinx.coroutines.delay(1000L)
            currentTimeRemaining = (currentTimeRemaining - 1000L).coerceAtLeast(0L)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Hero Header with Dome & Dial (Exact match to screenshot 1)
            item {
                IslamicDomeHeader(
                    schedule = schedule,
                    timeRemainingMillis = currentTimeRemaining,
                    onOpenSettings = onNavigateToSettings,
                    onOpenDedication = onOpenDedication
                )
            }

            // Dedicated Remembrance Banner
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    DuaDedicationBanner(onCardClick = onOpenDedication)
                }
            }

            // White rounded prayer cards list (Exact match to screenshot 1)
            items(schedule.prayers) { prayer ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    ScreenshotPrayerCard(
                        prayer = prayer,
                        isNotifOn = isNotificationEnabled(prayer.type),
                        onToggleNotif = { onTogglePrayerNotification(prayer.type, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenshotPrayerCard(
    prayer: SinglePrayerTime,
    isNotifOn: Boolean,
    onToggleNotif: (Boolean) -> Unit
) {
    val isNext = prayer.isNext
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm") }
    val amPmFormatter = remember { DateTimeFormatter.ofPattern("a", Locale("ar")) }

    val timeStr = prayer.time.format(timeFormatter)
    val amPmStr = prayer.time.format(amPmFormatter)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCardWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 3.dp else 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isNext) Modifier.border(1.5.dp, GoldenDomePrimary, RoundedCornerShape(18.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Golden Bell Icon Box (left side as in screenshot)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldenBellContainer)
                    .clickable { onToggleNotif(!isNotifOn) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isNotifOn) Icons.Default.Notifications else Icons.Outlined.NotificationsOff,
                    contentDescription = "Notification",
                    tint = if (isNotifOn) GoldenBellIcon else SubtitleGray,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Arabic Prayer Name & Time Text: e.g. "العشاء: 8:15 مساءً"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${prayer.type.displayNameAr}:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = DarkNavyText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$timeStr $amPmStr",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = DarkNavyText
                )
            }
        }
    }
}
