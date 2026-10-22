package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Spinner
import android.widget.ArrayAdapter
import java.util.Calendar

class EventDetailActivity : AppCompatActivity() {

    private lateinit var txtTitulo: TextView
    private lateinit var txtDescricao: TextView
    private lateinit var txtData: TextView
    private lateinit var txtHorario: TextView
    private lateinit var txtLocal: TextView
    private lateinit var txtOrganizador: TextView
    private lateinit var btnInscricao: Button
    private lateinit var btnFavorito: Button

    private lateinit var edtComentario: EditText

    private lateinit var btnPublicarComentario: Button

    private lateinit var layoutComentarios: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var evento: Event? = null
    private var inscrito = false
    private var favorito = false

    private lateinit var txtMediaAvaliacoes: TextView

    private lateinit var spNota: Spinner

    private lateinit var btnAvaliar: Button

    private lateinit var txtOrientacaoAvaliacao: TextView

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
        edtComentario = findViewById(R.id.edtComentario)
        btnPublicarComentario = findViewById(R.id.btnPublicarComentario)
        layoutComentarios = findViewById(R.id.layoutComentarios)
        txtMediaAvaliacoes = findViewById(R.id.txtMediaAvaliacoes)
        spNota = findViewById(R.id.spNota)
        btnAvaliar = findViewById(R.id.btnAvaliar)
        txtOrientacaoAvaliacao = findViewById(R.id.txtOrientacaoAvaliacao)

        val notas = (1..5).map { "$it estrela${if (it > 1) "s" else ""}" }

        spNota.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            notas
        )

        btnAvaliar.setOnClickListener {
            salvarAvaliacao()
        }

        btnPublicarComentario.setOnClickListener {
            publicarComentario()
        }

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
                carregarComentarios()
                carregarMediaAvaliacoes()
                verificarPermissaoAvaliacao()

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
    private fun publicarComentario() {
        val usuario = auth.currentUser
        val eventoAtual = evento
        val texto = edtComentario.text.toString().trim()

        if (usuario == null) {
            Toast.makeText(this, "Faça login para comentar", Toast.LENGTH_LONG).show()
            return
        }

        if (eventoAtual == null) {
            Toast.makeText(this, "Evento ainda não carregado", Toast.LENGTH_LONG).show()
            return
        }

        if (texto.isEmpty()) {
            edtComentario.error = "Digite um comentário"
            return
        }

        btnPublicarComentario.isEnabled = false

        firestore.collection("usuarios")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->
                val nome = documento.getString("nome")
                    ?: usuario.displayName
                    ?: usuario.email
                    ?: "Usuário"

                val dados = hashMapOf(
                    "autorUid" to usuario.uid,
                    "autorNome" to nome,
                    "texto" to texto,
                    "dataPublicacao" to FieldValue.serverTimestamp()
                )

                firestore.collection("eventos")
                    .document(eventoAtual.id.toString())
                    .collection("comentarios")
                    .add(dados)
                    .addOnSuccessListener {
                        edtComentario.text.clear()
                        Toast.makeText(
                            this,
                            "Comentário publicado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        carregarComentarios()
                    }
                    .addOnFailureListener { erro ->
                        Toast.makeText(
                            this,
                            "Erro ao publicar: ${erro.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnCompleteListener {
                        btnPublicarComentario.isEnabled = true
                    }
            }
            .addOnFailureListener { erro ->
                btnPublicarComentario.isEnabled = true
                Toast.makeText(
                    this,
                    "Erro ao buscar usuário: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun carregarComentarios() {
        val eventoAtual = evento ?: return

        firestore.collection("eventos")
            .document(eventoAtual.id.toString())
            .collection("comentarios")
            .get()
            .addOnSuccessListener { resultado ->
                layoutComentarios.removeAllViews()

                for (documento in resultado.documents) {
                    val nome = documento.getString("autorNome") ?: "Usuário"
                    val texto = documento.getString("texto") ?: ""
                    val data = documento.getTimestamp("dataPublicacao")
                        ?.toDate()

                    val dataFormatada = data?.let {
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(it)
                    } ?: "Data não disponível"

                    val comentario = TextView(this).apply {
                        text = "$nome • $dataFormatada\n$texto"
                        textSize = 16f
                        setPadding(0, 16, 0, 16)
                    }

                    layoutComentarios.addView(comentario)

                    if (documento.getString("autorUid") == auth.currentUser?.uid) {
                        val btnExcluir = Button(this).apply {
                            text = "Excluir comentário"
                            setOnClickListener {
                                documento.reference.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@EventDetailActivity,
                                            "Comentário excluído",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        carregarComentarios()
                                    }
                                    .addOnFailureListener { erro ->
                                        Toast.makeText(
                                            this@EventDetailActivity,
                                            "Erro ao excluir: ${erro.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }

                        layoutComentarios.addView(btnExcluir)
                    }
                }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar comentários: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    private fun eventoEncerrado(): Boolean {
        val eventoAtual = evento ?: return false

        val formatos = listOf("dd/MM/yyyy", "yyyy-MM-dd")

        for (formato in formatos) {
            try {
                val sdf = SimpleDateFormat(formato, Locale.getDefault())
                sdf.isLenient = false
                val dataEvento = sdf.parse(eventoAtual.data) ?: continue

                val hoje = Calendar.getInstance()
                hoje.set(Calendar.HOUR_OF_DAY, 0)
                hoje.set(Calendar.MINUTE, 0)
                hoje.set(Calendar.SECOND, 0)
                hoje.set(Calendar.MILLISECOND, 0)

                return dataEvento.before(hoje.time)
            } catch (_: Exception) {
            }
        }

        return false
    }

    private fun verificarPermissaoAvaliacao() {
        val usuario = auth.currentUser
        val eventoAtual = evento

        if (usuario == null || eventoAtual == null) {
            btnAvaliar.isEnabled = false
            txtOrientacaoAvaliacao.text = "Faça login para avaliar."
            return
        }

        if (!eventoEncerrado()) {
            btnAvaliar.isEnabled = false
            txtOrientacaoAvaliacao.text =
                "A avaliação estará disponível após o encerramento do evento."
            return
        }

        firestore.collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .document(eventoAtual.id.toString())
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    btnAvaliar.isEnabled = true
                    txtOrientacaoAvaliacao.text =
                        "Você pode avaliar este evento. Sua nota pode ser alterada depois."
                    carregarMinhaAvaliacao()
                } else {
                    btnAvaliar.isEnabled = false
                    txtOrientacaoAvaliacao.text =
                        "Somente alunos inscritos podem avaliar este evento."
                }
            }
            .addOnFailureListener { erro ->
                btnAvaliar.isEnabled = false
                txtOrientacaoAvaliacao.text =
                    "Não foi possível verificar sua inscrição."
                Toast.makeText(
                    this,
                    "Erro: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun carregarMinhaAvaliacao() {
        val usuario = auth.currentUser ?: return
        val eventoAtual = evento ?: return

        firestore.collection("eventos")
            .document(eventoAtual.id.toString())
            .collection("avaliacoes")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->
                val nota = documento.getLong("nota")?.toInt()

                if (nota != null && nota in 1..5) {
                    spNota.setSelection(nota - 1)
                    btnAvaliar.text = "Alterar avaliação"
                } else {
                    btnAvaliar.text = "Enviar avaliação"
                }
            }
    }

    private fun salvarAvaliacao() {
        val usuario = auth.currentUser
        val eventoAtual = evento

        if (usuario == null || eventoAtual == null) {
            Toast.makeText(
                this,
                "Faça login para avaliar.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!eventoEncerrado()) {
            Toast.makeText(
                this,
                "Só é possível avaliar eventos encerrados.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val nota = spNota.selectedItemPosition + 1

        btnAvaliar.isEnabled = false

        firestore.collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .document(eventoAtual.id.toString())
            .get()
            .addOnSuccessListener { inscricao ->
                if (!inscricao.exists()) {
                    btnAvaliar.isEnabled = true
                    Toast.makeText(
                        this,
                        "Você precisa estar inscrito no evento.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val dados = hashMapOf(
                    "uid" to usuario.uid,
                    "nota" to nota,
                    "dataAvaliacao" to FieldValue.serverTimestamp()
                )

                firestore.collection("eventos")
                    .document(eventoAtual.id.toString())
                    .collection("avaliacoes")
                    .document(usuario.uid)
                    .set(dados)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Avaliação salva com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()

                        btnAvaliar.text = "Alterar avaliação"
                        carregarMediaAvaliacoes()
                    }
                    .addOnFailureListener { erro ->
                        Toast.makeText(
                            this,
                            "Erro ao salvar avaliação: ${erro.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnCompleteListener {
                        btnAvaliar.isEnabled = true
                    }
            }
            .addOnFailureListener { erro ->
                btnAvaliar.isEnabled = true
                Toast.makeText(
                    this,
                    "Erro ao verificar inscrição: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun carregarMediaAvaliacoes() {
        val eventoAtual = evento ?: return

        firestore.collection("eventos")
            .document(eventoAtual.id.toString())
            .collection("avaliacoes")
            .get()
            .addOnSuccessListener { resultado ->
                val notas = resultado.documents.mapNotNull {
                    it.getLong("nota")?.toInt()?.takeIf { nota -> nota in 1..5 }
                }

                if (notas.isEmpty()) {
                    txtMediaAvaliacoes.text = "Ainda não há avaliações."
                } else {
                    val media = notas.average()
                    txtMediaAvaliacoes.text =
                        "Média: ${"%.1f".format(Locale.getDefault(), media)} de 5 estrelas (${notas.size} avaliações)"
                }
            }
            .addOnFailureListener { erro ->
                txtMediaAvaliacoes.text = "Não foi possível carregar as avaliações."
                Toast.makeText(
                    this,
                    "Erro ao carregar avaliações: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}