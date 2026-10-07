package com.example.campus_hub

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EventDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event_detail)

        val txtTitulo =
            findViewById<TextView>(R.id.txtTitulo)

        val txtDescricao =
            findViewById<TextView>(R.id.txtDescricao)

        val txtData =
            findViewById<TextView>(R.id.txtData)

        val txtHorario =
            findViewById<TextView>(R.id.txtHorario)

        val txtLocal =
            findViewById<TextView>(R.id.txtLocal)

        val txtOrganizador =
            findViewById<TextView>(R.id.txtOrganizador)

        val btnInscricao =
            findViewById<Button>(R.id.btnInscricao)

        val session =
            SessionManager(this)

        val eventoId =
            intent.getIntExtra("EVENT_ID", -1)

        val evento =
            EventRepository.eventos.find {
                it.id == eventoId
            }

        if (evento == null) {

            Toast.makeText(
                this,
                "Evento não encontrado",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        txtTitulo.text = evento.titulo

        txtDescricao.text =
            evento.descricao

        txtData.text =
            "Data: ${evento.data}"

        txtHorario.text =
            "Horário: ${evento.horario}"

        txtLocal.text =
            "Local: ${evento.local}"

        txtOrganizador.text =
            "Organizador: ${evento.organizador}"

        atualizarBotao(
            btnInscricao,
            session.estaInscrito(evento.id)
        )

        btnInscricao.setOnClickListener {

            if (session.estaInscrito(evento.id)) {

                session.cancelarInscricao(evento.id)

                Toast.makeText(
                    this,
                    "Inscrição cancelada!",
                    Toast.LENGTH_SHORT
                ).show()

                atualizarBotao(
                    btnInscricao,
                    false
                )

            } else {

                session.inscreverEvento(evento.id)

                Toast.makeText(
                    this,
                    "Inscrição realizada!",
                    Toast.LENGTH_SHORT
                ).show()

                atualizarBotao(
                    btnInscricao,
                    true
                )
            }
        }
    }

    private fun atualizarBotao(
        botao: Button,
        inscrito: Boolean
    ) {

        if (inscrito) {

            botao.text =
                "Cancelar inscrição"

        } else {

            botao.text =
                "Inscrever-se"
        }
    }
}