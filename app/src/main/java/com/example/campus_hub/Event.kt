package com.example.campus_hub

data class Event(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val data: String,
    val horario: String,
    val local: String,
    val organizador: String
)