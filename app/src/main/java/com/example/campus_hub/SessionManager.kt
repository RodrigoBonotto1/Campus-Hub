package com.example.campus_hub
import android.content.Context

class SessionManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(
        "CampusHubPrefs",
        Context.MODE_PRIVATE
    )

    fun salvarUsuario(nome: String, email: String, senha: String) {
        prefs.edit()
            .putString("nome", nome)
            .putString("email", email)
            .putString("senha", senha)
            .apply()
    }

    fun usuarioExiste(): Boolean {
        return prefs.contains("email")
    }

    fun validarLogin(email: String, senha: String): Boolean {
        val emailSalvo = prefs.getString("email", "")
        val senhaSalva = prefs.getString("senha", "")

        return email == emailSalvo && senha == senhaSalva
    }

    fun recuperarNome(): String {
        return prefs.getString("nome", "") ?: ""
    }

    fun recuperarEmail(): String {
        return prefs.getString("email", "") ?: ""
    }

    fun atualizarNome(nome: String) {
        prefs.edit()
            .putString("nome", nome)
            .apply()
    }

    fun atualizarEmail(email: String) {
        prefs.edit()
            .putString("email", email)
            .apply()
    }

    fun atualizarSenha(senha: String) {
        prefs.edit()
            .putString("senha", senha)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean("logado", false)
            .apply()
    }

    fun login() {
        prefs.edit()
            .putBoolean("logado", true)
            .apply()
    }

    fun estaLogado(): Boolean {
        return prefs.getBoolean("logado", false)
    }
    fun inscreverEvento(eventoId: Int) {

        val eventosAtuais =
            prefs.getStringSet("eventos_inscritos", emptySet())
                ?.toMutableSet()
                ?: mutableSetOf()

        eventosAtuais.add(eventoId.toString())

        prefs.edit()
            .putStringSet("eventos_inscritos", eventosAtuais)
            .apply()
    }

    fun cancelarInscricao(eventoId: Int) {

        val eventosAtuais =
            prefs.getStringSet("eventos_inscritos", emptySet())
                ?.toMutableSet()
                ?: mutableSetOf()

        eventosAtuais.remove(eventoId.toString())

        prefs.edit()
            .putStringSet("eventos_inscritos", eventosAtuais)
            .apply()
    }

    fun estaInscrito(eventoId: Int): Boolean {

        val eventosAtuais =
            prefs.getStringSet("eventos_inscritos", emptySet())
                ?: emptySet()

        return eventosAtuais.contains(eventoId.toString())
    }

    fun recuperarEventosInscritos(): Set<String> {

        return prefs.getStringSet(
            "eventos_inscritos",
            emptySet()
        ) ?: emptySet()
    }
}