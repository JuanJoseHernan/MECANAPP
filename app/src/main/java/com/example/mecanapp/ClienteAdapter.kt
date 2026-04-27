
package com.example.mecanapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.ClienteConVehiculos

class ClienteAdapter(private var clientesList: List<ClienteConVehiculos>) :
    RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    // 1. Enlaza los IDs del diseño de la tarjeta
    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreCliente)
        val tvTelefono = view.findViewById<TextView>(R.id.tvTelefonoCliente)
        val tvPlaca = view.findViewById<TextView>(R.id.tvPlacaVehiculo)
        val tvDetalle = view.findViewById<TextView>(R.id.tvDetalleVehiculo)
    }

    // 2. Crea la tarjeta visualmente
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    // 3. Pega los datos de la BD en la tarjeta
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val item = clientesList[position]

        holder.tvNombre.text = item.cliente.nombre
        holder.tvTelefono.text = item.cliente.telefono ?: "Sin teléfono"

        if (item.vehiculos.isNotEmpty()) {
            val vehiculo = item.vehiculos[0]
            holder.tvPlaca.text = vehiculo.placas ?: "S/P"
            holder.tvDetalle.text = "${vehiculo.marca ?: ""} ${vehiculo.modelo ?: ""} (${vehiculo.anio ?: ""})"
        } else {
            holder.tvPlaca.text = "Sin vehículo"
            holder.tvDetalle.text = "No hay datos"
        }
    }

    override fun getItemCount() = clientesList.size

    // 4. Sirve para recargar la lista cuando agregas uno nuevo
    fun actualizarLista(nuevaLista: List<ClienteConVehiculos>) {
        clientesList = nuevaLista
        notifyDataSetChanged()
    }
}