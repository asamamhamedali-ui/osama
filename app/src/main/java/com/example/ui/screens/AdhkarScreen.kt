package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight

data class DhikrItem(
    val arabicText: String,
    val translation: String,
    val targetCount: Int,
    val virtue: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    var selectedTab by remember { mutableIntStateOf(0) }

    val morningAdhkar = remember {
        listOf(
            DhikrItem(
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "We have reached the morning and at this very time unto Allah belongs all sovereignty, and all praise is for Allah.",
                1,
                "رواه مسلم"
            ),
            DhikrItem(
                "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ ، خَلَقْتَنِي وَأَنَا عَبْدُكَ ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لا يَغْفِرُ الذُّنُوبَ إِلا أَنْتَ",
                "سيد الاستغفار: من قالها موقناً بها حين يمسي فمات من ليلته دخل الجنة، وكذلك إذا أصبح",
                1,
                "سيد الاستغفار - صحيح البخاري"
            ),
            DhikrItem(
                "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ العَلِيمُ",
                "لم يضره شيء حتى يمسي أو يصبح",
                3,
                "رواه أبو داود والترمذي"
            ),
            DhikrItem(
                "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالْإِسْلَامِ دِينَاً، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيَّاً",
                "من قالها حين يصبح وحين يمسي كان حقاً على الله أن يرضيه يوم القيامة",
                3,
                "رواه أحمد وأبو داود"
            ),
            DhikrItem(
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي",
                "دعاء العافية وحفظ العبد من كل جانب",
                1,
                "رواه أبو داود وابن ماجه"
            ),
            DhikrItem(
                "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
                "تعدل ساعات طويلة من الذكر",
                3,
                "صحيح مسلم"
            )
        )
    }

    val eveningAdhkar = remember {
        listOf(
            DhikrItem(
                "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "We have reached the evening and at this very time unto Allah belongs all sovereignty.",
                1,
                "رواه مسلم"
            ),
            DhikrItem(
                "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                "من قالها حين يمسي ثلاث مرات لم تضره حمة تلك الليلة",
                3,
                "صحيح مسلم"
            ),
            DhikrItem(
                "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ العَلِيمُ",
                "لم يضره شيء حتى يصبح",
                3,
                "رواه الترمذي"
            ),
            DhikrItem(
                "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
                "ذكر المساء المبارك",
                1,
                "رواه أبو داود"
            )
        )
    }

    val afterPrayerAdhkar = remember {
        listOf(
            DhikrItem(
                "أَسْتَغْفِرُ اللَّهَ (ثَلاثاً)، اللَّهُمَّ أَنْتَ السَّلامُ وَمِنْكَ السَّلامُ، تَبَارَكْتَ يَا ذَا الْجَلالِ وَالإِكْرَامِ",
                "تقال دبر كل صلاة مكتوبة",
                1,
                "صحيح مسلم"
            ),
            DhikrItem(
                "لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لا حَوْلَ وَلا قُوَّةَ إِلا بِاللَّهِ",
                "ذكر ما بعد الصلاة",
                1,
                "صحيح مسلم"
            ),
            DhikrItem(
                "سُبْحَانَ اللَّهِ (33)، الْحَمْدُ لِلَّهِ (33)، اللَّهُ أَكْبَرُ (33)، وتَمام المائة: لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ...",
                "غفرت خطاياه وإن كانت مثل زبد البحر",
                33,
                "صحيح مسلم"
            ),
            DhikrItem(
                "آية الكرسي (اللَّهُ لا إِلَهَ إِلا هُوَ الْحَيُّ الْقَيُّومُ...)",
                "من قرأها دبر كل صلاة مكتوبة لم يمنعه من دخول الجنة إلا أن يموت",
                1,
                "رواه النسائي وصححه الألباني"
            )
        )
    }

    val currentList = when (selectedTab) {
        0 -> morningAdhkar
        1 -> eveningAdhkar
        else -> afterPrayerAdhkar
    }

    // Dynamic state map for counters
    val counterState = remember(selectedTab) { mutableStateMapOf<Int, Int>() }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.adhkar_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            // Category Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = IslamicGold
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(text = "الصباح", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(text = "المساء", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(text = "بعد الصلاة", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(currentList) { index, item ->
                    val count = counterState.getOrDefault(index, 0)
                    val isFinished = count >= item.targetCount

                    DhikrCard(
                        item = item,
                        currentCount = count,
                        isFinished = isFinished,
                        onIncrement = {
                            if (!isFinished) {
                                counterState[index] = count + 1
                                vibrate()
                            }
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
fun DhikrCard(
    item: DhikrItem,
    currentCount: Int,
    isFinished: Boolean,
    onIncrement: () -> Unit
) {
    val cardBg by animateColorAsState(
        targetValue = if (isFinished) IslamicEmerald.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface,
        label = "dhikr_card_bg"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = if (isFinished) androidx.compose.foundation.BorderStroke(1.5.dp, IslamicGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIncrement() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Arabic Text
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Virtue Note
            if (item.virtue.isNotBlank()) {
                Text(
                    text = "• ${item.virtue}",
                    style = MaterialTheme.typography.labelSmall,
                    color = IslamicGold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Action / Counter Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFinished) "تم الانتهاء" else "اضغط على البطاقة للتسبيح",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFinished) IslamicEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Counter Badge Button
                Surface(
                    color = if (isFinished) IslamicEmerald else IslamicGold,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFinished) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "$currentCount / ${item.targetCount}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
