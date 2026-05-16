package com.example.mecanapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.ReparacionDisplay

class ReparacionAdapter(
    private var list: List<ReparacionDisplay>,
    private val onFinalizar: (Int) -> Unit
) : RecyclerView.Adapter<ReparacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCliente = view.findViewById<TextView>(R.id.tvClienteOrden)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstadoOrden)
        val tvVehiculo = view.findViewById<TextView>(R.id.tvVehiculoOrden)
        val tvMecanico = view.findViewById<TextView>(R.id.tvMecanicoOrden)
        val tvEntrega = view.findViewById<TextView>(R.id.tvFechaEntrega)
        val btnFinalizar = view.findViewById<Button>(R.id.btnFinalizarOrden)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reparacion, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvCliente.text = item.nombreCliente
        holder.tvEstado.text = item.estado
        holder.tvVehiculo.text = "${item.placas} - ${item.marca} ${item.modelo}"
        holder.tvMecanico.text = "Mecánico: ${item.nombreMecanico ?: "Sin asignar"}"
        holder.tvEntrega.text = "Entrega: ${item.fecha_fin}"

        if (item.estado == "Completada") {
            holder.tvEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            holder.btnFinalizar.visibility = View.GONE
        } else {
            holder.tvEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
            holder.btnFinalizar.visibility = View.VISIBLE
        }

        holder.btnFinalizar.setOnClickListener { onFinalizar(item.id_reparacion) }
    }

    override fun getItemCount() = list.size

    fun actualizar(newList: List<ReparacionDisplay>) {
        list = newList
        notifyDataSetChanged()
    }
}