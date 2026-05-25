package com.example.mecanapp.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id_cliente: Int = 0,
    val nombre: String,
    val telefono: String?,
    val correo: String?
)

@Entity(
    tableName = "vehiculos",
    foreignKeys = [ForeignKey(entity = Cliente::class, parentColumns = ["id_cliente"], childColumns = ["id_cliente"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("id_cliente")]
)
data class Vehiculo(
    @PrimaryKey(autoGenerate = true) val id_vehiculo: Int = 0,
    val id_cliente: Int,
    val marca: String?,
    val modelo: String?,
    val anio: Int?,
    val placas: String?
)

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val nombre: String,
    val rol: String?,
    val telefono: String?,
    val correo: String?,
    val estado: String = "Disponible"
)

@Entity(tableName = "servicios")
data class Servicio(
    @PrimaryKey(autoGenerate = true) val id_servicio: Int = 0,
    val nombre: String,
    val descripcion: String?,
    val precio: Double?
)

@Entity(tableName = "inventario")
data class Inventario(
    @PrimaryKey(autoGenerate = true) val id_refaccion: Int = 0,
    val nombre: String,
    val descripcion: String?,
    val cantidad: Int = 0,
    val cantidad_minima: Int = 0,
    val precio: Double?
)

@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(entity = Vehiculo::class, parentColumns = ["id_vehiculo"], childColumns = ["id_vehiculo"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("id_vehiculo"), Index("id_usuario")]
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id_cita: Int = 0,
    val id_vehiculo: Int,
    val id_usuario: Int?,
    val fecha: String?,
    val hora: String?,
    val estado: String?,
    val descripcion: String?
)

@Entity(
    tableName = "reparaciones",
    foreignKeys = [
        ForeignKey(entity = Vehiculo::class, parentColumns = ["id_vehiculo"], childColumns = ["id_vehiculo"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("id_vehiculo"), Index("id_usuario")]
)
data class Reparacion(
    @PrimaryKey(autoGenerate = true) val id_reparacion: Int = 0,
    val id_vehiculo: Int,
    val id_usuario: Int?,
    val estado: String?,
    val fecha_inicio: String?,
    val fecha_fin: String?,
    val total_orden: Double = 0.0 // <-- NUEVO CAMPO PARA CONGELAR EL TOTAL
)

@Entity(
    tableName = "reparacion_servicios",
    primaryKeys = ["id_reparacion", "id_servicio"],
    foreignKeys = [
        ForeignKey(entity = Reparacion::class, parentColumns = ["id_reparacion"], childColumns = ["id_reparacion"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Servicio::class, parentColumns = ["id_servicio"], childColumns = ["id_servicio"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("id_servicio")]
)
data class ReparacionServicio(
    val id_reparacion: Int,
    val id_servicio: Int
)

@Entity(
    tableName = "reparacion_refacciones",
    primaryKeys = ["id_reparacion", "id_refaccion"],
    foreignKeys = [
        ForeignKey(entity = Reparacion::class, parentColumns = ["id_reparacion"], childColumns = ["id_reparacion"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Inventario::class, parentColumns = ["id_refaccion"], childColumns = ["id_refaccion"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("id_refaccion")]
)
data class ReparacionRefaccion(
    val id_reparacion: Int,
    val id_refaccion: Int,
    val cantidad: Int,
    val precio_cobrado: Double = 0.0 // <-- NUEVO CAMPO PARA CONGELAR EL PRECIO UNITARIO
)

@Database(
    entities = [
        Cliente::class, Vehiculo::class, Usuario::class,
        Servicio::class, Inventario::class, Cita::class,
        Reparacion::class, ReparacionServicio::class, ReparacionRefaccion::class
    ],
    version = 3, // <-- ACTUALIZADO A VERSIÓN 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun citaDao(): CitaDao
    abstract fun reparacionDao(): ReparacionDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun reparacionDetalleDao(): ReparacionDetalleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taller_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}