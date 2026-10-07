package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)

        val edtEmail =
            findViewById<EditText>(R.id.edtEmail)

        val edtNovaSenha =
            findViewById<EditText>(R.id.edtNovaSenha)

        val btnAlterarSenha =
            findViewById<Button>(R.id.btnAlterarSenha)

        val session = SessionManager(this)

        btnAlterarSenha.setOnClickListener {

            val email = edtEmail.text.toString().trim()
            val novaSenha = edtNovaSenha.text.toString()

            if (email.isEmpty() || novaSenha.isEmpty()) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!session.usuarioExiste()) {

                Toast.makeText(
                    this,
                    "Nenhuma conta cadastrada",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (email != session.recuperarEmail()) {

                Toast.makeText(
                    this,
                    "E-mail não encontrado",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            session.atualizarSenha(novaSenha)

            Toast.makeText(
                this,
                "Senha alterada com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
    }
}