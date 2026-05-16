package com.example.mecanapp.data

import androidx.room.*

data class ClienteConVehiculos(
    @Embedded val cliente: Cliente,
    @Relation(
        parentColumn = "id_cliente",
        entityColumn = "id_cliente"
    )
    val vehiculos: List<Vehiculo>
)

@Dao
interface ClienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente): Long

    @Transaction
    @Query("SELECT * FROM clientes")
    suspend fun getClientesConVehiculos(): List<ClienteConVehiculos>

    @Query("SELECT COUNT(*) FROM clientes")
    suspend fun getClientesCount(): Int

    // ¡NUEVA FUNCIÓN! Busca por nombre o placa
    @Transaction
    @Query("""
        SELECT DISTINCT c.* FROM clientes c 
        LEFT JOIN vehiculos v ON c.id_cliente = v.id_cliente 
        WHERE c.nombre LIKE '%' || :busqueda || '%' 
        OR v.placas LIKE '%' || :busqueda || '%'
    """)
    suspend fun buscarClientesConVehiculos(busqueda: String): List<ClienteConVehiculos>

    @Update
    suspend fun update(cliente: Cliente)

    @Delete
    suspend fun delete(cliente: Cliente)
}