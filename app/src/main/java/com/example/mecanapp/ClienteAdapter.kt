package com.example.mecanapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mecanapp.data.Cliente // Importante importar Cliente
import com.example.mecanapp.data.ClienteConVehiculos

class ClienteAdapter(
    private var clientesList: List<ClienteConVehiculos>,
    private val onItemClick: (Cliente) -> Unit // NUEVO: Callback para clics
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    // 1. Enlaza los IDs del diseño de la tarjeta
    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreCliente)
        val tvTelefono = view.findViewById<TextView>(R.id.tvTelefonoCliente)
       // val tvCorreo = view.findViewById<TextView>(R.id.tvCorreoCliente)
        val tvPlaca = view.findViewById<TextView>(R.id.tvPlacaVehiculo)
        val tvDetalle = view.findViewById<TextView>(R.id.tvDetalleVehiculo)
    }

    // 2. Crea la tarjeta visualmente
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        val viewHolder = ClienteViewHolder(view)

        // Configuramos el clic en la tarjeta completa
        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Pasamos solo el objeto Cliente a la función
                onItemClick(clientesList[position].cliente)
            }
        }

        return viewHolder
    }

    // 3. Pega los datos de la BD en la tarjeta
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val item = clientesList[position]

        holder.tvNombre.text = item.cliente.nombre
        holder.tvTelefono.text = item.cliente.telefono ?: "Sin teléfono"
        // holder.tvCorreo.text = item.cliente.correo ?: "Sin correo" // NUEVO: Mostrar correo

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