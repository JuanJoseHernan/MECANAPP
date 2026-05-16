package com.example.mecanapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.CitaItem

class CitaAdapter(
    private var citasList: List<CitaItem>,
    private val onCambiarEstado: (Int, String) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreClienteCita)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoCita)
        val tvFechaHora = view.findViewById<TextView>(R.id.tvFechaHora)
        val tvVehiculo = view.findViewById<TextView>(R.id.tvVehiculoInfo)
        val tvServicio = view.findViewById<TextView>(R.id.tvServicioDesc)
        val btnNoAsistio = view.findViewById<Button>(R.id.btnNoAsistio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val item = citasList[position]

        holder.tvNombre.text = item.nombreCliente
        holder.tvEstado.text = item.estado ?: "Confirmada"
        holder.tvFechaHora.text = "${item.fecha} - ${item.hora}"
        holder.tvVehiculo.text = "${item.placas ?: "S/P"} - ${item.marca ?: ""} ${item.modelo ?: ""}"
        holder.tvServicio.text = item.servicio ?: "Sin descripción"

        when (item.estado?.lowercase()) {
            "confirmada", "pendiente" -> holder.tvEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3")) // Azul
            "completada" -> holder.tvEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Verde
            "no asistió" -> holder.tvEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")) // Rojo
            else -> holder.tvEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
        }

        // Ocultar el botón si la cita ya finalizó o no asistió
        if (item.estado?.lowercase() == "completada" || item.estado?.lowercase() == "no asistió") {
            holder.btnNoAsistio.visibility = View.GONE
        } else {
            holder.btnNoAsistio.visibility = View.VISIBLE
        }

        holder.btnNoAsistio.setOnClickListener { onCambiarEstado(item.id_cita, "No asistió") }
    }

    override fun getItemCount() = citasList.size

    fun actualizarLista(nuevaLista: List<CitaItem>) {
        citasList = nuevaLista
        notifyDataSetChanged()
    }
}