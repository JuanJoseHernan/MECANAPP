package com.example.mecanapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.Cita
import com.example.mecanapp.data.CitaItem
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CitasFragment : Fragment() {

    private lateinit var adapter: CitaAdapter
    private lateinit var tvSinCitas: TextView
    private lateinit var rvCitas: RecyclerView
    private lateinit var etBuscarCita: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_citas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSinCitas = view.findViewById(R.id.tvSinCitas)
        rvCitas = view.findViewById(R.id.rvCitas)
        etBuscarCita = view.findViewById(R.id.etBuscarCita)

        // Inicializar el adaptador y pasarle la acción para los botones
        adapter = CitaAdapter(emptyList()) { idCita, nuevoEstado ->
            cambiarEstadoCita(idCita, nuevoEstado)
        }
        rvCitas.adapter = adapter

        cargarCitasDeBD()

        val fabAgregarCita = view.findViewById<FloatingActionButton>(R.id.fabAgregarCita)
        fabAgregarCita.setOnClickListener {
            mostrarFormularioNuevaCita()
        }

        // Búsqueda
        etBuscarCita.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etBuscarCita.text.toString().trim()
                buscarCitas(query)
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun cambiarEstadoCita(idCita: Int, nuevoEstado: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).citaDao().actualizarEstado(idCita, nuevoEstado)
            }
            Toast.makeText(requireContext(), "Estado cambiado a $nuevoEstado", Toast.LENGTH_SHORT).show()
            cargarCitasDeBD() // Recargamos para ver los colores y botones actualizados
        }
    }

    private fun cargarCitasDeBD() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                db.citaDao().getTodasLasCitas()
            }
            actualizarUI(lista)
        }
    }

    private fun buscarCitas(query: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                if (query.isEmpty()) {
                    db.citaDao().getTodasLasCitas()
                } else {
                    db.citaDao().buscarCitas(query)
                }
            }
            actualizarUI(lista)
        }
    }

    private fun actualizarUI(lista: List<CitaItem>) {
        adapter.actualizarLista(lista)
        if (lista.isEmpty()) {
            rvCitas.visibility = View.GONE
            tvSinCitas.visibility = View.VISIBLE
            if (etBuscarCita.text.toString().isNotEmpty()) {
                tvSinCitas.text = "No se encontraron citas"
            } else {
                tvSinCitas.text = "Aún no hay citas registradas"
            }
        } else {
            rvCitas.visibility = View.VISIBLE
            tvSinCitas.visibility = View.GONE
        }
    }

    private fun mostrarFormularioNuevaCita() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_cita, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarCita)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarCita)

        val etCliente = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.etClienteCita)
        val etFecha = dialogView.findViewById<TextInputEditText>(R.id.etFechaCita)
        val etHora = dialogView.findViewById<TextInputEditText>(R.id.etHoraCita)
        val etServicio = dialogView.findViewById<TextInputEditText>(R.id.etServicioCita)

        etFecha.isFocusable = false
        etFecha.isClickable = true
        etFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Seleccionar fecha").build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                etFecha.setText(sdf.format(Date(selection)))
                etFecha.error = null
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        etHora.isFocusable = false
        etHora.isClickable = true
        etHora.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setTitleText("Seleccionar hora").build()
            timePicker.addOnPositiveButtonClickListener {
                val h = timePicker.hour.toString().padStart(2, '0')
                val m = timePicker.minute.toString().padStart(2, '0')
                etHora.setText("$h:$m")
                etHora.error = null
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        var vehiculoSeleccionadoId = -1
        val vehiculosMap = mutableMapOf<String, Pair<Int, String>>()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val clientesConVehiculos = withContext(Dispatchers.IO) {
                db.clienteDao().getClientesConVehiculos()
            }

            val opciones = mutableListOf<String>()
            for (cv in clientesConVehiculos) {
                for (vehiculo in cv.vehiculos) {
                    val textoVisible = "${cv.cliente.nombre} - ${vehiculo.placas ?: "S/P"} (${vehiculo.marca ?: "S/M"})"
                    opciones.add(textoVisible)
                    vehiculosMap[textoVisible] = Pair(vehiculo.id_vehiculo, cv.cliente.nombre)
                }
            }

            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opciones)
            etCliente.setAdapter(arrayAdapter)
        }

        etCliente.setOnItemClickListener { _, _, position, _ ->
            val seleccion = etCliente.adapter.getItem(position).toString()
            val datos = vehiculosMap[seleccion]
            if (datos != null) {
                vehiculoSeleccionadoId = datos.first
                etCliente.setText(datos.second, false)
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val fecha = etFecha.text.toString().trim()
            val hora = etHora.text.toString().trim()
            val servicio = etServicio.text.toString().trim()

            if (vehiculoSeleccionadoId == -1) {
                etCliente.error = "Selecciona un cliente"
                return@setOnClickListener
            } else { etCliente.error = null }

            if (fecha.isEmpty()) {
                etFecha.error = "Selecciona una fecha"
                return@setOnClickListener
            } else { etFecha.error = null }

            if (hora.isEmpty()) {
                etHora.error = "Selecciona una hora"
                return@setOnClickListener
            } else { etHora.error = null }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val nuevaCita = Cita(
                        id_vehiculo = vehiculoSeleccionadoId,
                        id_usuario = null,
                        fecha = fecha,
                        hora = hora,
                        estado = "Confirmada", // ¡Cambio a Confirmada por defecto!
                        descripcion = servicio
                    )
                    AppDatabase.getDatabase(requireContext()).citaDao().insert(nuevaCita)
                }

                Toast.makeText(requireContext(), "Cita Confirmada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                etBuscarCita.text?.clear()
                cargarCitasDeBD()
            }
        }
        dialog.show()
    }
}