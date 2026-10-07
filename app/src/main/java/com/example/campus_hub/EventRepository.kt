package com.example.campus_hub

import com.google.firebase.firestore.FirebaseFirestore

object EventRepository {

    val eventos = listOf(
        Event(
            1,
            "Semana Acadêmica de Tecnologia",
            "Evento com palestras, oficinas e atividades relacionadas à tecnologia e inovação.",
            "15/10/2026",
            "19:00",
            "Auditório Central",
            "Departamento de Tecnologia"
        ),
        Event(
            2,
            "Workshop de Programação",
            "Workshop prático sobre desenvolvimento de aplicações utilizando programação moderna.",
            "18/10/2026",
            "14:00",
            "Laboratório de Informática 01",
            "Curso de Computação"
        ),
        Event(
            3,
            "Palestra sobre Inteligência Artificial",
            "Palestra sobre aplicações, oportunidades e desafios da Inteligência Artificial.",
            "22/10/2026",
            "19:30",
            "Auditório Principal",
            "Universidade"
        ),
        Event(
            4,
            "Feira de Projetos Acadêmicos",
            "Apresentação de projetos desenvolvidos pelos alunos da universidade.",
            "25/10/2026",
            "09:00",
            "Centro de Eventos",
            "Universidade"
        ),
        Event(
            5,
            "Hackathon CampusHub",
            "Competição de desenvolvimento de soluções tecnológicas em equipe.",
            "30/10/2026",
            "08:00",
            "Laboratório de Tecnologia",
            "Núcleo de Inovação"
        )
    )

    private val firestore = FirebaseFirestore.getInstance()

    fun salvarEventosNoFirebase(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val batch = firestore.batch()

        eventos.forEach { evento ->

            val referencia = firestore
                .collection("eventos")
                .document(evento.id.toString())

            val dados = hashMapOf(
                "id" to evento.id,
                "titulo" to evento.titulo,
                "descricao" to evento.descricao,
                "data" to evento.data,
                "horario" to evento.horario,
                "local" to evento.local,
                "organizador" to evento.organizador
            )

            batch.set(referencia, dados)
        }

        batch.commit()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { erro ->
                onFailure(erro)
            }
    }

    fun carregarEventos(
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("eventos")
            .get()
            .addOnSuccessListener { resultado ->

                val lista = resultado.documents.mapNotNull { documento ->

                    val id = documento.getLong("id")?.toInt()
                        ?: documento.id.toIntOrNull()
                        ?: return@mapNotNull null

                    Event(
                        id,
                        documento.getString("titulo") ?: "",
                        documento.getString("descricao") ?: "",
                        documento.getString("data") ?: "",
                        documento.getString("horario") ?: "",
                        documento.getString("local") ?: "",
                        documento.getString("organizador") ?: ""
                    )
                }

                onSuccess(lista)
            }
            .addOnFailureListener { erro ->
                onFailure(erro)
            }
    }

    fun carregarEvento(
        id: Int,
        onSuccess: (Event?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("eventos")
            .document(id.toString())
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                val evento = Event(
                    documento.getLong("id")?.toInt() ?: id,
                    documento.getString("titulo") ?: "",
                    documento.getString("descricao") ?: "",
                    documento.getString("data") ?: "",
                    documento.getString("horario") ?: "",
                    documento.getString("local") ?: "",
                    documento.getString("organizador") ?: ""
                )

                onSuccess(evento)
            }
            .addOnFailureListener { erro ->
                onFailure(erro)
            }
    }
}