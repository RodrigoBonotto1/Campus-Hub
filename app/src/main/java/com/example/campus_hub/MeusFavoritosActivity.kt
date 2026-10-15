package com.example.campus_hub

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MeusFavoritosActivity : AppCompatActivity() {

    private lateinit var layoutFavoritos: LinearLayout
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favorites)
        layoutFavoritos = findViewById(R.id.layoutFavoritos)
    }

    override fun onResume() {
        super.onResume()
        carregarFavoritos()
    }

    private fun carregarFavoritos() {
        val usuario = auth.currentUser

        if (usuario == null) {
            finish()
            return
        }

        layoutFavoritos.removeViews(
            1,
            layoutFavoritos.childCount - 1
        )

        firestore.collection("usuarios")
            .document(usuario.uid)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { documentos ->

                if (documentos.isEmpty) {
                    val vazio = TextView(this)
                    vazio.text = "Você ainda não tem eventos favoritos."
                    vazio.textSize = 16f
                    layoutFavoritos.addView(vazio)
                    return@addOnSuccessListener
                }

                documentos.documents.forEach { documento ->
                    val eventId = documento.getLong("eventId")?.toInt()
                        ?: documento.id.toIntOrNull()
                        ?: return@forEach

                    val card = LinearLayout(this)
                    card.orientation = LinearLayout.VERTICAL
                    card.setPadding(20, 20, 20, 20)

                    val titulo = TextView(this)
                    titulo.text = documento.getString("titulo") ?: "Evento"
                    titulo.textSize = 20f
                    titulo.setTypeface(null, Typeface.BOLD)

                    val informacoes = TextView(this)
                    informacoes.text =
                        "Data: ${documento.getString("data") ?: ""}\n" +
                                "Horário: ${documento.getString("horario") ?: ""}\n" +
                                "Local: ${documento.getString("local") ?: ""}"
                    informacoes.textSize = 16f
                    informacoes.setPadding(0, 10, 0, 10)

                    val detalhes = Button(this)
                    detalhes.text = "Ver detalhes"
                    detalhes.setOnClickListener {
                        val intent = Intent(this, EventDetailActivity::class.java)
                        intent.putExtra("EVENT_ID", eventId)
                        startActivity(intent)
                    }

                    val remover = Button(this)
                    remover.text = "Remover dos favoritos"
                    remover.setOnClickListener {
                        firestore.collection("usuarios")
                            .document(usuario.uid)
                            .collection("favoritos")
                            .document(eventId.toString())
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Evento removido dos favoritos",
                                    Toast.LENGTH_SHORT
                                ).show()
                                carregarFavoritos()
                            }
                            .addOnFailureListener { erro ->
                                Toast.makeText(
                                    this,
                                    "Erro ao remover favorito: ${erro.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }

                    card.addView(titulo)
                    card.addView(informacoes)
                    card.addView(detalhes)
                    card.addView(remover)

                    val parametros = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    parametros.setMargins(0, 0, 0, 20)
                    layoutFavoritos.addView(card, parametros)
                }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar favoritos: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
