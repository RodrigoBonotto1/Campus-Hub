package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val edtNome = findViewById<EditText>(R.id.edtNome)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtSenha = findViewById<EditText>(R.id.edtSenha)
        val btnSalvarPerfil = findViewById<Button>(R.id.btnSalvarPerfil)
        val btnSair = findViewById<Button>(R.id.btnSair)

        val usuario = auth.currentUser

        if (usuario == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        edtEmail.setText(usuario.email ?: "")

        firestore.collection("usuarios")
            .document(usuario.uid)
            .get()
            .addOnSuccessListener { documento ->

                edtNome.setText(
                    documento.getString("nome") ?: ""
                )
            }

        btnSalvarPerfil.setOnClickListener {

            val nome = edtNome.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString()

            if (nome.isEmpty() || email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Preencha nome e e-mail",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnSalvarPerfil.isEnabled = false

            val dados = hashMapOf(
                "uid" to usuario.uid,
                "nome" to nome,
                "email" to email
            )

            firestore.collection("usuarios")
                .document(usuario.uid)
                .update(dados as Map<String, Any>)
                .addOnSuccessListener {

                    if (email != usuario.email) {

                        usuario.updateEmail(email)
                            .addOnSuccessListener {

                                atualizarSenha(
                                    usuario,
                                    senha,
                                    btnSalvarPerfil
                                )
                            }
                            .addOnFailureListener { erro ->

                                btnSalvarPerfil.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "Erro ao atualizar e-mail: ${erro.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {

                        atualizarSenha(
                            usuario,
                            senha,
                            btnSalvarPerfil
                        )
                    }
                }
                .addOnFailureListener { erro ->

                    btnSalvarPerfil.isEnabled = true

                    Toast.makeText(
                        this,
                        "Erro ao salvar perfil: ${erro.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        btnSair.setOnClickListener {

            auth.signOut()

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
        }
    }

    private fun atualizarSenha(
        usuario: com.google.firebase.auth.FirebaseUser,
        senha: String,
        botao: Button
    ) {

        if (senha.isEmpty()) {

            botao.isEnabled = true

            Toast.makeText(
                this,
                "Perfil atualizado com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (senha.length < 6) {

            botao.isEnabled = true

            Toast.makeText(
                this,
                "A nova senha deve possuir pelo menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        usuario.updatePassword(senha)
            .addOnSuccessListener {

                botao.isEnabled = true

                Toast.makeText(
                    this,
                    "Perfil atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { erro ->

                botao.isEnabled = true

                Toast.makeText(
                    this,
                    "Erro ao atualizar senha: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}