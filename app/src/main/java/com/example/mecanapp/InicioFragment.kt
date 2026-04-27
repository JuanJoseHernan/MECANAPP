package com.example.mecanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mecanapp.data.AppDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tarjetas
        val cardClientes = view.findViewById<MaterialCardView>(R.id.cardClientes)
        val cardCitas = view.findViewById<MaterialCardView>(R.id.cardCitas)
        val cardOrdenes = view.findViewById<MaterialCardView>(R.id.cardOrdenes)
        val cardInventario = view.findViewById<MaterialCardView>(R.id.cardInventario)

        val bottomNavigation = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        cardClientes.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_clientes }
        cardCitas.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_citas }
        cardOrdenes.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_ordenes }
        cardInventario.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_inventario }

        // --- INICIO: BOTÓN TEMPORAL PARA VER BD CORREGIDO ---
        val scrollView = view as ViewGroup
        val linearLayout = scrollView.getChildAt(0) as ViewGroup // Tomamos el LinearLayout interno

        val btnTemp = Button(requireContext()).apply {
            text = "🔍 VER BASE DE DATOS (Temporal)"
            setOnClickListener { mostrarBaseDeDatosTemporal() }
        }
        linearLayout.addView(btnTemp, 0) // Agregamos el botón dentro del LinearLayout
        // --- FIN: BOTÓN TEMPORAL ---
    }

    private fun mostrarBaseDeDatosTemporal() {
        lifecycleScope.launch {
            val resultado = java.lang.StringBuilder()

            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext()).openHelper.readableDatabase
                val tablas = listOf("clientes", "vehiculos", "citas", "reparaciones", "inventario")

                for (tabla in tablas) {
                    resultado.append("--- TABLA: ${tabla.uppercase()} ---\n")
                    try {
                        val cursor = db.query("SELECT * FROM $tabla")
                        if (cursor.count == 0) {
                            resultado.append("Vacía\n")
                        } else {
                            while (cursor.moveToNext()) {
                                for (i in 0 until cursor.columnCount) {
                                    resultado.append("${cursor.getColumnName(i)}: ${cursor.getString(i)} | ")
                                }
                                resultado.append("\n")
                            }
                        }
                        cursor.close()
                    } catch (e: Exception) {
                        resultado.append("Aún no creada o error.\n")
                    }
                    resultado.append("\n")
                }
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Estado de la BD")
                .setMessage(resultado.toString())
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }
}