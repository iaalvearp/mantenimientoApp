package com.alpes.mantenimientoapp

import androidx.room.*

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTarea(tarea: Tarea)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: Equipo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEstado(estado: Estado)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCliente(cliente: Cliente)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProyecto(proyecto: Proyecto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProvincia(provincia: Provincia)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividadMantenimiento(actividad: ActividadMantenimiento)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPosibleRespuesta(respuesta: PosibleRespuesta)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRol(rol: Rol)

    @Query("SELECT * FROM equipos WHERE tareaId = :idDeLaTarea")
    suspend fun obtenerEquiposPorTarea(idDeLaTarea: Int): List<Equipo>

    @Query("SELECT * FROM tareas WHERE usuarioId = :idDelUsuario")
    suspend fun obtenerTareasPorUsuario(idDelUsuario: Int): List<Tarea>

    @Query("SELECT * FROM usuarios WHERE id = :userId LIMIT 1")
    suspend fun obtenerUsuarioPorId(userId: Int): Usuario?

    @Query("SELECT * FROM roles WHERE id = :rolId LIMIT 1")
    suspend fun obtenerRolPorId(rolId: Int): Rol?

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun obtenerUsuarioPorCredenciales(email: String, password: String): Usuario?

    @Query("SELECT * FROM equipos WHERE id = :equipoId LIMIT 1")
    suspend fun obtenerEquipoPorId(equipoId: String): Equipo?

    @Query("SELECT * FROM tareas WHERE id = :tareaId LIMIT 1")
    suspend fun obtenerTareaPorId(tareaId: Int): Tarea?

    @Query("SELECT * FROM clientes WHERE id = :clienteId LIMIT 1")
    suspend fun obtenerClientePorId(clienteId: Int): Cliente?

    @Query("SELECT * FROM proyectos WHERE id = :proyectoId LIMIT 1")
    suspend fun obtenerProyectoPorId(proyectoId: Int): Proyecto?

    @Query("SELECT * FROM provincias WHERE id = :provinciaId LIMIT 1")
    suspend fun obtenerProvinciaPorId(provinciaId: Int): Provincia?

    @Query("SELECT * FROM actividades_mantenimiento")
    suspend fun obtenerActividadesMantenimiento(): List<ActividadMantenimiento>

    @Query("SELECT * FROM posibles_respuestas")
    suspend fun obtenerPosiblesRespuestas(): List<PosibleRespuesta>

    @Transaction
    @Query("SELECT * FROM actividades_mantenimiento WHERE tipo = :tipo")
    suspend fun obtenerActividadesConRespuestas(tipo: String): List<ActividadConRespuestas>
}