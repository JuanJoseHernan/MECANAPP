package com.example.mecanapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente): Long


    @Query("SELECT * FROM clientes")
    suspend fun getAllClientes(): List<Cliente>


    @Query("SELECT * FROM clientes WHERE nombre LIKE '%' || :busqueda || '%' OR telefono LIKE '%' || :busqueda || '%'")
    suspend fun buscarClientes(busqueda: String): List<Cliente>

    @Update
    suspend fun update(cliente: Cliente)

    @Delete
    suspend fun delete(cliente: Cliente)
}