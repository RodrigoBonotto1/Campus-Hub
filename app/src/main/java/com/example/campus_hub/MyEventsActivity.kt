package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyEventsActivity : AppCompatActivity() {

    private lateinit var layoutMeusEventos: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_events)

        layoutMeusEventos = findViewById(R.id.layoutMeusEventos)

        carregarMeusEventos()
    }

    override fun onResume() {
        super.onResume()
        if (::layoutMeusEventos.isInitialized) {
            carregarMeusEventos()
        }
    }

    private fun carregarMeusEventos() {

        val usuario = auth.currentUser

        if (usuario == null) {
            finish()
            return
        }

        firestore
            .collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .get()
            .addOnSuccessListener { resultado ->

                runOnUiThread {

                    while (layoutMeusEventos.childCount > 1) {
                        layoutMeusEventos.removeViewAt(1)
                    }

                    if (resultado.isEmpty) {

                        val mensagem = TextView(this)

                        mensagem.text = "Você ainda não está inscrito em nenhum evento."
                        mensagem.textSize = 18f
                        mensagem.gravity = Gravity.CENTER
                        mensagem.setPadding(20, 40, 20, 40)

                        layoutMeusEventos.addView(mensagem)

                        return@runOnUiThread
                    }

                    resultado.documents.forEach { documento ->

                        val eventId =
                            documento.getLong("eventId")?.toInt()
                                ?: documento.id.toIntOrNull()
                                ?: return@forEach

                        val titulo =
                            documento.getString("titulo") ?: ""

                        val data =
                            documento.getString("data") ?: ""

                        val horario =
                            documento.getString("horario") ?: ""

                        val local =
                            documento.getString("local") ?: ""

                        val card = LinearLayout(this)

                        card.orientation = LinearLayout.VERTICAL
                        card.setPadding(20, 20, 20, 20)

                        val txtTitulo = TextView(this)

                        txtTitulo.text = titulo
                        txtTitulo.textSize = 20f
                        txtTitulo.setTypeface(
                            null,
                            android.graphics.Typeface.BOLD
                        )

                        val txtInformacoes = TextView(this)

                        txtInformacoes.text =
                            "Data: $data\n" +
                                    "Horário: $horario\n" +
                                    "Local: $local"

                        txtInformacoes.textSize = 16f
                        txtInformacoes.setPadding(0, 10, 0, 10)

                        val btnDetalhes = Button(this)

                        btnDetalhes.text = "Ver detalhes"

                        btnDetalhes.setOnClickListener {

                            val intent = Intent(
                                this,
                                EventDetailActivity::class.java
                            )

                            intent.putExtra(
                                "EVENT_ID",
                                eventId
                            )

                            startActivity(intent)
                        }

                        card.addView(txtTitulo)
                        card.addView(txtInformacoes)
                        card.addView(btnDetalhes)

                        val parametros = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        parametros.setMargins(0, 0, 0, 20)

                        layoutMeusEventos.addView(
                            card,
                            parametros
                        )
                    }
                }
            }
            .addOnFailureListener { erro ->

                Toast.makeText(
                    this,
                    "Erro ao carregar seus eventos: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}