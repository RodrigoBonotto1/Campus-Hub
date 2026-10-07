package com.example.campus_hub

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailActivity : AppCompatActivity() {

    private lateinit var txtTitulo: TextView
    private lateinit var txtDescricao: TextView
    private lateinit var txtData: TextView
    private lateinit var txtHorario: TextView
    private lateinit var txtLocal: TextView
    private lateinit var txtOrganizador: TextView
    private lateinit var btnInscricao: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var evento: Event? = null
    private var inscrito = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        txtTitulo = findViewById(R.id.txtTitulo)
        txtDescricao = findViewById(R.id.txtDescricao)
        txtData = findViewById(R.id.txtData)
        txtHorario = findViewById(R.id.txtHorario)
        txtLocal = findViewById(R.id.txtLocal)
        txtOrganizador = findViewById(R.id.txtOrganizador)
        btnInscricao = findViewById(R.id.btnInscricao)

        val eventId = intent.getIntExtra("EVENT_ID", -1)

        if (eventId == -1) {
            finish()
            return
        }

        carregarEvento(eventId)

        btnInscricao.setOnClickListener {
            if (inscrito) {
                cancelarInscricao()
            } else {
                inscrever()
            }
        }
    }

    private fun carregarEvento(eventId: Int) {

        EventRepository.carregarEvento(
            eventId,
            onSuccess = { eventoCarregado ->

                if (eventoCarregado == null) {
                    Toast.makeText(
                        this,
                        "Evento não encontrado",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@carregarEvento
                }

                evento = eventoCarregado

                txtTitulo.text = eventoCarregado.titulo
                txtDescricao.text = eventoCarregado.descricao
                txtData.text = "Data: ${eventoCarregado.data}"
                txtHorario.text = "Horário: ${eventoCarregado.horario}"
                txtLocal.text = "Local: ${eventoCarregado.local}"
                txtOrganizador.text = "Organizador: ${eventoCarregado.organizador}"

                verificarInscricao(eventoCarregado.id)
            },
            onFailure = { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar evento: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun verificarInscricao(eventId: Int) {

        val usuario = auth.currentUser ?: return

        firestore
            .collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .document(eventId.toString())
            .get()
            .addOnSuccessListener { documento ->

                inscrito = documento.exists()

                atualizarBotao()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Erro ao verificar inscrição",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun inscrever() {

        val usuario = auth.currentUser
        val eventoAtual = evento

        if (usuario == null || eventoAtual == null) {
            return
        }

        val dados = hashMapOf(
            "eventId" to eventoAtual.id,
            "titulo" to eventoAtual.titulo,
            "data" to eventoAtual.data,
            "horario" to eventoAtual.horario,
            "local" to eventoAtual.local
        )

        firestore
            .collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .document(eventoAtual.id.toString())
            .set(dados)
            .addOnSuccessListener {

                inscrito = true
                atualizarBotao()

                Toast.makeText(
                    this,
                    "Inscrição realizada com sucesso!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { erro ->

                Toast.makeText(
                    this,
                    "Erro ao realizar inscrição: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun cancelarInscricao() {

        val usuario = auth.currentUser
        val eventoAtual = evento

        if (usuario == null || eventoAtual == null) {
            return
        }

        firestore
            .collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .document(eventoAtual.id.toString())
            .delete()
            .addOnSuccessListener {

                inscrito = false
                atualizarBotao()

                Toast.makeText(
                    this,
                    "Inscrição cancelada",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { erro ->

                Toast.makeText(
                    this,
                    "Erro ao cancelar inscrição: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun atualizarBotao() {

        if (inscrito) {
            btnInscricao.text = "Cancelar inscrição"
        } else {
            btnInscricao.text = "Inscrever-se"
        }
    }
}