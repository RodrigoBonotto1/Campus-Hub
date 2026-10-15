package com.example.campus_hub

import android.content.Intent
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
    private lateinit var btnFavorito: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var evento: Event? = null
    private var inscrito = false
    private var favorito = false

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
        btnFavorito = findViewById(R.id.btnFavorito)

        val eventId = intent.getIntExtra("EVENT_ID", -1)

        if (eventId == -1) {
            finish()
            return
        }

        carregarEvento(eventId)

        btnInscricao.setOnClickListener {
            if (inscrito) cancelarInscricao() else inscrever()
        }

        btnFavorito.setOnClickListener {
            if (favorito) desfavoritar() else favoritar()
        }
    }

    private fun carregarEvento(eventId: Int) {
        EventRepository.carregarEvento(
            eventId,
            onSuccess = { eventoCarregado ->
                if (eventoCarregado == null) {
                    Toast.makeText(this, "Evento não encontrado", Toast.LENGTH_LONG).show()
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
                verificarFavorito(eventoCarregado.id)
            },
            onFailure = { erro ->
                Toast.makeText(this, "Erro ao carregar evento: ${erro.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun referenciaUsuario() =
        auth.currentUser?.let {
            firestore.collection("usuarios").document(it.uid)
        }

    private fun verificarInscricao(eventId: Int) {
        val referencia = referenciaUsuario() ?: return

        referencia.collection("inscricoes")
            .document(eventId.toString())
            .get()
            .addOnSuccessListener {
                inscrito = it.exists()
                atualizarBotaoInscricao()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao verificar inscrição", Toast.LENGTH_LONG).show()
            }
    }

    private fun verificarFavorito(eventId: Int) {
        val referencia = referenciaUsuario() ?: return

        referencia.collection("favoritos")
            .document(eventId.toString())
            .get()
            .addOnSuccessListener {
                favorito = it.exists()
                atualizarBotaoFavorito()
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
        if (usuario == null || eventoAtual == null) return

        val dados = hashMapOf(
            "eventId" to eventoAtual.id,
            "titulo" to eventoAtual.titulo,
            "data" to eventoAtual.data,
            "horario" to eventoAtual.horario,
            "local" to eventoAtual.local
        )

        firestore.collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .document(eventoAtual.id.toString())
            .set(dados)
            .addOnSuccessListener {
                inscrito = true
                atualizarBotaoInscricao()
                Toast.makeText(this, "Inscrição realizada com sucesso!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao realizar inscrição: ${erro.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun cancelarInscricao() {
        val referencia = referenciaUsuario() ?: return
        val eventoAtual = evento ?: return

        referencia.collection("inscricoes")
            .document(eventoAtual.id.toString())
            .delete()
            .addOnSuccessListener {
                inscrito = false
                atualizarBotaoInscricao()
                Toast.makeText(this, "Inscrição cancelada", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao cancelar inscrição: ${erro.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun favoritar() {
        val referencia = referenciaUsuario()
        val eventoAtual = evento

        if (referencia == null || eventoAtual == null) {
            Toast.makeText(this, "Faça login para favoritar eventos", Toast.LENGTH_LONG).show()
            return
        }

        val dados = hashMapOf(
            "eventId" to eventoAtual.id,
            "titulo" to eventoAtual.titulo,
            "descricao" to eventoAtual.descricao,
            "data" to eventoAtual.data,
            "horario" to eventoAtual.horario,
            "local" to eventoAtual.local,
            "organizador" to eventoAtual.organizador
        )

        referencia.collection("favoritos")
            .document(eventoAtual.id.toString())
            .set(dados)
            .addOnSuccessListener {
                favorito = true
                atualizarBotaoFavorito()
                Toast.makeText(this, "Evento adicionado aos favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao favoritar: ${erro.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun desfavoritar() {
        val referencia = referenciaUsuario() ?: return
        val eventoAtual = evento ?: return

        referencia.collection("favoritos")
            .document(eventoAtual.id.toString())
            .delete()
            .addOnSuccessListener {
                favorito = false
                atualizarBotaoFavorito()
                Toast.makeText(this, "Evento removido dos favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao remover favorito: ${erro.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun atualizarBotaoInscricao() {
        btnInscricao.text = if (inscrito) "Cancelar inscrição" else "Inscrever-se"
    }

    private fun atualizarBotaoFavorito() {
        btnFavorito.text = if (favorito) "♥ Remover dos favoritos" else "♡ Adicionar aos favoritos"
    }
}