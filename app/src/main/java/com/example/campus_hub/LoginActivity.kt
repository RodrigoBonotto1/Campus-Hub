package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtSenha = findViewById<EditText>(R.id.edtSenha)

        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val btnRecuperar = findViewById<Button>(R.id.btnRecuperar)
        val btnCriarConta = findViewById<Button>(R.id.btnCriarConta)

        val session = SessionManager(this)

        btnEntrar.setOnClickListener {

            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (session.validarLogin(email, senha)) {

                session.login()

                Toast.makeText(
                    this,
                    "Login realizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(this, HomeActivity::class.java)
                )

                finish()

            } else {

                Toast.makeText(
                    this,
                    "E-mail ou senha incorretos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnRecuperar.setOnClickListener {

            startActivity(
                Intent(this, ForgotPasswordActivity::class.java)
            )
        }

        btnCriarConta.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }
}