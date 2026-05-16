package com.example.mecanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout // Importado
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.CitaParaOrden
import com.example.mecanapp.data.Inventario // Importado
import com.example.mecanapp.data.Reparacion
import com.example.mecanapp.data.ReparacionRefaccion // Importado
import com.example.mecanapp.data.ReparacionServicio // Importado
import com.example.mecanapp.data.Servicio // Importado
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class OrdenesFragment : Fragment() {

    private lateinit var adapter: ReparacionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ordenes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Lista
        val rv = view.findViewById<RecyclerView>(R.id.rvOrdenes)
        adapter = ReparacionAdapter(emptyList()) { id -> finalizarOrden(id) }
        rv.adapter = adapter

        cargarOrdenesDeBD()

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAgregarOrden)
        fab.setOnClickListener { mostrarFormularioNuevaOrden() }
    }

    private fun cargarOrdenesDeBD() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                db.reparacionDao().getTodasLasReparaciones()
            }
            adapter.actualizar(lista)
        }
    }

    private fun finalizarOrden(idReparacion: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_finalizar_orden, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.containerServicios)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddServicioForm)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Lista de piezas para los dropdowns
        var listaInventario = emptyList<Inventario>()
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            listaInventario = withContext(Dispatchers.IO) {
                db.inventarioDao().getInventario()
            }
            // Agregamos el primer bloque de servicio automáticamente
            agregarBloqueServicio(container, listaInventario)
        }

        btnAdd.setOnClickListener { agregarBloqueServicio(container, listaInventario) }

        dialogView.findViewById<Button>(R.id.btnCancelarFinalizar).setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<Button>(R.id.btnGuardarFinalizar).setOnClickListener {
            guardarTodoYCompletar(idReparacion, container, dialog)
        }

        dialog.show()
    }

    private fun agregarBloqueServicio(container: LinearLayout, inventario: List<Inventario>) {
        val view = layoutInflater.inflate(R.layout.item_servicio_form, null)
        val autoComp = view.findViewById<AutoCompleteTextView>(R.id.etRefaccionServicio)

        val nombres = inventario.map { it.nombre }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombres)
        autoComp.setAdapter(adapter)

        container.addView(view)
    }

    private fun guardarTodoYCompletar(idRep: Int, container: LinearLayout, dialog: androidx.appcompat.app.AlertDialog) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val daoDetalle = db.reparacionDetalleDao()
            val inventarioFull = withContext(Dispatchers.IO) { db.inventarioDao().getInventario() }

            withContext(Dispatchers.IO) {
                // Recorremos cada bloque de servicio del formulario
                for (i in 0 until container.childCount) {
                    val view = container.getChildAt(i)
                    val nombreS = view.findViewById<TextInputEditText>(R.id.etNombreServicio).text.toString()
                    val precioS = view.findViewById<TextInputEditText>(R.id.etPrecioServicio).text.toString().toDoubleOrNull() ?: 0.0
                    val refaccionNombre = view.findViewById<AutoCompleteTextView>(R.id.etRefaccionServicio).text.toString()
                    val cantRef = view.findViewById<TextInputEditText>(R.id.etCantidadRefaccion).text.toString().toIntOrNull() ?: 0

                    if (nombreS.isNotEmpty()) {
                        // 1. Guardar el Servicio
                        val nuevoServicio = Servicio(nombre = nombreS, descripcion = "Reparación #$idRep", precio = precioS)
                        val idServicio = daoDetalle.insertarServicio(nuevoServicio)

                        // 2. Vincular con la Reparación (reparacion_servicios)
                        daoDetalle.vincularServicio(ReparacionServicio(idRep, idServicio.toInt()))

                        // 3. Si usó refacción, vincular y descontar stock
                        val refEncontrada = inventarioFull.find { it.nombre == refaccionNombre }
                        if (refEncontrada != null && cantRef > 0) {
                            daoDetalle.vincularRefaccion(ReparacionRefaccion(idRep, refEncontrada.id_refaccion, cantRef))
                            daoDetalle.descontarInventario(refEncontrada.id_refaccion, cantRef)
                        }
                    }
                }
                // 4. Marcar orden como completada
                db.reparacionDao().actualizarEstado(idRep, "Completada")
            }

            Toast.makeText(requireContext(), "Orden finalizada y servicios registrados", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            cargarOrdenesDeBD()
        }
    }

    private fun mostrarFormularioNuevaOrden() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_orden, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etCita = dialogView.findViewById<AutoCompleteTextView>(R.id.etCitaOrden)
        val etMecanico = dialogView.findViewById<AutoCompleteTextView>(R.id.etMecanicoOrden)
        val etFechaFin = dialogView.findViewById<TextInputEditText>(R.id.etFechaFinOrden)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarOrden)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarOrden)

        var citaSeleccionada: CitaParaOrden? = null
        val citasMap = mutableMapOf<String, CitaParaOrden>()

        var mecanicoSeleccionadoId: Int? = null
        val mecanicosMap = mutableMapOf<String, Int>()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val (citasCompletadas, todosUsuarios) = withContext(Dispatchers.IO) {
                Pair(db.citaDao().getCitasCompletadasParaOrden(), db.usuarioDao().getAllUsuarios())
            }

            val opcionesCitas = citasCompletadas.map {
                val txt = "${it.nombreCliente} - ${it.placas ?: "S/P"} (${it.vehiculo})"
                citasMap[txt] = it
                txt
            }
            etCita.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesCitas))

            val mecanicos = todosUsuarios.filter { it.rol == "Mecánico" }
            val opcionesMecanicos = mecanicos.map {
                mecanicosMap[it.nombre] = it.id_usuario
                it.nombre
            }
            etMecanico.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesMecanicos))
        }

        etCita.setOnItemClickListener { _, _, position, _ ->
            citaSeleccionada = citasMap[etCita.adapter.getItem(position).toString()]
        }

        etMecanico.setOnItemClickListener { _, _, position, _ ->
            mecanicoSeleccionadoId = mecanicosMap[etMecanico.adapter.getItem(position).toString()]
        }

        etFechaFin.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Fecha estimada").build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
                etFechaFin.setText(sdf.format(Date(selection)))
            }
            datePicker.show(parentFragmentManager, "DP")
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val fechaFin = etFechaFin.text.toString().trim()
            if (citaSeleccionada == null || mecanicoSeleccionadoId == null || fechaFin.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaInicio = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val nueva = Reparacion(
                        id_vehiculo = citaSeleccionada!!.id_vehiculo,
                        id_usuario = mecanicoSeleccionadoId,
                        estado = "En curso",
                        fecha_inicio = fechaInicio,
                        fecha_fin = fechaFin
                    )
                    AppDatabase.getDatabase(requireContext()).reparacionDao().insert(nueva)
                }
                Toast.makeText(requireContext(), "Orden creada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                cargarOrdenesDeBD()
            }
        }
        dialog.show()
    }
}
