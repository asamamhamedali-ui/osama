package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun app_identity_constants() {
        val appName = "قُلْ لِي لِمَاذَا لَا تُصَلِّي يَا نَاسِي الصَّلَوَاتِ قُلْ لِي"
        assertEquals(true, appName.contains("تُصَلِّي"))
        assertEquals(true, appName.contains("الصَّلَوَاتِ"))
    }
}
