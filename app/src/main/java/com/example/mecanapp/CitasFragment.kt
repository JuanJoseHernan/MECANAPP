package com.example.mecanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CitasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_citas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabAgregarCita = view.findViewById<FloatingActionButton>(R.id.fabAgregarCita)

        fabAgregarCita.setOnClickListener {
            mostrarFormularioNuevaCita()
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

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            Toast.makeText(requireContext(), "¡Cita agendada! (Prueba)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}