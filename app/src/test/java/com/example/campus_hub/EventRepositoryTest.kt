package com.example.campus_hub

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EventRepositoryTest {

    @Before
    fun setUp() {
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        }
    }

    @Test
    fun testEventosListIsNotEmpty() {
        val eventos = EventRepository.eventos
        assertNotNull(eventos)
        assertFalse(eventos.isEmpty())
        assertTrue(eventos.size >= 5)
    }

    @Test
    fun testEventosHaveValidProperties() {
        val eventos = EventRepository.eventos
        for (event in eventos) {
            assertTrue(event.id > 0)
            assertFalse(event.titulo.isBlank())
            assertFalse(event.descricao.isBlank())
            assertFalse(event.data.isBlank())
            assertFalse(event.horario.isBlank())
            assertFalse(event.local.isBlank())
            assertFalse(event.organizador.isBlank())
        }
    }
}
