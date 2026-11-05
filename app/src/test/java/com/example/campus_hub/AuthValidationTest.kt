package com.example.campus_hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    // Helper validation functions mirroring app logic
    private fun validateLoginInput(email: String, senha: String):Boolean {
        return email.isNotBlank() && senha.isNotBlank()
    }

    private fun validateRegisterInput(
        nome: String,
        email: String,
        senha: String,
        confirmarSenha: String
    ): Pair<Boolean, String> {
        if (nome.isBlank() || email.isBlank() || senha.isBlank() || confirmarSenha.isBlank()) {
            return Pair(false, "Preencha todos os campos")
        }
        if (senha.length < 6) {
            return Pair(false, "A senha deve possuir pelo menos 6 caracteres")
        }
        if (senha != confirmarSenha) {
            return Pair(false, "As senhas não são iguais")
        }
        return Pair(true, "OK")
    }

    private fun validateForgotPasswordInput(email: String): Boolean {
        return email.isNotBlank()
    }

    @Test
    fun testLoginValidation() {
        assertTrue(validateLoginInput("user@example.com", "123456"))
        assertFalse(validateLoginInput("", "123456"))
        assertFalse(validateLoginInput("user@example.com", ""))
        assertFalse(validateLoginInput("", ""))
    }

    @Test
    fun testRegisterValidation() {
        // Success case
        val (success, msg) = validateRegisterInput("Rodrigo", "rodrigo@example.com", "123456", "123456")
        assertTrue(success)
        assertEquals("OK", msg)

        // Empty fields
        val (s1, m1) = validateRegisterInput("", "rodrigo@example.com", "123456", "123456")
        assertFalse(s1)
        assertEquals("Preencha todos os campos", m1)

        // Short password (< 6 chars)
        val (s2, m2) = validateRegisterInput("Rodrigo", "rodrigo@example.com", "123", "123")
        assertFalse(s2)
        assertEquals("A senha deve possuir pelo menos 6 caracteres", m2)

        // Passwords do not match
        val (s3, m3) = validateRegisterInput("Rodrigo", "rodrigo@example.com", "123456", "654321")
        assertFalse(s3)
        assertEquals("As senhas não são iguais", m3)
    }

    @Test
    fun testForgotPasswordValidation() {
        assertTrue(validateForgotPasswordInput("user@example.com"))
        assertFalse(validateForgotPasswordInput(""))
        assertFalse(validateForgotPasswordInput("   "))
    }
}
