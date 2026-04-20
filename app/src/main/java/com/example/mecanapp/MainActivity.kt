package com.example.mecanapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Quitamos padding abajo para el menú
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // cargar la pantalla de Inicio
        if (savedInstanceState == null) {
            cargarFragmento(InicioFragment())
        }

        // clicks en el menu inferir
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cargarFragmento(InicioFragment())
                    true
                }
                R.id.nav_clientes -> {
                    cargarFragmento(ClientesFragment())
                    true
                }

                else -> false
            }
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cargarFragmento(InicioFragment())
                    true
                }
                R.id.nav_clientes -> {
                    cargarFragmento(ClientesFragment())
                    true
                }
                R.id.nav_citas -> {
                    cargarFragmento(CitasFragment())
                    true
                }

                else -> false
            }
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> { cargarFragmento(InicioFragment()); true }
                R.id.nav_clientes -> { cargarFragmento(ClientesFragment()); true }
                R.id.nav_citas -> { cargarFragmento(CitasFragment()); true }
                R.id.nav_ordenes -> { cargarFragmento(OrdenesFragment()); true } // <-- NUEVO
                R.id.nav_inventario -> { cargarFragmento(InventarioFragment()); true } // <-- NUEVO
                else -> false
            }
        }

    }


    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_fragmentos, fragment)
            .commit()
    }
}