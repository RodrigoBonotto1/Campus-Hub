package com.example.campus_hub

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val edtEmail =
            findViewById<EditText>(R.id.edtEmail)

        val btnAlterarSenha =
            findViewById<Button>(R.id.btnAlterarSenha)

        btnAlterarSenha.setOnClickListener {

            val email =
                edtEmail.text.toString().trim()

            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    "Digite seu e-mail",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            btnAlterarSenha.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "E-mail de recuperação enviado!",
                        Toast.LENGTH_LONG
                    ).show()

                    btnAlterarSenha.isEnabled = true
                }
                .addOnFailureListener { erro ->

                    btnAlterarSenha.isEnabled = true

                    Toast.makeText(
                        this,
                        "Erro: ${erro.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}