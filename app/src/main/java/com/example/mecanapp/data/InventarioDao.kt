package com.example.mecanapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface InventarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refaccion: Inventario)

    @Query("SELECT * FROM inventario")
    suspend fun getInventario(): List<Inventario>

    @Update
    suspend fun update(refaccion: Inventario)

    @Delete
    suspend fun delete(refaccion: Inventario)

    @Query("SELECT COUNT(*) FROM inventario")
    suspend fun getInventarioCount(): Int
}