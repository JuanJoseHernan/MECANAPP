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

class InventarioAdapter(private var inventarioList: List<Inventario>) :
    RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    class InventarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card = view.findViewById<MaterialCardView>(R.id.cardInventario)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreRefaccion)
        val tvCategoria = view.findViewById<TextView>(R.id.tvCategoria)
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
        holder.tvCategoria.text = item.descripcion ?: "Sin categoría"
        holder.tvCantidades.text = "${item.cantidad} / ${item.cantidad_minima} unidades"

        // Lógica de Stock Crítico
        val isStockBajo = item.cantidad < item.cantidad_minima

        if (isStockBajo) {
            holder.card.setCardBackgroundColor(Color.parseColor("#FFF0F0")) // Fondo rojizo
            holder.ivEstatus.setImageResource(R.drawable.ic_warning)
            holder.ivEstatus.setColorFilter(Color.parseColor("#F44336")) // Rojo
            holder.progressStock.setIndicatorColor(Color.parseColor("#F44336"))
            holder.tvCantidades.setTextColor(Color.parseColor("#F44336"))
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE) // Fondo normal
            holder.ivEstatus.setImageResource(R.drawable.ic_check)
            holder.ivEstatus.setColorFilter(Color.parseColor("#4CAF50")) // Verde
            holder.progressStock.setIndicatorColor(Color.parseColor("#388E3C"))
            holder.tvCantidades.setTextColor(Color.BLACK)
        }

        // Llenado de barra (calcula porcentaje)
        val maxProgress = if (item.cantidad_minima > 0) item.cantidad_minima * 2 else 100
        holder.progressStock.max = maxProgress
        holder.progressStock.progress = item.cantidad
    }

    override fun getItemCount() = inventarioList.size

    fun actualizarLista(nuevaLista: List<Inventario>) {
        inventarioList = nuevaLista
        notifyDataSetChanged()
    }
}