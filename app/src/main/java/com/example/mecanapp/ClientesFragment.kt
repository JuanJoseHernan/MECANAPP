package com.example.mecanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.Cliente
import com.example.mecanapp.data.Vehiculo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientesFragment : Fragment() {

    // 1. Declaramos nuestro adaptador
    private lateinit var adapter: ClienteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Configuramos el RecyclerView y le pegamos el Adaptador
        val rvClientes = view.findViewById<RecyclerView>(R.id.rvClientes)
        adapter = ClienteAdapter(emptyList())
        rvClientes.adapter = adapter

        // 3. Cargamos los datos de la BD al abrir la pantalla
        cargarClientesDeBD()

        val fabAgregarCliente = view.findViewById<FloatingActionButton>(R.id.fabAgregarCliente)
        fabAgregarCliente.setOnClickListener {
            mostrarFormularioNuevoCliente()
        }
    }

    // Función que va a la BD, trae los clientes y actualiza las tarjetas
    private fun cargarClientesDeBD() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                db.clienteDao().getClientesConVehiculos()
            }
            adapter.actualizarLista(lista)
        }
    }

    private fun mostrarFormularioNuevoCliente() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_cliente, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)

        // Vincular los campos de texto
        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombre)
        val etTelefono = dialogView.findViewById<TextInputEditText>(R.id.etTelefono)
        val etPlaca = dialogView.findViewById<TextInputEditText>(R.id.etPlaca)
        val etMarca = dialogView.findViewById<TextInputEditText>(R.id.etMarca)
        val etModelo = dialogView.findViewById<TextInputEditText>(R.id.etModelo)
        val etAnio = dialogView.findViewById<TextInputEditText>(R.id.etAnio)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Operaciones de BD en segundo plano (Corrutinas)
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(requireContext())

                    // Guardar cliente y obtener su ID
                    val nuevoCliente = Cliente(nombre = nombre, telefono = telefono, correo = null)
                    val idGenerado = db.clienteDao().insert(nuevoCliente)

                    // Si llenaron la placa o marca, guardamos el vehículo
                    val placa = etPlaca.text.toString().trim()
                    val marca = etMarca.text.toString().trim()
                    val modelo = etModelo.text.toString().trim()
                    val anioStr = etAnio.text.toString().trim()

                    if (placa.isNotEmpty() || marca.isNotEmpty()) {
                        val anioInt = anioStr.toIntOrNull()
                        val nuevoVehiculo = Vehiculo(
                            id_cliente = idGenerado.toInt(), // Vinculamos con el ID del cliente
                            placas = placa,
                            marca = marca,
                            modelo = modelo,
                            anio = anioInt
                        )
                        db.vehiculoDao().insert(nuevoVehiculo)
                    }
                }

                // Mostrar éxito en el hilo principal
                Toast.makeText(requireContext(), "Cliente registrado en BD", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

                // 4. RECARGAMOS LA LISTA para que el cliente aparezca de inmediato
                cargarClientesDeBD()
            }
        }

        dialog.show()
    }
}