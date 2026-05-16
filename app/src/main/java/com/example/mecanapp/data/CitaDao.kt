package com.example.mecanapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

data class CitaItem(
    val id_cita: Int,
    val nombreCliente: String,
    val estado: String?,
    val fecha: String?,
    val hora: String?,
    val placas: String?,
    val marca: String?,
    val modelo: String?,
    val servicio: String?
)

data class CitaParaOrden(
    val id_cita: Int,
    val id_vehiculo: Int,
    val id_usuario: Int?,
    val nombreCliente: String,
    val placas: String?,
    val vehiculo: String?
)

@Dao
interface CitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cita: Cita)

    @Query("SELECT COUNT(*) FROM citas")
    suspend fun getCitasCount(): Int

    @Query("SELECT COUNT(*) FROM citas WHERE fecha = :fecha")
    suspend fun getCitasDelDiaCount(fecha: String): Int

    @Query("UPDATE citas SET estado = :nuevoEstado WHERE id_cita = :idCita")
    suspend fun actualizarEstado(idCita: Int, nuevoEstado: String)

    @Query("""
        SELECT c.id_cita, cl.nombre AS nombreCliente, c.estado, c.fecha, c.hora, 
               v.placas, v.marca, v.modelo, c.descripcion AS servicio
        FROM citas c
        INNER JOIN vehiculos v ON c.id_vehiculo = v.id_vehiculo
        INNER JOIN clientes cl ON v.id_cliente = cl.id_cliente
        ORDER BY c.fecha DESC, c.hora DESC
    """)
    suspend fun getTodasLasCitas(): List<CitaItem>

    @Query("""
        SELECT c.id_cita, cl.nombre AS nombreCliente, c.estado, c.fecha, c.hora, 
               v.placas, v.marca, v.modelo, c.descripcion AS servicio
        FROM citas c
        INNER JOIN vehiculos v ON c.id_vehiculo = v.id_vehiculo
        INNER JOIN clientes cl ON v.id_cliente = cl.id_cliente
        WHERE cl.nombre LIKE '%' || :busqueda || '%'
        ORDER BY c.fecha DESC, c.hora DESC
    """)
    suspend fun buscarCitas(busqueda: String): List<CitaItem>

    // ¡Ahora sí está ADENTRO de la interfaz!
    @Query("""
        SELECT c.id_cita, c.id_vehiculo, c.id_usuario, cl.nombre AS nombreCliente, v.placas, (v.marca || ' ' || v.modelo) AS vehiculo
        FROM citas c
        INNER JOIN vehiculos v ON c.id_vehiculo = v.id_vehiculo
        INNER JOIN clientes cl ON v.id_cliente = cl.id_cliente
        WHERE c.estado = 'Completada'
    """)
    suspend fun getCitasCompletadasParaOrden(): List<CitaParaOrden>
}