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

class OrdenesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ordenes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAgregarOrden)
        fab.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_orden, null)
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView).setCancelable(false).create()

            dialogView.findViewById<Button>(R.id.btnCancelarOrden).setOnClickListener { dialog.dismiss() }
            dialogView.findViewById<Button>(R.id.btnGuardarOrden).setOnClickListener {
                Toast.makeText(requireContext(), "¡Orden creada! (Prueba)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}