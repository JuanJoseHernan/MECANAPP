package com.example.mecanapp.data

import androidx.room.*

@Dao
interface ReparacionDetalleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarServicio(servicio: Servicio): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun vincularServicio(reparacionServicio: ReparacionServicio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun vincularRefaccion(reparacionRefaccion: ReparacionRefaccion)

    @Query("UPDATE inventario SET cantidad = cantidad - :usado WHERE id_refaccion = :id")
    suspend fun descontarInventario(id: Int, usado: Int)
}