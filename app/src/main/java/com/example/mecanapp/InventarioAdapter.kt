package com.example.mecanapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.Inventario
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale

class InventarioAdapter(private var inventarioList: List<Inventario>) :
    RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    class InventarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card = view.findViewById<MaterialCardView>(R.id.cardInventario)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreRefaccion)
        val tvCategoria = view.findViewById<TextView>(R.id.tvCategoria)
        val tvPrecio = view.findViewById<TextView>(R.id.tvPrecioRefaccion) // NUEVO ID
        val tvCantidades = view.findViewById<TextView>(R.id.tvCantidades)
        val ivEstatus = view.findViewById<ImageView>(R.id.ivEstatus)
        val progressStock = view.findViewById<LinearProgressIndicator>(R.id.progressStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventario, parent, false)
        return InventarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val item = inventarioList[position]

        holder.tvNombre.text = item.nombre
        holder.tvCategoria.text = item.descripcion ?: "Sin descripción"

        // NUEVO: Formateamos y mostramos el precio
        val precio = item.precio ?: 0.0
        holder.tvPrecio.text = "$${String.format(Locale.US, "%.2f", precio)} c/u"

        holder.tvCantidades.text = "${item.cantidad} / ${item.cantidad_minima} unidades"

        // --- LÓGICA DE LA BARRA ---
        val maxProgress = if (item.cantidad_minima > 0) item.cantidad_minima else 1
        holder.progressStock.max = maxProgress
        holder.progressStock.progress = if (item.cantidad >= item.cantidad_minima) maxProgress else item.cantidad

        // --- LÓGICA DE COLORES ---
        val isStockBajo = item.cantidad < item.cantidad_minima

        if (isStockBajo) {
            holder.card.setCardBackgroundColor(Color.parseColor("#FFF0F0"))
            holder.ivEstatus.setImageResource(R.drawable.ic_warning)
            holder.ivEstatus.setColorFilter(Color.parseColor("#F44336"))
            holder.progressStock.setIndicatorColor(Color.parseColor("#F44336"))
            holder.tvCantidades.setTextColor(Color.parseColor("#F44336"))
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.ivEstatus.setImageResource(R.drawable.ic_check)
            holder.ivEstatus.setColorFilter(Color.parseColor("#4CAF50"))
            holder.progressStock.setIndicatorColor(Color.parseColor("#388E3C"))
            holder.tvCantidades.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = inventarioList.size

    fun actualizarLista(nuevaLista: List<Inventario>) {
        inventarioList = nuevaLista
        notifyDataSetChanged()
    }
}