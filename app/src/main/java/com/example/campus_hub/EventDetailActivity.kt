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
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream

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
    private lateinit var btnTirarFoto: Button
    private lateinit var btnEscolherFoto: Button
    private lateinit var layoutFotos: LinearLayout

    private val tirarFotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) salvarFoto(bitmap)
        }

    private val escolherFotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) salvarFotoDaGaleria(uri)
        }

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
        btnTirarFoto = findViewById(R.id.btnTirarFoto)
        btnEscolherFoto = findViewById(R.id.btnEscolherFoto)
        layoutFotos = findViewById(R.id.layoutFotos)

        btnTirarFoto.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, "Entre na sua conta para publicar fotos.", Toast.LENGTH_SHORT).show()
            } else {
                tirarFotoLauncher.launch(null)
            }
        }

        btnEscolherFoto.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, "Entre na sua conta para publicar fotos.", Toast.LENGTH_SHORT).show()
            } else {
                escolherFotoLauncher.launch("image/*")
            }
        }

        val notas = (1..5).map { "$it estrela${if (it > 1) "s" else ""}" }

        val btnLocalizacao = findViewById<Button>(R.id.btnLocalizacao)

        btnLocalizacao.setOnClickListener {
            abrirLocalizacaoEvento()
        }

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

    private fun abrirLocalizacaoEvento() {
        val eventoAtual = evento

        if (eventoAtual == null) {
            Toast.makeText(
                this,
                "Evento não carregado.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val endereco = eventoAtual.local

        val uri = Uri.parse(
            "geo:0,0?q=${Uri.encode(endereco)}"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val url = Uri.parse(
                "https://www.google.com/maps/search/?api=1&query=${Uri.encode(endereco)}"
            )

            startActivity(Intent(Intent.ACTION_VIEW, url))
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
                carregarFotos()
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
    private fun obterPastaFotos(): File? {
        val eventoAtual = evento ?: return null
        val pasta = File(filesDir, "fotos_eventos/${eventoAtual.id}")
        if (!pasta.exists() && !pasta.mkdirs()) return null
        return pasta
    }

    private fun salvarFoto(bitmap: Bitmap) {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Entre na sua conta para publicar fotos.", Toast.LENGTH_SHORT).show()
            return
        }

        val pasta = obterPastaFotos()
        if (pasta == null) {
            Toast.makeText(this, "Não foi possível acessar a pasta do evento.", Toast.LENGTH_SHORT).show()
            return
        }

        val arquivo = File(pasta, "${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(arquivo).use { saida ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, saida)) {
                    throw Exception("Falha ao converter a imagem.")
                }
            }
            registrarFoto(arquivo)
        } catch (e: Exception) {
            arquivo.delete()
            Toast.makeText(this, "Erro ao salvar a foto: ${e.message ?: "erro desconhecido"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun salvarFotoDaGaleria(uri: Uri) {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Entre na sua conta para publicar fotos.", Toast.LENGTH_SHORT).show()
            return
        }

        val pasta = obterPastaFotos()
        if (pasta == null) {
            Toast.makeText(this, "Não foi possível acessar a pasta do evento.", Toast.LENGTH_SHORT).show()
            return
        }

        val arquivo = File(pasta, "${System.currentTimeMillis()}.jpg")
        try {
            val bitmap = contentResolver.openInputStream(uri)?.use { entrada ->
                BitmapFactory.decodeStream(entrada)
            } ?: throw Exception("Não foi possível abrir a imagem.")

            FileOutputStream(arquivo).use { saida ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, saida)) {
                    throw Exception("Falha ao converter a imagem.")
                }
            }
            bitmap.recycle()
            registrarFoto(arquivo)
        } catch (e: Exception) {
            arquivo.delete()
            Toast.makeText(this, "Não foi possível importar a foto: ${e.message ?: "erro desconhecido"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun registrarFoto(arquivo: File) {
        val usuario = auth.currentUser ?: return
        val eventoAtual = evento ?: return

        referenciaUsuario()?.get()
            ?.addOnSuccessListener { documento ->
                val nome = documento.getString("nome")
                    ?: usuario.displayName
                    ?: usuario.email
                    ?: "Usuário"
                guardarDadosFoto(arquivo, usuario.uid, nome, eventoAtual.id)
            }
            ?.addOnFailureListener {
                val nome = usuario.displayName ?: usuario.email ?: "Usuário"
                guardarDadosFoto(arquivo, usuario.uid, nome, eventoAtual.id)
            }
            ?: run {
                val nome = usuario.displayName ?: usuario.email ?: "Usuário"
                guardarDadosFoto(arquivo, usuario.uid, nome, eventoAtual.id)
            }
    }

    private fun guardarDadosFoto(arquivo: File, uid: String, nome: String, eventId: Int) {
        val chave = "${eventId}_${arquivo.name}"
        getSharedPreferences("fotos_eventos", MODE_PRIVATE).edit()
            .putString("${chave}_autorUid", uid)
            .putString("${chave}_autorNome", nome)
            .putLong("${chave}_data", System.currentTimeMillis())
            .apply()
        carregarFotos()
    }

    private fun carregarBitmapReduzido(caminho: String): Bitmap? {
        val opcoes = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(caminho, opcoes)
        val maiorDimensao = maxOf(opcoes.outWidth, opcoes.outHeight)
        var amostra = 1
        while (maiorDimensao / amostra > 1200) amostra *= 2
        val opcoesFinal = BitmapFactory.Options().apply { inSampleSize = amostra }
        return BitmapFactory.decodeFile(caminho, opcoesFinal)
    }

    private fun carregarFotos() {
        if (!::layoutFotos.isInitialized) return
        val eventoAtual = evento ?: return
        val pasta = obterPastaFotos() ?: return
        val preferencias = getSharedPreferences("fotos_eventos", MODE_PRIVATE)
        val usuarioUid = auth.currentUser?.uid

        layoutFotos.removeAllViews()
        val arquivos = pasta.listFiles()
            ?.filter { it.isFile && it.extension.equals("jpg", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

        for (arquivo in arquivos) {
            val chave = "${eventoAtual.id}_${arquivo.name}"
            val autorUid = preferencias.getString("${chave}_autorUid", null) ?: continue
            val autorNome = preferencias.getString("${chave}_autorNome", "Usuário") ?: "Usuário"
            val data = preferencias.getLong("${chave}_data", arquivo.lastModified())
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 12, 0, 20)
            }

            val imagem = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    240
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = "Foto publicada por $autorNome"
                setImageBitmap(carregarBitmapReduzido(arquivo.absolutePath))
            }

            val textoAutor = TextView(this).apply {
                text = "Publicado por: $autorNome"
                textSize = 14f
                setPadding(0, 8, 0, 2)
            }

            val textoData = TextView(this).apply {
                text = "Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(data))}"
                textSize = 12f
            }

            item.addView(imagem)
            item.addView(textoAutor)
            item.addView(textoData)

            if (usuarioUid != null && usuarioUid == autorUid) {
                val btnExcluir = Button(this).apply {
                    text = "Excluir minha foto"
                    setOnClickListener { excluirFoto(arquivo, chave) }
                }
                item.addView(btnExcluir)
            }

            layoutFotos.addView(item)
        }
    }

    private fun excluirFoto(arquivo: File, chave: String) {
        val usuarioUid = auth.currentUser?.uid
        if (usuarioUid == null) {
            Toast.makeText(this, "Entre na sua conta para excluir uma foto.", Toast.LENGTH_SHORT).show()
            return
        }

        val preferencias = getSharedPreferences("fotos_eventos", MODE_PRIVATE)
        if (preferencias.getString("${chave}_autorUid", null) != usuarioUid) {
            Toast.makeText(this, "Você só pode excluir suas próprias fotos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (arquivo.delete()) {
            preferencias.edit()
                .remove("${chave}_autorUid")
                .remove("${chave}_autorNome")
                .remove("${chave}_data")
                .apply()
            carregarFotos()
            Toast.makeText(this, "Foto excluída.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Não foi possível excluir a foto.", Toast.LENGTH_SHORT).show()
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
