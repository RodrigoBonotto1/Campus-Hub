package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        val edtNome = findViewById<EditText>(R.id.edtNome)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtSenha = findViewById<EditText>(R.id.edtSenha)
        val edtConfirmarSenha =
            findViewById<EditText>(R.id.edtConfirmarSenha)

        val btnCadastrar =
            findViewById<Button>(R.id.btnCadastrar)

        val session = SessionManager(this)

        btnCadastrar.setOnClickListener {

            val nome = edtNome.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString()
            val confirmarSenha =
                edtConfirmarSenha.text.toString()

            if (
                nome.isEmpty() ||
                email.isEmpty() ||
                senha.isEmpty() ||
                confirmarSenha.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!email.contains("@")) {

                Toast.makeText(
                    this,
                    "Digite um e-mail válido",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (senha.length < 4) {

                Toast.makeText(
                    this,
                    "A senha deve possuir pelo menos 4 caracteres",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (senha != confirmarSenha) {

                Toast.makeText(
                    this,
                    "As senhas não são iguais",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            session.salvarUsuario(
                nome,
                email,
                senha
            )

            Toast.makeText(
                this,
                "Conta criada com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
    }
}