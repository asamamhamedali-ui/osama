package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserPreferences
import com.example.data.model.CalculationMethod
import com.example.data.model.JuristicSchool
import com.example.service.AdhanAlarmScheduler
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: UserPreferences,
    onBack: () -> Unit,
    onOpenDedication: () -> Unit,
    onRequestLocation: () -> Unit
) {
    val context = LocalContext.current
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.release()
            previewPlayer = null
        }
    }

    var selectedMethod by remember { mutableStateOf(prefs.getCalculationMethod()) }
    var selectedSchool by remember { mutableStateOf(prefs.getJuristicSchool()) }
    var selectedMuezzin by remember { mutableStateOf(prefs.getMuezzinSound()) }

    var isMethodDialogVisible by remember { mutableStateOf(false) }
    var isMuezzinDialogVisible by remember { mutableStateOf(false) }
    var isCityDialogVisible by remember { mutableStateOf(false) }
    var isBatteryDialogVisible by remember { mutableStateOf(false) }

    var adhanEnabledToggle by remember { mutableStateOf(true) }
    var fullScreenAlarmToggle by remember { mutableStateOf(true) }
    var preAlarmToggle by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإعدادات",
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
                .background(SoftAppBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dedication Reminder
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDedication() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = GoldenDomePrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "عن التطبيق والإهداء",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Text(
                                text = "صدقة جارية لروح جدتي رحمها الله",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldenDomePrimary
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GoldenDomePrimary)
                }
            }

            // Section 1: إعدادات الأذان (Matching screenshot 2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "إعدادات الأذان",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavyText,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Switch 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إعدادات الأذان",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Switch(
                                checked = adhanEnabledToggle,
                                onCheckedChange = { adhanEnabledToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PetrolDarkNavy
                                )
                            )
                        }

                        Divider(color = SoftAppBackground)

                        // Switch 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صوت المؤذن",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Switch(
                                checked = fullScreenAlarmToggle,
                                onCheckedChange = { fullScreenAlarmToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PetrolDarkNavy
                                )
                            )
                        }

                        // Dropdown selection box (matching screenshot 2)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftAppBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMuezzinDialogVisible = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MutedNavyText)
                                Text(
                                    text = when (selectedMuezzin) {
                                        "madinah" -> "المدينة المنورة"
                                        "fajr" -> "أذان الفجر المميز"
                                        "takbeer" -> "تكبيرات فقط"
                                        else -> "مكة المكرمة"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkNavyText
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: طريقة الحساب (Matching screenshot 2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "طريقة الحساب",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavyText,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftAppBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMethodDialogVisible = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MutedNavyText)
                                Text(
                                    text = selectedMethod.titleAr,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkNavyText
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftAppBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCityDialogVisible = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MutedNavyText)
                                Text(
                                    text = "${prefs.getCityName()} (${prefs.getCountryName()})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkNavyText
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: المنبهات والبطارية (Matching screenshot 2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "المنبهات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavyText,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "انطلاق المواد الأذان",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Switch(
                                checked = true,
                                onCheckedChange = {},
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PetrolDarkNavy
                                )
                            )
                        }

                        Divider(color = SoftAppBackground)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "انطلاق المؤذن الإسلامي",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkNavyText
                            )
                            Switch(
                                checked = preAlarmToggle,
                                onCheckedChange = { preAlarmToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PetrolDarkNavy
                                )
                            )
                        }

                        Divider(color = SoftAppBackground)

                        Button(
                            onClick = { isBatteryDialogVisible = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PetrolDarkNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تعليمات دقة الأذان واستثناء البطارية", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Method Dialog
    if (isMethodDialogVisible) {
        AlertDialog(
            onDismissRequest = { isMethodDialogVisible = false },
            title = { Text("طريقة الحساب") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMethod = method
                                    prefs.setCalculationMethod(method)
                                    AdhanAlarmScheduler.scheduleDailyPrayers(context)
                                    isMethodDialogVisible = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedMethod == method, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method.titleAr, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isMethodDialogVisible = false }) { Text("إغلاق") }
            }
        )
    }

    // Muezzin Dialog
    if (isMuezzinDialogVisible) {
        AlertDialog(
            onDismissRequest = { isMuezzinDialogVisible = false },
            title = { Text("صوت المؤذن") },
            text = {
                val voices = listOf(
                    "makkah" to "مكة المكرمة",
                    "madinah" to "المدينة المنورة",
                    "fajr" to "أذان الفجر المميز",
                    "takbeer" to "تكبيرات فقط"
                )
                Column {
                    voices.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMuezzin = key
                                    prefs.setMuezzinSound(key)
                                    isMuezzinDialogVisible = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedMuezzin == key, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isMuezzinDialogVisible = false }) { Text("إغلاق") }
            }
        )
    }

    // City Selection Dialog
    if (isCityDialogVisible) {
        val popularCities = listOf(
            Triple("مكة المكرمة", "السعودية", Pair(21.4225, 39.8262)),
            Triple("المدينة المنورة", "السعودية", Pair(24.4672, 39.6111)),
            Triple("القاهرة", "مصر", Pair(30.0444, 31.2357)),
            Triple("الرياض", "السعودية", Pair(24.7136, 46.6753)),
            Triple("دبي", "الإمارات", Pair(25.2048, 55.2708)),
            Triple("عَمّان", "الأردن", Pair(31.9539, 35.9106)),
            Triple("القدس الشريف", "فلسطين", Pair(31.7683, 35.2137)),
            Triple("إسطنبول", "تركيا", Pair(41.0082, 28.9784))
        )
        AlertDialog(
            onDismissRequest = { isCityDialogVisible = false },
            title = { Text("اختيار المدينة") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Button(
                        onClick = {
                            onRequestLocation()
                            isCityDialogVisible = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PetrolDarkNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحديد موقعي التلقائي عبر GPS")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    popularCities.forEach { (city, country, coords) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.setLocation(coords.first, coords.second, city, country)
                                    AdhanAlarmScheduler.scheduleDailyPrayers(context)
                                    isCityDialogVisible = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(city, fontWeight = FontWeight.Bold)
                            Text(country, color = MutedNavyText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isCityDialogVisible = false }) { Text("إلغاء") }
            }
        )
    }

    // Battery Optimization Dialog
    if (isBatteryDialogVisible) {
        AlertDialog(
            onDismissRequest = { isBatteryDialogVisible = false },
            title = { Text("ضمان دقة الأذان بالخلفية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. استثنِ التطبيق من قيود توفير طاقة البطارية.")
                    Text("2. فعّل التشغيل التلقائي (Autostart) في هواتف شاومي وهواوي وسامسونج.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                    isBatteryDialogVisible = false
                }) {
                    Text("فتح إعدادات البطارية")
                }
            },
            dismissButton = {
                TextButton(onClick = { isBatteryDialogVisible = false }) { Text("تم") }
            }
        )
    }
}
