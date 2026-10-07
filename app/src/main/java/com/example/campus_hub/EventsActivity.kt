package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EventsActivity : AppCompatActivity() {

    private lateinit var layoutEventos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_events)

        layoutEventos = findViewById(R.id.layoutEventos)

        carregarEventos()
    }

    private fun carregarEventos() {

        EventRepository.carregarEventos(
            onSuccess = { eventos ->

                runOnUiThread {

                    eventos.sortedBy { it.id }.forEach { evento ->

                        val card = LinearLayout(this)

                        card.orientation = LinearLayout.VERTICAL
                        card.setPadding(20, 20, 20, 20)

                        val titulo = TextView(this)

                        titulo.text = evento.titulo
                        titulo.textSize = 21f
                        titulo.setTypeface(null, android.graphics.Typeface.BOLD)

                        val informacoes = TextView(this)

                        informacoes.text =
                            "Data: ${evento.data}\n" +
                                    "Horário: ${evento.horario}\n" +
                                    "Local: ${evento.local}\n" +
                                    "Organizador: ${evento.organizador}"

                        informacoes.textSize = 16f
                        informacoes.setPadding(0, 10, 0, 10)

                        val botao = Button(this)

                        botao.text = "Ver detalhes"

                        botao.setOnClickListener {

                            val intent = Intent(
                                this,
                                EventDetailActivity::class.java
                            )

                            intent.putExtra(
                                "EVENT_ID",
                                evento.id
                            )

                            startActivity(intent)
                        }

                        card.addView(titulo)
                        card.addView(informacoes)
                        card.addView(botao)

                        val parametros = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        parametros.setMargins(0, 0, 0, 20)

                        layoutEventos.addView(card, parametros)
                    }
                }
            },
            onFailure = { erro ->

                Toast.makeText(
                    this,
                    "Erro ao carregar eventos: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}