package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val edtNome = findViewById<EditText>(R.id.edtNome)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtSenha = findViewById<EditText>(R.id.edtSenha)
        val edtConfirmarSenha = findViewById<EditText>(R.id.edtConfirmarSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {

            val nome = edtNome.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString()
            val confirmarSenha = edtConfirmarSenha.text.toString()

            if (nome.isEmpty() ||
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

            if (senha.length < 6) {
                Toast.makeText(
                    this,
                    "A senha deve possuir pelo menos 6 caracteres",
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

            btnCadastrar.isEnabled = false

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { resultado ->

                    val usuario = resultado.user

                    if (usuario == null) {
                        btnCadastrar.isEnabled = true
                        return@addOnSuccessListener
                    }

                    val dadosUsuario = hashMapOf(
                        "uid" to usuario.uid,
                        "nome" to nome,
                        "email" to email
                    )

                    firestore
                        .collection("usuarios")
                        .document(usuario.uid)
                        .set(dadosUsuario)
                        .addOnSuccessListener {

                            Toast.makeText(
                                this,
                                "Conta criada com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()

                            auth.signOut()

                            startActivity(
                                Intent(
                                    this,
                                    LoginActivity::class.java
                                )
                            )

                            finish()
                        }
                        .addOnFailureListener { erro ->

                            btnCadastrar.isEnabled = true

                            Toast.makeText(
                                this,
                                "Erro ao salvar usuário: ${erro.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { erro ->

                    btnCadastrar.isEnabled = true

                    Toast.makeText(
                        this,
                        "Erro no cadastro: ${erro.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}