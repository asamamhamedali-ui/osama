package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.calculation.AstronomicalPrayerCalculator
import com.example.data.local.UserPreferences
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.PrayerType
import com.example.service.AdhanAlarmScheduler
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

enum class AppScreen {
    HOME, QURAN, QIBLA, TASBIH, SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppRoot()
            }
        }
    }
}

@Composable
fun MainAppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(context) }

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var isDedicationDialogOpen by remember { mutableStateOf(false) }

    var schedule by remember {
        mutableStateOf(computeCurrentSchedule(prefs))
    }

    fun refreshSchedule() {
        schedule = computeCurrentSchedule(prefs)
        AdhanAlarmScheduler.scheduleDailyPrayers(context)
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                var detectedCity = "موقعي الحالي"
                                var detectedCountry = ""
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val addr = addresses[0]
                                        detectedCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي"
                                        detectedCountry = addr.countryName ?: ""
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                prefs.setLocation(loc.latitude, loc.longitude, detectedCity, detectedCountry)
                                withContext(Dispatchers.Main) {
                                    refreshSchedule()
                                }
                            }
                        }
                    }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AdhanAlarmScheduler.scheduleDailyPrayers(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Global Islamic Girih Pattern (Gold & Deep Emerald Green)
        Image(
            painter = painterResource(id = R.drawable.bg_girih_pattern),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle ambient tint for optimal contrast and readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PetrolDarkNavy.copy(alpha = 0.85f),
                            PetrolMidNavy.copy(alpha = 0.72f),
                            SoftAppBackground.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // Screen Body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    schedule = schedule,
                    onNavigateToQibla = { currentScreen = AppScreen.QIBLA },
                    onNavigateToQuran = { currentScreen = AppScreen.QURAN },
                    onNavigateToTasbih = { currentScreen = AppScreen.TASBIH },
                    onNavigateToAdhkar = { currentScreen = AppScreen.TASBIH },
                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                    onOpenDedication = { isDedicationDialogOpen = true },
                    onTogglePrayerNotification = { prayer, isEnabled ->
                        prefs.setPrayerNotificationEnabled(prayer, isEnabled)
                        refreshSchedule()
                    },
                    isNotificationEnabled = { prayer ->
                        prefs.isPrayerNotificationEnabled(prayer)
                    }
                )

                AppScreen.QURAN -> QuranScreen(
                    onBack = { currentScreen = AppScreen.HOME }
                )

                AppScreen.QIBLA -> QiblaScreen(
                    userLat = prefs.getLatitude(),
                    userLng = prefs.getLongitude(),
                    cityName = prefs.getCityName(),
                    onBack = { currentScreen = AppScreen.HOME }
                )

                AppScreen.TASBIH -> TasbihScreen(
                    onBack = { currentScreen = AppScreen.HOME }
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    prefs = prefs,
                    onBack = { currentScreen = AppScreen.HOME },
                    onOpenDedication = { isDedicationDialogOpen = true },
                    onRequestLocation = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        }

        // Custom Floating Bottom Navigation Bar (Matching screenshot 1 & 3)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = SurfaceCardWhite,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item 1: Home
                IconButton(onClick = { currentScreen = AppScreen.HOME }) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = if (currentScreen == AppScreen.HOME) PetrolDarkNavy else SubtitleGray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Item 2: Quran / Book
                IconButton(onClick = { currentScreen = AppScreen.QURAN }) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.QURAN) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                        contentDescription = "Quran",
                        tint = if (currentScreen == AppScreen.QURAN) PetrolDarkNavy else SubtitleGray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Center Item: Elevated Dark Navy Crescent Badge (Matching screenshot 1 & 3)
                Box(
                    modifier = Modifier
                        .offset(y = (-14).dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CenterPillNavy)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Opens the central Dedication modal or quick adhan
                            isDedicationDialogOpen = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = "Dedication & Night",
                        tint = CenterPillGold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Item 4: Tasbih / Adhkar
                IconButton(onClick = { currentScreen = AppScreen.TASBIH }) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.TASBIH) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Tasbih",
                        tint = if (currentScreen == AppScreen.TASBIH) PetrolDarkNavy else SubtitleGray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Item 5: Settings / Profile
                IconButton(onClick = { currentScreen = AppScreen.SETTINGS }) {
                    Icon(
                        imageVector = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Settings",
                        tint = if (currentScreen == AppScreen.SETTINGS) PetrolDarkNavy else SubtitleGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        if (isDedicationDialogOpen) {
            DedicationDialog(onDismiss = { isDedicationDialogOpen = false })
        }
    }
}

private fun computeCurrentSchedule(prefs: UserPreferences): DayPrayerSchedule {
    return AstronomicalPrayerCalculator.calculate(
        latitude = prefs.getLatitude(),
        longitude = prefs.getLongitude(),
        date = LocalDate.now(),
        zoneId = ZoneId.systemDefault(),
        method = prefs.getCalculationMethod(),
        school = prefs.getJuristicSchool(),
        fajrAdjustment = prefs.getPrayerAdjustment(PrayerType.FAJR),
        dhuhrAdjustment = prefs.getPrayerAdjustment(PrayerType.DHUHR),
        asrAdjustment = prefs.getPrayerAdjustment(PrayerType.ASR),
        maghribAdjustment = prefs.getPrayerAdjustment(PrayerType.MAGHRIB),
        ishaAdjustment = prefs.getPrayerAdjustment(PrayerType.ISHA),
        hijriDayOffset = prefs.getHijriOffset(),
        cityName = prefs.getCityName(),
        countryName = prefs.getCountryName()
    )
}
