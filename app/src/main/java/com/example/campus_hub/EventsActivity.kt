package com.example.campus_hub

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventsActivity : AppCompatActivity() {

    private lateinit var edtBuscarEvento: EditText
    private lateinit var spFiltroCategoria: android.widget.Spinner
    private lateinit var spFiltroSituacao: android.widget.Spinner
    private lateinit var layoutListaEventos: LinearLayout
    private lateinit var txtSemResultados: TextView

    private var listaEventos: List<Event> = emptyList()

    private val categorias = listOf(
        "Todas",
        "Acadêmico",
        "Tecnologia",
        "Programação",
        "Palestra",
        "Feira",
        "Hackathon",
        "Outros"
    )

    private val situacoes = listOf(
        "Todos",
        "Próximos",
        "Encerrados"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        edtBuscarEvento = findViewById(R.id.edtBuscarEvento)
        spFiltroCategoria = findViewById(R.id.spFiltroCategoria)
        spFiltroSituacao = findViewById(R.id.spFiltroSituacao)
        layoutListaEventos = findViewById(R.id.layoutListaEventos)
        txtSemResultados = findViewById(R.id.txtSemResultados)

        spFiltroCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )

        spFiltroSituacao.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            situacoes
        )

        edtBuscarEvento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                aplicarFiltros()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        spFiltroCategoria.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltros()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }

        spFiltroSituacao.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltros()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }

        carregarEventos()
    }

    private fun carregarEventos() {
        EventRepository.carregarEventos(
            onSuccess = { eventos ->
                if (eventos.isEmpty()) {
                    EventRepository.salvarEventosNoFirebase(
                        onSuccess = {
                            carregarEventos()
                        },
                        onFailure = { erro ->
                            Toast.makeText(
                                this,
                                "Erro ao criar eventos: ${erro.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                    return@carregarEventos
                }

                listaEventos = eventos
                aplicarFiltros()
            },
            onFailure = { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar eventos: ${erro.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun aplicarFiltros() {
        val textoBusca = edtBuscarEvento.text.toString().trim()

        val categoriaSelecionada =
            spFiltroCategoria.selectedItem?.toString() ?: "Todas"

        val situacaoSelecionada =
            spFiltroSituacao.selectedItem?.toString() ?: "Todos"

        val eventosFiltrados = listaEventos.filter { evento ->
            val correspondeTitulo =
                evento.titulo.contains(textoBusca, ignoreCase = true)

            val correspondeCategoria =
                categoriaSelecionada == "Todas" ||
                        evento.categoria.equals(
                            categoriaSelecionada,
                            ignoreCase = true
                        )

            val situacaoEvento = obterSituacao(evento.data)

            val correspondeSituacao = when (situacaoSelecionada) {
                "Próximos" -> situacaoEvento == "Próximo"
                "Encerrados" -> situacaoEvento == "Encerrado"
                else -> true
            }

            correspondeTitulo &&
                    correspondeCategoria &&
                    correspondeSituacao
        }

        mostrarEventos(eventosFiltrados)
    }

    private fun obterSituacao(data: String): String {
        return try {
            val formato = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )
            formato.isLenient = false

            val dataEvento = formato.parse(data)
                ?: return "Indefinido"

            val hoje = Calendar.getInstance()
            hoje.set(Calendar.HOUR_OF_DAY, 0)
            hoje.set(Calendar.MINUTE, 0)
            hoje.set(Calendar.SECOND, 0)
            hoje.set(Calendar.MILLISECOND, 0)

            if (dataEvento.before(hoje.time)) {
                "Encerrado"
            } else {
                "Próximo"
            }
        } catch (_: Exception) {
            "Indefinido"
        }
    }

    private fun mostrarEventos(eventos: List<Event>) {
        layoutListaEventos.removeAllViews()

        txtSemResultados.visibility =
            if (eventos.isEmpty()) View.VISIBLE else View.GONE

        eventos.sortedBy { it.id }.forEach { evento ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 20, 20, 20)
                setBackgroundResource(
                    android.R.drawable.dialog_holo_light_frame
                )
            }

            val titulo = TextView(this).apply {
                text = evento.titulo
                textSize = 21f
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

            val situacao = obterSituacao(evento.data)

            val informacoes = TextView(this).apply {
                text = "Categoria: ${evento.categoria}\n" +
                        "Situação: $situacao\n" +
                        "Data: ${evento.data}\n" +
                        "Horário: ${evento.horario}\n" +
                        "Local: ${evento.local}\n" +
                        "Organizador: ${evento.organizador}"

                textSize = 16f
                setPadding(0, 10, 0, 10)
            }

            val botao = Button(this).apply {
                text = "Ver detalhes"

                setOnClickListener {
                    val intent = Intent(
                        this@EventsActivity,
                        EventDetailActivity::class.java
                    )

                    intent.putExtra("EVENT_ID", evento.id)
                    startActivity(intent)
                }
            }

            card.addView(titulo)
            card.addView(informacoes)
            card.addView(botao)

            val parametros = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            parametros.setMargins(0, 0, 0, 20)

            layoutListaEventos.addView(card, parametros)
        }
    }
}