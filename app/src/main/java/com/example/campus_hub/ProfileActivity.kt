package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        val edtNome =
            findViewById<EditText>(R.id.edtNome)

        val edtEmail =
            findViewById<EditText>(R.id.edtEmail)

        val edtSenha =
            findViewById<EditText>(R.id.edtSenha)

        val btnSalvarPerfil =
            findViewById<Button>(R.id.btnSalvarPerfil)

        val btnSair =
            findViewById<Button>(R.id.btnSair)

        val session =
            SessionManager(this)

        // Carrega os dados atuais
        edtNome.setText(
            session.recuperarNome()
        )

        edtEmail.setText(
            session.recuperarEmail()
        )

        // Salvar alterações
        btnSalvarPerfil.setOnClickListener {

            val nome =
                edtNome.text.toString().trim()

            val email =
                edtEmail.text.toString().trim()

            val senha =
                edtSenha.text.toString()

            if (nome.isEmpty()) {

                Toast.makeText(
                    this,
                    "Digite seu nome",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    "Digite seu e-mail",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            session.atualizarNome(nome)

            session.atualizarEmail(email)

            if (senha.isNotEmpty()) {
                session.atualizarSenha(senha)
            }

            Toast.makeText(
                this,
                "Perfil atualizado com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

            edtSenha.text.clear()
        }

        // Logout
        btnSair.setOnClickListener {

            session.logout()

            val intent =
                Intent(this, MainActivity::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()
        }
    }
}