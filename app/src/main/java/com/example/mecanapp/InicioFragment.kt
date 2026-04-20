package com.example.mecanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //tarjetas
        val cardClientes = view.findViewById<MaterialCardView>(R.id.cardClientes)
        val cardCitas = view.findViewById<MaterialCardView>(R.id.cardCitas)
        val cardOrdenes = view.findViewById<MaterialCardView>(R.id.cardOrdenes)
        val cardInventario = view.findViewById<MaterialCardView>(R.id.cardInventario)


        val bottomNavigation = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)


        cardClientes.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_clientes
        }

        cardCitas.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_citas
        }

        cardOrdenes.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_ordenes
        }

        cardInventario.setOnClickListener {
            bottomNavigation.selectedItemId = R.id.nav_inventario
        }

    }
}