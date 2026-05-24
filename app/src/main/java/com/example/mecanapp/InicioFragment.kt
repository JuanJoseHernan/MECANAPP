package com.example.mecanapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.AppDatabase
import com.example.mecanapp.data.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InicioFragment : Fragment() {

    private lateinit var adapterUsuarios: UsuarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vincular Tarjetas
        val cardClientes = view.findViewById<MaterialCardView>(R.id.cardClientes)
        val cardCitas = view.findViewById<MaterialCardView>(R.id.cardCitas)
        val cardOrdenes = view.findViewById<MaterialCardView>(R.id.cardOrdenes)
        val cardInventario = view.findViewById<MaterialCardView>(R.id.cardInventario)

        // Configurar navegación
        val bottomNavigation = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        cardClientes.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_clientes }
        cardCitas.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_citas }
        cardOrdenes.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_ordenes }
        cardInventario.setOnClickListener { bottomNavigation.selectedItemId = R.id.nav_inventario }

        // --- INICIO: LISTA DE USUARIOS ---
        val rvUsuarios = view.findViewById<RecyclerView>(R.id.rvUsuarios)
        rvUsuarios.layoutManager = LinearLayoutManager(requireContext())

        // Inicializamos el adapter con el callback para manejar el menú
        adapterUsuarios = UsuarioAdapter(emptyList()) { usuario, accion ->
            manejarAccionUsuario(usuario, accion)
        }
        rvUsuarios.adapter = adapterUsuarios

        val btnAgregarUsuario = view.findViewById<Button>(R.id.btnAgregarUsuario)
        btnAgregarUsuario.setOnClickListener {
            mostrarFormularioUsuario()
        }
        // --- FIN: LISTA DE USUARIOS ---

        cargarDatos()
    }

    private fun manejarAccionUsuario(usuario: Usuario, accion: Int) {
        when (accion) {
            1 -> { // Llamar
                if (usuario.telefono.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Sin teléfono registrado", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${usuario.telefono}")
                    startActivity(intent)
                }
            }
            2 -> { // Correo
                if (usuario.correo.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Sin correo registrado", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:${usuario.correo}")
                    startActivity(intent)
                }
            }
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hoyStr = sdf.format(Calendar.getInstance().time)

            val (clientesCount, citasCount, ordenesCount, inventarioCount, citasHoyCount, usuarios) = withContext(Dispatchers.IO) {
                DashboardData(
                    db.clienteDao().getClientesCount(),
                    db.citaDao().getCitasCount(),
                    db.reparacionDao().getReparacionesCount(),
                    db.inventarioDao().getInventarioCount(),
                    db.citaDao().getCitasDelDiaCount(hoyStr),
                    db.usuarioDao().getAllUsuarios()
                )
            }

            view?.findViewById<TextView>(R.id.tvTotalClientes)?.text = clientesCount.toString()
            view?.findViewById<TextView>(R.id.tvTotalCitas)?.text = citasCount.toString()
            view?.findViewById<TextView>(R.id.tvTotalOrdenes)?.text = ordenesCount.toString()
            view?.findViewById<TextView>(R.id.tvTotalInventario)?.text = inventarioCount.toString()
            view?.findViewById<TextView>(R.id.tvResumenCitas)?.text = "• $citasHoyCount citas programadas hoy ($hoyStr)"

            adapterUsuarios.actualizarLista(usuarios)
        }
    }

    private fun mostrarFormularioUsuario() {
        val context = requireContext()

        // Crear layout principal del formulario
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        // Crear los campos de texto
        val inputNombre = TextInputEditText(context).apply { hint = "Nombre (Solo letras)" }
        val inputTelefono = TextInputEditText(context).apply {
            hint = "Teléfono (10 dígitos)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val inputCorreo = TextInputEditText(context).apply {
            hint = "Correo Electrónico"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        // --- SOLUCIÓN SENCILLA AL ROL ---
        val btnRol = Button(context, null, com.google.android.material.R.style.Widget_MaterialComponents_Button_OutlinedButton).apply {
            text = "Seleccionar Rol *"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 20, 0, 20)
            }
        }

        var rolSeleccionado = ""
        btnRol.setOnClickListener {
            val roles = arrayOf("Administrador", "Mecánico")
            MaterialAlertDialogBuilder(context)
                .setTitle("Elige un Rol")
                .setItems(roles) { _, which ->
                    rolSeleccionado = roles[which]
                    btnRol.text = "Rol: $rolSeleccionado"
                }
                .show()
        }
        // --------------------------------

        // Envolver campos en TextInputLayout para mejor estética
        layout.addView(TextInputLayout(context).apply { addView(inputNombre) })
        layout.addView(btnRol)
        layout.addView(TextInputLayout(context).apply { addView(inputTelefono) })
        layout.addView(TextInputLayout(context).apply { addView(inputCorreo) })

        // Mostrar el Dialogo
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Registrar Personal")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Guardar", null) // Se anula abajo para validar sin cerrar
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        // Lógica del botón guardar
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val tel = inputTelefono.text.toString().trim()
            val correo = inputCorreo.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty() || !nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$"))) {
                inputNombre.error = "Ingresa un nombre válido (solo letras)"
                return@setOnClickListener
            }
            if (rolSeleccionado.isEmpty()) {
                Toast.makeText(context, "Debes seleccionar un rol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tel.length != 10 || !tel.matches(Regex("^[0-9]+$"))) {
                inputTelefono.error = "El teléfono debe tener 10 números"
                return@setOnClickListener
            }
            if (correo.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                inputCorreo.error = "Formato de correo inválido"
                return@setOnClickListener
            }

            // Guardar usuario en la BD si todo es correcto
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    // AQUÍ AÑADIMOS EL ESTADO POR DEFECTO
                    val nuevoUsuario = Usuario(nombre = nombre, rol = rolSeleccionado, telefono = tel, correo = correo, estado = "Disponible")
                    AppDatabase.getDatabase(context).usuarioDao().insert(nuevoUsuario)
                }
                Toast.makeText(context, "Personal registrado ", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                cargarDatos() // Recargamos para ver al nuevo usuario en la lista
            }
        }
    }

    data class DashboardData(
        val a: Int, val b: Int, val c: Int, val d: Int, val e: Int, val f: List<Usuario>
    )
}