package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.AyahEntity
import com.example.data.local.OfflineQuranData
import com.example.data.local.QuranIndexData
import com.example.data.local.SurahEntity
import com.example.data.local.UserPreferences
import com.example.data.remote.ApiClient
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(context) }
    val db = remember { AppDatabase.getDatabase(context) }

    var selectedSurah by remember { mutableStateOf<SurahEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Surahs, 1: Juz

    val allSurahs = remember { QuranIndexData.surahs }
    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) allSurahs
        else allSurahs.filter {
            it.nameArabic.contains(searchQuery) ||
                    it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                    it.number.toString() == searchQuery.trim()
        }
    }

    if (selectedSurah != null) {
        SurahReaderView(
            surah = selectedSurah!!,
            prefs = prefs,
            db = db,
            onBack = { selectedSurah = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.quran_title), fontWeight = FontWeight.Bold, color = Color.White) },
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
                    .padding(horizontal = 16.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = stringResource(R.string.search_surah)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslamicGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Surahs / Juz
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = IslamicGold
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(text = stringResource(R.string.surahs), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(text = stringResource(R.string.juz), fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSurahs) { surah ->
                            SurahListItem(
                                surah = surah,
                                onClick = { selectedSurah = surah }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                } else {
                    // Juz list (1-30)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(30) { index ->
                            val juzNumber = index + 1
                            JuzListItem(
                                juzNumber = juzNumber,
                                onClick = {
                                    // Open the first surah that corresponds to this juz approximately
                                    val targetSurahNumber = when (juzNumber) {
                                        1 -> 1; 2 -> 2; 3 -> 2; 4 -> 3; 5 -> 4; 6 -> 4; 7 -> 5; 8 -> 6; 9 -> 7; 10 -> 8;
                                        11 -> 9; 12 -> 11; 13 -> 12; 14 -> 15; 15 -> 17; 16 -> 18; 17 -> 21; 18 -> 23;
                                        19 -> 25; 20 -> 27; 21 -> 29; 22 -> 33; 23 -> 36; 24 -> 39; 25 -> 41; 26 -> 46;
                                        27 -> 51; 28 -> 58; 29 -> 67; 30 -> 78; else -> 1
                                    }
                                    selectedSurah = allSurahs.firstOrNull { it.number == targetSurahNumber }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItem(
    surah: SurahEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Surah Number badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(IslamicEmerald.copy(alpha = 0.1f))
                        .border(1.dp, IslamicGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = IslamicEmerald
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = surah.nameEnglish,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${surah.englishTranslation} • ${surah.numberOfAyahs} verses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = IslamicGold
                )
                Text(
                    text = if (surah.revelationType == "Meccan") "مكية" else "مدنية",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun JuzListItem(
    juzNumber: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(IslamicGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$juzNumber",
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "الجزء $juzNumber • Juz $juzNumber",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IslamicGold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderView(
    surah: SurahEntity,
    prefs: UserPreferences,
    db: AppDatabase,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var ayahs by remember { mutableStateOf<List<AyahEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var fontSize by remember { mutableFloatStateOf(prefs.getQuranFontSize()) }
    var showTranslation by remember { mutableStateOf(prefs.getShowTranslation()) }

    var isPlayingAudio by remember { mutableStateOf(false) }
    var activePlayingAyah by remember { mutableIntStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Clean up media player when exiting reader
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Load ayahs from Room DB or fetch from AlQuran Cloud
    LaunchedEffect(surah.number) {
        isLoading = true
        withContext(Dispatchers.IO) {
            // First check local DB
            val localAyahs = OfflineQuranData.initialAyahs.filter { it.surahNumber == surah.number }
            if (localAyahs.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    ayahs = localAyahs
                    isLoading = false
                }
            }

            try {
                val response = ApiClient.quranApi.getSurahEditions(surah.number)
                val editions = response.data
                if (editions != null && editions.size >= 2) {
                    val arabicEdition = editions[0]
                    val englishEdition = editions.getOrNull(1)
                    val audioEdition = editions.getOrNull(2)

                    val fetched = arabicEdition.ayahs.mapIndexed { idx, remoteAyah ->
                        AyahEntity(
                            id = (surah.number * 1000) + remoteAyah.numberInSurah,
                            surahNumber = surah.number,
                            numberInSurah = remoteAyah.numberInSurah,
                            juz = remoteAyah.juz,
                            textUthmani = remoteAyah.text,
                            textTranslation = englishEdition?.ayahs?.getOrNull(idx)?.text ?: "",
                            audioUrl = audioEdition?.ayahs?.getOrNull(idx)?.audio
                                ?: "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${remoteAyah.number}.mp3"
                        )
                    }

                    db.quranDao().insertAyahs(fetched)
                    withContext(Dispatchers.Main) {
                        ayahs = fetched
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                // If offline and wasn't in initialAyahs, generate baseline placeholder ayahs so reader never crashes
                if (ayahs.isEmpty()) {
                    val fallback = (1..surah.numberOfAyahs).map { num ->
                        AyahEntity(
                            id = (surah.number * 1000) + num,
                            surahNumber = surah.number,
                            numberInSurah = num,
                            juz = 1,
                            textUthmani = "سورة ${surah.nameArabic} - الآية $num",
                            textTranslation = "Surah ${surah.nameEnglish} - Verse $num",
                            audioUrl = null
                        )
                    }
                    withContext(Dispatchers.Main) {
                        ayahs = fallback
                        isLoading = false
                    }
                }
            }
        }
    }

    fun playAyahAudio(ayah: AyahEntity) {
        val url = ayah.audioUrl ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    isPlayingAudio = true
                    activePlayingAyah = ayah.numberInSurah
                }
                setOnCompletionListener {
                    isPlayingAudio = false
                    activePlayingAyah = -1
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر تشغيل الصوت بدون اتصال بالإنترنت", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سورة ${surah.nameArabic}",
                            fontWeight = FontWeight.Bold,
                            color = IslamicGold
                        )
                        Text(
                            text = "${surah.nameEnglish} • ${surah.numberOfAyahs} آيات",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Font size adjustments
                    IconButton(onClick = {
                        if (fontSize > 16f) {
                            fontSize -= 2f
                            prefs.setQuranFontSize(fontSize)
                        }
                    }) {
                        Text("A-", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = {
                        if (fontSize < 38f) {
                            fontSize += 2f
                            prefs.setQuranFontSize(fontSize)
                        }
                    }) {
                        Text("A+", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = {
                        showTranslation = !showTranslation
                        prefs.setShowTranslation(showTranslation)
                    }) {
                        Icon(
                            imageVector = if (showTranslation) Icons.Default.Translate else Icons.Default.TextFields,
                            contentDescription = "Toggle Translation",
                            tint = if (showTranslation) IslamicGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IslamicGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bismillah Header (except for Surah At-Tawbah 9)
                if (surah.number != 9 && surah.number != 1) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (fontSize + 2).sp
                                ),
                                color = IslamicGold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(ayahs) { ayah ->
                    val isPlaying = activePlayingAyah == ayah.numberInSurah
                    AyahCard(
                        ayah = ayah,
                        fontSize = fontSize,
                        showTranslation = showTranslation,
                        isPlaying = isPlaying,
                        onPlayAudio = { playAyahAudio(ayah) },
                        onShare = {
                            val shareText = "${ayah.textUthmani}\n\n[سورة ${surah.nameArabic} - الآية ${ayah.numberInSurah}]"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة الآية"))
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: AyahEntity,
    fontSize: Float,
    showTranslation: Boolean,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) IslamicEmerald.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(1.5.dp, IslamicGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ayah Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verse circle ornament
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(IslamicGold.copy(alpha = 0.15f))
                        .border(1.dp, IslamicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.numberInSurah}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = IslamicGold
                    )
                }

                Row {
                    if (ayah.audioUrl != null) {
                        IconButton(onClick = onPlayAudio, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "Play Audio",
                                tint = if (isPlaying) IslamicGold else IslamicEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Arabic Quran Uthmani Text
            Text(
                text = ayah.textUthmani,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.8).sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Optional English / Localized Translation
            if (showTranslation && ayah.textTranslation.isNotBlank()) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    text = ayah.textTranslation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (fontSize * 0.65f).coerceAtLeast(13f).sp,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
