package com.example.mecanapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Clases de apoyo para agrupar los datos de los JOINs
data class DetalleOrdenBase(
    val placas: String?,
    val marca: String?,
    val modelo: String?,
    val nombre_cliente: String,
    val nombre_mecanico: String?,
    val fecha_fin: String?
)

data class DetalleServicio(val nombre: String, val precio: Double?)
data class DetalleRefaccion(val nombre: String, val cantidad: Int)

@Dao
interface ReparacionDetalleDao {

    @Insert
    suspend fun insertarServicio(servicio: Servicio): Long

    @Insert
    suspend fun vincularServicio(reparacionServicio: ReparacionServicio)

    @Insert
    suspend fun vincularRefaccion(reparacionRefaccion: ReparacionRefaccion)

    @Query("UPDATE inventario SET cantidad = cantidad - :cantidad WHERE id_refaccion = :idRefaccion")
    suspend fun descontarInventario(idRefaccion: Int, cantidad: Int)

    // --- NUEVAS CONSULTAS PARA VER LOS DETALLES ---

    @Query("""
        SELECT v.placas, v.marca, v.modelo, c.nombre AS nombre_cliente, u.nombre AS nombre_mecanico, r.fecha_fin 
        FROM reparaciones r 
        INNER JOIN vehiculos v ON r.id_vehiculo = v.id_vehiculo 
        INNER JOIN clientes c ON v.id_cliente = c.id_cliente 
        LEFT JOIN usuarios u ON r.id_usuario = u.id_usuario 
        WHERE r.id_reparacion = :idReparacion
    """)
    suspend fun getDetalleBase(idReparacion: Int): DetalleOrdenBase

    @Query("""
        SELECT s.nombre, s.precio 
        FROM servicios s 
        INNER JOIN reparacion_servicios rs ON s.id_servicio = rs.id_servicio 
        WHERE rs.id_reparacion = :idReparacion
    """)
    suspend fun getServiciosDeReparacion(idReparacion: Int): List<DetalleServicio>

    @Query("""
        SELECT i.nombre, rr.cantidad 
        FROM inventario i 
        INNER JOIN reparacion_refacciones rr ON i.id_refaccion = rr.id_refaccion 
        WHERE rr.id_reparacion = :idReparacion
    """)
    suspend fun getRefaccionesDeReparacion(idReparacion: Int): List<DetalleRefaccion>
}