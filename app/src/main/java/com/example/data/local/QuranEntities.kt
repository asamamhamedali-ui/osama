package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String // Meccan / Medinan
)

@Entity(
    tableName = "ayahs",
    indices = [
        Index(value = ["surahNumber"]),
        Index(value = ["surahNumber", "numberInSurah"])
    ]
)
data class AyahEntity(
    @PrimaryKey val id: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
    val juz: Int,
    val textUthmani: String,
    val textTranslation: String,
    val audioUrl: String? = null
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameArabic: String,
    val timestamp: Long = System.currentTimeMillis()
)
