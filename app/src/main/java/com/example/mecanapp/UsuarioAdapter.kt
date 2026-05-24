package com.example.mecanapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.Usuario

// Agregamos un callback para manejar los clics del menú
class UsuarioAdapter(
    private var usuariosList: List<Usuario>,
    private val onMenuClick: (Usuario, Int) -> Unit // Pasamos el usuario y una acción (1 para llamar, 2 para correo)
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvRol = view.findViewById<TextView>(R.id.tvRolUsuario)
        val btnOpciones = view.findViewById<ImageButton>(R.id.btnOpcionesUsuario) // El nuevo botón
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuariosList[position]
        holder.tvNombre.text = usuario.nombre
        holder.tvRol.text = usuario.rol ?: "Sin Rol"

        // Configurar el PopupMenu
        holder.btnOpciones.setOnClickListener { view ->
            val popup = PopupMenu(view.context, holder.btnOpciones)
            popup.menu.add(0, 1, 0, "Llamar")
            popup.menu.add(0, 2, 0, "Enviar Correo")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        onMenuClick(usuario, 1) // 1 significa Llamar
                        true
                    }
                    2 -> {
                        onMenuClick(usuario, 2) // 2 significa Correo
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = usuariosList.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuariosList = nuevaLista
        notifyDataSetChanged()
    }
}