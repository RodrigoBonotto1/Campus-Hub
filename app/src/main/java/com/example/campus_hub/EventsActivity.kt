package com.example.campus_hub

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EventsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_events)

        val layoutEventos =
            findViewById<LinearLayout>(R.id.layoutEventos)

        for (evento in EventRepository.eventos) {

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

            val botao = Button(this)

            botao.text = "Ver detalhes"

            botao.gravity = Gravity.CENTER

            botao.setOnClickListener {

                val intent =
                    Intent(
                        this,
                        EventDetailActivity::class.java
                    )

                intent.putExtra(
                    "EVENT_ID",
                    evento.id
                )

                startActivity(intent)
            }

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

            card.addView(botao)

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