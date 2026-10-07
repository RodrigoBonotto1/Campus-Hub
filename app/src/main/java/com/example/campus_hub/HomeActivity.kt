package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val txtSaudacao = findViewById<TextView>(R.id.txtSaudacao)
        val btnEventos = findViewById<Button>(R.id.btnEventos)
        val btnMeusEventos = findViewById<Button>(R.id.btnMeusEventos)
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnSair = findViewById<Button>(R.id.btnSair)

        val usuario = auth.currentUser

        if (usuario == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        firestore.collection("usuarios")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->

                val nome = documento.getString("nome")

                txtSaudacao.text = if (!nome.isNullOrEmpty()) {
                    "Olá, $nome!"
                } else {
                    "Olá!"
                }
            }
            .addOnFailureListener {
                txtSaudacao.text = "Olá!"
            }

        btnEventos.setOnClickListener {
            startActivity(Intent(this, EventsActivity::class.java))
        }

        btnMeusEventos.setOnClickListener {
            startActivity(Intent(this, MyEventsActivity::class.java))
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnSair.setOnClickListener {

            auth.signOut()

            Toast.makeText(
                this,
                "Sessão encerrada",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}