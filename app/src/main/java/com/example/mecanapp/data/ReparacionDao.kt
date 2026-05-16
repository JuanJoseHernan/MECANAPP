package com.example.mecanapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Clase de apoyo para la UI
data class ReparacionDisplay(
    val id_reparacion: Int,
    val nombreCliente: String,
    val placas: String?,
    val marca: String?,
    val modelo: String?,
    val nombreMecanico: String?,
    val fecha_fin: String?,
    val estado: String?
)

@Dao
interface ReparacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reparacion: Reparacion)

    @Query("SELECT COUNT(*) FROM reparaciones")
    suspend fun getReparacionesCount(): Int

    @Query("UPDATE reparaciones SET estado = :nuevoEstado WHERE id_reparacion = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    @Query("""
        SELECT r.id_reparacion, cl.nombre AS nombreCliente, v.placas, v.marca, v.modelo, 
               u.nombre AS nombreMecanico, r.fecha_fin, r.estado
        FROM reparaciones r
        INNER JOIN vehiculos v ON r.id_vehiculo = v.id_vehiculo
        INNER JOIN clientes cl ON v.id_cliente = cl.id_cliente
        LEFT JOIN usuarios u ON r.id_usuario = u.id_usuario
        ORDER BY r.id_reparacion DESC
    """)
    suspend fun getTodasLasReparaciones(): List<ReparacionDisplay>

    // --- NUEVA FUNCIÓN AÑADIDA ---
    // Nos permite obtener una reparación específica para saber qué mecánico tenía asignado
    @Query("SELECT * FROM reparaciones WHERE id_reparacion = :id")
    suspend fun getReparacionById(id: Int): Reparacion?
}