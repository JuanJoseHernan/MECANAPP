
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

    // Esta es la consulta clave para llenar tus tarjetas
    @Transaction
    @Query("SELECT * FROM clientes")
    suspend fun getClientesConVehiculos(): List<ClienteConVehiculos>

    @Query("SELECT * FROM clientes")
    suspend fun getAllClientes(): List<Cliente>

    @Update
    suspend fun update(cliente: Cliente)

    @Delete
    suspend fun delete(cliente: Cliente)
}