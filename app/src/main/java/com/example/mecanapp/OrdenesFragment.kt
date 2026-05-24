package com.example.mecanapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.CitaParaOrden
import com.example.mecanapp.data.Inventario
import com.example.mecanapp.data.Reparacion
import com.example.mecanapp.data.ReparacionRefaccion
import com.example.mecanapp.data.ReparacionServicio
import com.example.mecanapp.data.Servicio
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
    private lateinit var tvSinResultados: TextView
    private lateinit var rvOrdenes: RecyclerView
    private lateinit var etBuscarOrden: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ordenes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvOrdenes = view.findViewById(R.id.rvOrdenes)
        tvSinResultados = view.findViewById(R.id.tvSinResultadosOrdenes)
        etBuscarOrden = view.findViewById(R.id.etBuscarOrden)

        adapter = ReparacionAdapter(emptyList(),
            onFinalizarClick = { id -> finalizarOrden(id) },
            onDetalleClick = { id -> mostrarDetallesOrden(id) }
        )
        rvOrdenes.adapter = adapter
        rvOrdenes.layoutManager = LinearLayoutManager(requireContext())

        cargarOrdenesDeBD()

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAgregarOrden)
        fab.setOnClickListener { mostrarFormularioNuevaOrden() }

        // --- LÓGICA DE LA BARRA DE BÚSQUEDA ---
        etBuscarOrden.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etBuscarOrden.text.toString().trim()
                buscarOrdenes(query)

                // Ocultar el teclado al buscar
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun cargarOrdenesDeBD() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                db.reparacionDao().getTodasLasReparaciones()
            }
            actualizarUI(lista)
        }
    }

    private fun buscarOrdenes(query: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                if (query.isEmpty()) {
                    db.reparacionDao().getTodasLasReparaciones()
                } else {
                    // LLAMAMOS A LA NUEVA FUNCIÓN DEL DAO
                    db.reparacionDao().buscarReparacionesPorCliente(query)
                }
            }
            actualizarUI(lista)
        }
    }

    // Función auxiliar para actualizar lista y mostrar texto de vacío si es necesario
    private fun actualizarUI(lista: List<Any>) { // Reemplaza 'Any' por el tipo de dato que usa ReparacionAdapter
        // Hacemos un casteo inseguro solo para el ejemplo, asegúrate de que lista sea del tipo correcto
        adapter.actualizar(lista as List<Nothing>)

        if (lista.isEmpty()) {
            rvOrdenes.visibility = View.GONE
            tvSinResultados.visibility = View.VISIBLE
            if (etBuscarOrden.text.toString().isNotEmpty()) {
                tvSinResultados.text = "No se encontraron órdenes para este cliente"
            } else {
                tvSinResultados.text = "Aún no hay órdenes registradas"
            }
        } else {
            rvOrdenes.visibility = View.VISIBLE
            tvSinResultados.visibility = View.GONE
        }
    }

    // --- EL RESTO DE TU CÓDIGO SE MANTIENE EXACTAMENTE IGUAL ---

    private fun mostrarDetallesOrden(idReparacion: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_orden, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val dao = db.reparacionDetalleDao()

            val infoBase = withContext(Dispatchers.IO) { dao.getDetalleBase(idReparacion) }
            val servicios = withContext(Dispatchers.IO) { dao.getServiciosDeReparacion(idReparacion) }
            val refacciones = withContext(Dispatchers.IO) { dao.getRefaccionesDeReparacion(idReparacion) }

            dialogView.findViewById<TextView>(R.id.tvDetalleCliente).text = "Cliente: ${infoBase?.nombre_cliente ?: "Desconocido"}"
            dialogView.findViewById<TextView>(R.id.tvDetalleVehiculo).text = "Vehículo: ${infoBase?.marca ?: ""} ${infoBase?.modelo ?: ""} (${infoBase?.placas ?: ""})"
            dialogView.findViewById<TextView>(R.id.tvDetalleMecanico).text = "Mecánico: ${infoBase?.nombre_mecanico ?: "Sin asignar"}"
            dialogView.findViewById<TextView>(R.id.tvDetalleFecha).text = "Entrega: ${infoBase?.fecha_fin ?: "N/A"}"

            var totalFinal = 0.0

            val containerServicios = dialogView.findViewById<LinearLayout>(R.id.containerDetalleServicios)
            if (servicios.isEmpty()) {
                containerServicios.addView(TextView(requireContext()).apply { text = "No se registraron servicios." })
            } else {
                servicios.forEach { s ->
                    val tv = TextView(requireContext()).apply {
                        text = "• ${s.nombre} - $${s.precio ?: 0.0}"
                        textSize = 15f
                        setPadding(0, 4, 0, 4)
                    }
                    containerServicios.addView(tv)
                    totalFinal += (s.precio ?: 0.0)
                }
            }

            val containerRefacciones = dialogView.findViewById<LinearLayout>(R.id.containerDetalleRefacciones)
            if (refacciones.isEmpty()) {
                containerRefacciones.addView(TextView(requireContext()).apply { text = "No se usaron refacciones." })
            } else {
                refacciones.forEach { r ->
                    val precioUnitario = r.precio ?: 0.0
                    val subtotalRefaccion = precioUnitario * r.cantidad
                    val tv = TextView(requireContext()).apply {
                        text = "• ${r.nombre} (x${r.cantidad}) - $${String.format(Locale.US, "%.2f", precioUnitario)} c/u = $${String.format(Locale.US, "%.2f", subtotalRefaccion)}"
                        textSize = 15f
                        setPadding(0, 4, 0, 4)
                    }
                    containerRefacciones.addView(tv)
                    totalFinal += subtotalRefaccion
                }
            }

            val tvTotal = dialogView.findViewById<TextView>(R.id.tvTotalOrden)
            tvTotal.text = "$${String.format(Locale.US, "%.2f", totalFinal)}"
        }

        dialogView.findViewById<Button>(R.id.btnCerrarDetalles).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun finalizarOrden(idReparacion: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_finalizar_orden, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.containerServicios)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddServicioForm)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        var listaInventario = emptyList<Inventario>()
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            listaInventario = withContext(Dispatchers.IO) {
                db.inventarioDao().getInventario()
            }
            agregarBloqueServicio(container, listaInventario)
        }

        btnAdd.setOnClickListener { agregarBloqueServicio(container, listaInventario) }
        dialogView.findViewById<Button>(R.id.btnCancelarFinalizar).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnGuardarFinalizar).setOnClickListener {
            guardarTodoYCompletar(idReparacion, container, dialog)
        }

        dialog.show()
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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

            val reparacion = withContext(Dispatchers.IO) {
                db.reparacionDao().getReparacionById(idRep)
            }
            val idUsuarioAsignado = reparacion?.id_usuario

            withContext(Dispatchers.IO) {
                for (i in 0 until container.childCount) {
                    val view = container.getChildAt(i)
                    val nombreS = view.findViewById<TextInputEditText>(R.id.etNombreServicio).text.toString()
                    val precioS = view.findViewById<TextInputEditText>(R.id.etPrecioServicio).text.toString().toDoubleOrNull() ?: 0.0
                    val refaccionNombre = view.findViewById<AutoCompleteTextView>(R.id.etRefaccionServicio).text.toString()
                    val cantRef = view.findViewById<TextInputEditText>(R.id.etCantidadRefaccion).text.toString().toIntOrNull() ?: 0

                    if (nombreS.isNotEmpty()) {
                        val nuevoServicio = Servicio(nombre = nombreS, descripcion = "Reparación #$idRep", precio = precioS)
                        val idServicio = daoDetalle.insertarServicio(nuevoServicio)
                        daoDetalle.vincularServicio(ReparacionServicio(idRep, idServicio.toInt()))

                        val refEncontrada = inventarioFull.find { it.nombre == refaccionNombre }
                        if (refEncontrada != null && cantRef > 0) {
                            daoDetalle.vincularRefaccion(ReparacionRefaccion(idRep, refEncontrada.id_refaccion, cantRef))
                            daoDetalle.descontarInventario(refEncontrada.id_refaccion, cantRef)
                        }
                    }
                }

                db.reparacionDao().actualizarEstado(idRep, "Completada")

                if (idUsuarioAsignado != null) {
                    db.usuarioDao().actualizarEstado(idUsuarioAsignado, "Disponible")
                }
            }

            Toast.makeText(requireContext(), "Orden finalizada y mecánico liberado", Toast.LENGTH_SHORT).show()
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
            val (citasConfirmadas, todosUsuarios) = withContext(Dispatchers.IO) {
                Pair(db.citaDao().getCitasConfirmadasParaOrden(), db.usuarioDao().getAllUsuarios())
            }

            val opcionesCitas = citasConfirmadas.map {
                val txt = "${it.nombreCliente} - ${it.placas ?: "S/P"} (${it.vehiculo})"
                citasMap[txt] = it
                txt
            }
            etCita.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesCitas))

            val mecanicosDisponibles = todosUsuarios.filter { it.rol == "Mecánico" && it.estado == "Disponible" }

            if (mecanicosDisponibles.isEmpty()) {
                val mensajeVacio = listOf("No hay mecánicos disponibles")
                etMecanico.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mensajeVacio))
                etMecanico.setText("", false)
            } else {
                val opcionesMecanicos = mecanicosDisponibles.map {
                    mecanicosMap[it.nombre] = it.id_usuario
                    it.nombre
                }
                etMecanico.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opcionesMecanicos))
            }
        }

        etCita.setOnItemClickListener { _, _, position, _ -> citaSeleccionada = citasMap[etCita.adapter.getItem(position).toString()] }
        etMecanico.setOnItemClickListener { _, _, position, _ ->
            val seleccion = etMecanico.adapter.getItem(position).toString()
            if (seleccion != "No hay mecánicos disponibles") {
                mecanicoSeleccionadoId = mecanicosMap[seleccion]
            } else {
                mecanicoSeleccionadoId = null
                etMecanico.setText("", false)
                Toast.makeText(requireContext(), "Debes registrar o liberar a un mecánico primero.", Toast.LENGTH_SHORT).show()
            }
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

                    val db = AppDatabase.getDatabase(requireContext())
                    db.reparacionDao().insert(nueva)
                    db.citaDao().actualizarEstado(citaSeleccionada!!.id_cita, "Completada")
                    db.usuarioDao().actualizarEstado(mecanicoSeleccionadoId!!, "Ocupado")
                }
                Toast.makeText(requireContext(), "Orden creada y mecánico asignado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                cargarOrdenesDeBD()
            }
        }
        dialog.show()
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}