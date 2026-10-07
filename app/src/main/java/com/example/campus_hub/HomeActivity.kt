package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val session = SessionManager(this)

        val txtSaudacao = findViewById<TextView>(R.id.txtSaudacao)

        val btnEventos = findViewById<Button>(R.id.btnEventos)
        val btnMeusEventos = findViewById<Button>(R.id.btnMeusEventos)
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnSair = findViewById<Button>(R.id.btnSair)

        txtSaudacao.text = "Olá, ${session.recuperarNome()}!"

        btnEventos.setOnClickListener {

            startActivity(
                Intent(this, EventsActivity::class.java)
            )
        }

        btnMeusEventos.setOnClickListener {

            startActivity(
                Intent(this, MyEventsActivity::class.java)
            )
        }

        btnPerfil.setOnClickListener {

            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }

        btnSair.setOnClickListener {

            session.logout()

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()
        }
    }
}