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

class ClientesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val fabAgregarCliente = view.findViewById<FloatingActionButton>(R.id.fabAgregarCliente)


        fabAgregarCliente.setOnClickListener {
            mostrarFormularioNuevoCliente()
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

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {

            Toast.makeText(requireContext(), "¡Cliente guardado con éxito! (Prueba)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }


        dialog.show()
    }
}