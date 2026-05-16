package com.example.mecanapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.Usuario

class UsuarioAdapter(private var usuariosList: List<Usuario>) :
    RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvRol = view.findViewById<TextView>(R.id.tvRolUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuariosList[position]
        holder.tvNombre.text = usuario.nombre
        holder.tvRol.text = usuario.rol ?: "Sin Rol"
    }

    override fun getItemCount() = usuariosList.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuariosList = nuevaLista
        notifyDataSetChanged()
    }
}