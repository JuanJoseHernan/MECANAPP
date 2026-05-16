package com.example.mecanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.Inventario
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventarioFragment : Fragment() {

    private lateinit var adapter: InventarioAdapter
    private lateinit var tvAlertas: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAlertas = view.findViewById(R.id.tvAlertasInventario)
        val rvInventario = view.findViewById<RecyclerView>(R.id.rvInventario)
        adapter = InventarioAdapter(emptyList())
        rvInventario.adapter = adapter

        cargarInventario()

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAgregarInventario)
        fab.setOnClickListener { mostrarFormularioNuevaRefaccion() }
    }

    private fun cargarInventario() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val lista = withContext(Dispatchers.IO) {
                db.inventarioDao().getInventario()
            }

            adapter.actualizarLista(lista)

            // Contar productos bajos en stock
            val bajos = lista.count { it.cantidad < it.cantidad_minima }
            if (bajos > 0) {
                tvAlertas.text = "$bajos producto(s) bajo stock mínimo"
                tvAlertas.setTextColor(android.graphics.Color.parseColor("#F44336"))
            } else {
                tvAlertas.text = "Inventario estable"
                tvAlertas.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
        }
    }

    private fun mostrarFormularioNuevaRefaccion() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_refaccion, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView).setCancelable(false).create()

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarInv)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarInv)

        val etNombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreRefaccion)
        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcionRefaccion)
        val etCantidad = dialogView.findViewById<TextInputEditText>(R.id.etCantidadActual)
        val etPrecio = dialogView.findViewById<TextInputEditText>(R.id.etPrecioRefaccion)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val cantidadStr = etCantidad.text.toString().trim()
            val precioStr = etPrecio.text.toString().trim()

            if (nombre.isEmpty()) {
                etNombre.error = "Nombre es obligatorio"
                return@setOnClickListener
            }
            if (cantidadStr.isEmpty()) {
                etCantidad.error = "Cantidad es obligatoria"
                return@setOnClickListener
            }
            if (precioStr.isEmpty()) {
                etPrecio.error = "Precio es obligatorio"
                return@setOnClickListener
            }

            val actual = cantidadStr.toIntOrNull() ?: 0
            val precio = precioStr.toDoubleOrNull() ?: 0.0

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val nuevaRefaccion = Inventario(
                        nombre = nombre,
                        descripcion = descripcion,
                        cantidad = actual,
                        cantidad_minima = actual, // ¡Se guarda igual a la cantidad!
                        precio = precio
                    )
                    AppDatabase.getDatabase(requireContext()).inventarioDao().insert(nuevaRefaccion)
                }
                Toast.makeText(requireContext(), "Refacción guardada ✅", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                cargarInventario()
            }
        }
        dialog.show()
    }
}