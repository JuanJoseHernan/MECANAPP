package com.example.mecanapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario)

    @Query("SELECT * FROM usuarios")
    suspend fun getAllUsuarios(): List<Usuario>

    // --- NUEVO: Función para actualizar el estado del mecánico ---
    @Query("UPDATE usuarios SET estado = :nuevoEstado WHERE id_usuario = :idUsuario")
    suspend fun actualizarEstado(idUsuario: Int, nuevoEstado: String)
}