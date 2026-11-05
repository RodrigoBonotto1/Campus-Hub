package com.example.campus_hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EventTest {

    @Test
    fun testEventCreationAndProperties() {
        val event = Event(
            id = 1,
            titulo = "Semana Acadêmica",
            descricao = "Evento de tecnologia",
            data = "15/10/2026",
            horario = "19:00",
            local = "Auditório",
            organizador = "TI"
        )

        assertEquals(1, event.id)
        assertEquals("Semana Acadêmica", event.titulo)
        assertEquals("Evento de tecnologia", event.descricao)
        assertEquals("15/10/2026", event.data)
        assertEquals("19:00", event.horario)
        assertEquals("Auditório", event.local)
        assertEquals("TI", event.organizador)
        assertEquals("Outros", event.categoria) // default category
    }

    @Test
    fun testEventCustomCategory() {
        val event = Event(
            id = 2,
            titulo = "Workshop Kotlin",
            descricao = "Aprenda Kotlin",
            data = "16/10/2026",
            horario = "14:00",
            local = "Lab",
            organizador = "Comp",
            categoria = "Programação"
        )

        assertEquals("Programação", event.categoria)
    }

    @Test
    fun testEventCopyAndEquality() {
        val event1 = Event(1, "Titulo", "Desc", "Data", "Hora", "Local", "Org", "Cat")
        val event2 = event1.copy(titulo = "Novo Titulo")
        val event3 = event1.copy()

        assertEquals("Novo Titulo", event2.titulo)
        assertEquals(event1, event3)
        assertNotEquals(event1, event2)
    }
}
