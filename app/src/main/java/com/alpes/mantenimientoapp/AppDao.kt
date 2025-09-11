package com.alpes.mantenimientoapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// ANDROID/ROOM: La anotación @Dao le dice a Room que esta es una interfaz de acceso a datos.
@Dao
interface AppDao {

    // ANDROID/ROOM: @Insert define una función para insertar datos.
    // 'onConflict' le dice a Room qué hacer si intentamos insertar un dato que ya existe.
    // 'REPLACE' significa que si el dato ya existe, lo reemplazará con el nuevo.
    // Esto es muy útil para cuando sincronicemos con la API.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTarea(tarea: Tarea)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: Equipo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEstado(estado: Estado)


    // ANDROID/ROOM: @Query nos permite hacer consultas personalizadas a la base de datos.
    // Escribimos la consulta en lenguaje SQL. Esta, por ejemplo, selecciona todos
    // los equipos que pertenecen a un ID de tarea específico.
    @Query("SELECT * FROM equipos WHERE tareaId = :idDeLaTarea")
    suspend fun obtenerEquiposPorTarea(idDeLaTarea: Int): List<Equipo>

    // Esta consulta nos traerá todas las tareas asignadas a un usuario específico.
    @Query("SELECT * FROM tareas WHERE usuarioId = :idDelUsuario")
    suspend fun obtenerTareasPorUsuario(idDelUsuario: Int): List<Tarea>

    @Query("SELECT * FROM usuarios WHERE id = :userId LIMIT 1")
    suspend fun obtenerUsuarioPorId(userId: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE nombre = :nombre AND password = :password LIMIT 1")
    suspend fun obtenerUsuarioPorCredenciales(nombre: String, password: String): Usuario?
}