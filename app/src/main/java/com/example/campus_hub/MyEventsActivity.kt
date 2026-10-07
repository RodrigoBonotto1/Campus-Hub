package com.example.campus_hub

import android.os.Bundle
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyEventsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_events)

        val layoutEventos =
            findViewById<LinearLayout>(R.id.layoutMeusEventos)

        val session =
            SessionManager(this)

        val eventosInscritos =
            session.recuperarEventosInscritos()

        if (eventosInscritos.isEmpty()) {

            val mensagem = TextView(this)

            mensagem.text =
                "Você ainda não está inscrito em nenhum evento."

            mensagem.textSize = 17f

            layoutEventos.addView(mensagem)

            return
        }

        for (evento in EventRepository.eventos) {

            if (!eventosInscritos.contains(evento.id.toString())) {
                continue
            }

            val titulo = TextView(this)

            titulo.text = evento.titulo
            titulo.textSize = 20f
            titulo.setTypeface(null, Typeface.BOLD)

            val data = TextView(this)

            data.text =
                "${evento.data} às ${evento.horario}"

            data.textSize = 15f

            val local = TextView(this)

            local.text =
                "Local: ${evento.local}"

            local.textSize = 15f

            val organizador = TextView(this)

            organizador.text =
                "Organizador: ${evento.organizador}"

            organizador.textSize = 15f

            val card = LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                20,
                20,
                20,
                20
            )

            card.addView(titulo)
            card.addView(data)
            card.addView(local)
            card.addView(organizador)

            val parametros =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            parametros.setMargins(
                0,
                0,
                0,
                20
            )

            card.layoutParams = parametros

            layoutEventos.addView(card)
        }
    }
}