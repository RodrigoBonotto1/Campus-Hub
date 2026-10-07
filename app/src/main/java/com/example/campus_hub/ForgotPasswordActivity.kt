package com.example.campus_hub

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var btnAlterarSenha: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        edtEmail = findViewById(R.id.edtEmail)
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha)

        btnAlterarSenha.setOnClickListener {
            recuperarSenha()
        }
    }

    private fun recuperarSenha() {

        val email = edtEmail.text.toString().trim()

        if (email.isEmpty()) {
            edtEmail.error = "Digite seu e-mail"
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Link de recuperação enviado para seu e-mail.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}