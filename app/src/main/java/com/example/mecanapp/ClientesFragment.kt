package com.example.mecanapp

import android.content.Context
import android.content.Intent // NUEVO: Para la llamada
import android.net.Uri // NUEVO: Para la llamada
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.Cliente
import com.example.mecanapp.data.ClienteConVehiculos
import com.example.mecanapp.data.Vehiculo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientesFragment : Fragment() {

    private lateinit var adapter: ClienteAdapter
    private lateinit var tvSinResultados: TextView
    private lateinit var rvClientes: RecyclerView
    private lateinit var etBuscarCliente: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSinResultados = view.findViewById(R.id.tvSinResultados)
        rvClientes = view.findViewById(R.id.rvClientes)
        etBuscarCliente = view.findViewById(R.id.etBuscarCliente)

        // NUEVO: Pasamos la lógica del clic al Adapter
        adapter = ClienteAdapter(emptyList()) { cliente ->
            mostrarDialogoLlamada(cliente)
        }
        rvClientes.adapter = adapter

        cargarClientesDeBD()

        val fabAgregarCliente = view.findViewById<FloatingActionButton>(R.id.fabAgregarCliente)
        fabAgregarCliente.setOnClickListener {
            mostrarFormularioNuevoCliente()
        }

        // Lógica para buscar cuando se presiona "Enter/Buscar" en el teclado
        etBuscarCliente.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etBuscarCliente.text.toString().trim()
                buscarClientes(query)

                // Ocultar el teclado al buscar
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun cargarClientesDeBD() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                db.clienteDao().getClientesConVehiculos()
            }
            actualizarUI(lista)
        }
    }

    private fun buscarClientes(query: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                if (query.isEmpty()) {
                    db.clienteDao().getClientesConVehiculos()
                } else {
                    db.clienteDao().buscarClientesConVehiculos(query)
                }
            }
            actualizarUI(lista)
        }
    }

    // Función auxiliar para mostrar la lista o el mensaje de vacío
    private fun actualizarUI(lista: List<ClienteConVehiculos>) {
        adapter.actualizarLista(lista)
        if (lista.isEmpty()) {
            rvClientes.visibility = View.GONE
            tvSinResultados.visibility = View.VISIBLE
            // Mostramos un texto distinto si estaban buscando o no
            if (etBuscarCliente.text.toString().isNotEmpty()) {
                tvSinResultados.text = "Cliente no encontrado"
            } else {
                tvSinResultados.text = "Aún no hay clientes registrados"
            }
        } else {
            rvClientes.visibility = View.VISIBLE
            tvSinResultados.visibility = View.GONE
        }
    }

    // NUEVA FUNCIÓN: Ventana para confirmar la llamada
    private fun mostrarDialogoLlamada(cliente: Cliente) {
        if (cliente.telefono.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Este cliente no tiene un número registrado.", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Contactar a ${cliente.nombre}")
            .setMessage("¿Quieres llamar al cliente al número ${cliente.telefono}?")
            .setPositiveButton("Llamar") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${cliente.telefono}")
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Mantén tu función "mostrarFormularioNuevoCliente" exactamente igual aquí abajo
    private fun mostrarFormularioNuevoCliente() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_cliente, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)

        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombre)
        val etTelefono = dialogView.findViewById<TextInputEditText>(R.id.etTelefono)
        val etPlaca = dialogView.findViewById<TextInputEditText>(R.id.etPlaca)
        val etModelo = dialogView.findViewById<TextInputEditText>(R.id.etModelo)
        val etAnio = dialogView.findViewById<TextInputEditText>(R.id.etAnio)

        val etMarca = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.etMarca)
        val tilOtraMarca = dialogView.findViewById<View>(R.id.tilOtraMarca)
        val etOtraMarca = dialogView.findViewById<TextInputEditText>(R.id.etOtraMarca)

        val marcas = arrayOf("Nissan", "Chevrolet", "Toyota", "Volkswagen", "Ford", "Honda", "Otro")
        val adapterMarcas = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, marcas)
        etMarca.setAdapter(adapterMarcas)

        etMarca.setOnItemClickListener { _, _, position, _ ->
            if (marcas[position] == "Otro") {
                tilOtraMarca.visibility = View.VISIBLE
            } else {
                tilOtraMarca.visibility = View.GONE
                etOtraMarca.text?.clear()
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val anioStr = etAnio.text.toString().trim()

            val nombreValido = nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$"))
            if (nombre.isEmpty() || !nombreValido) {
                etNombre.error = "Ingresa un nombre válido (solo letras)"
                return@setOnClickListener
            } else { etNombre.error = null }

            if (telefono.length != 10) {
                etTelefono.error = "Debe tener exactamente 10 números"
                return@setOnClickListener
            } else { etTelefono.error = null }

            if (anioStr.isNotEmpty() && anioStr.length != 4) {
                etAnio.error = "El año debe tener 4 dígitos"
                return@setOnClickListener
            } else { etAnio.error = null }

            val marcaSeleccionada = etMarca.text.toString()
            val marcaFinal = if (marcaSeleccionada == "Otro") etOtraMarca.text.toString().trim() else marcaSeleccionada

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(requireContext())
                    val nuevoCliente = Cliente(nombre = nombre, telefono = telefono, correo = null)
                    val idGenerado = db.clienteDao().insert(nuevoCliente)

                    val placa = etPlaca.text.toString().trim()
                    val modelo = etModelo.text.toString().trim()

                    if (placa.isNotEmpty() || marcaFinal.isNotEmpty()) {
                        val anioInt = anioStr.toIntOrNull()
                        val nuevoVehiculo = Vehiculo(
                            id_cliente = idGenerado.toInt(),
                            placas = placa,
                            marca = marcaFinal,
                            modelo = modelo,
                            anio = anioInt
                        )
                        db.vehiculoDao().insert(nuevoVehiculo)
                    }
                }

                Toast.makeText(requireContext(), "Cliente registrado en BD", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                cargarClientesDeBD()
                // Limpiamos la búsqueda por si había algo escrito
                etBuscarCliente.text?.clear()
            }
        }

        dialog.show()
    }
}