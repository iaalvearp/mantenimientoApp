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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUnidadNegocio(unidadNegocio: UnidadNegocio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAgencia(agencia: Agencia)

    // En AppDao.kt, dentro de la interfaz
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCiudad(ciudad: Ciudad)

    @Insert
    suspend fun insertarResultado(resultado: MantenimientoResultado)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFinalizacion(finalizacion: MantenimientoFinal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMantenimientoFoto(foto: MantenimientoFoto)

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

    @Query("SELECT * FROM ciudades WHERE id = :ciudadId LIMIT 1")
    suspend fun obtenerCiudadPorId(ciudadId: Int): Ciudad?

    @Query("SELECT * FROM agencias WHERE id = :agenciaId LIMIT 1")
    suspend fun obtenerAgenciaPorId(agenciaId: Int): Agencia?

    @Query("SELECT * FROM ciudades WHERE provinciaId = :idDeLaProvincia")
    suspend fun obtenerCiudadesPorProvincia(idDeLaProvincia: Int): List<Ciudad>

    @Query("SELECT * FROM unidades_negocio WHERE provinciaId = :idDeLaProvincia")
    suspend fun obtenerUnidadesNegocioPorProvincia(idDeLaProvincia: Int): List<UnidadNegocio>

    @Query("SELECT * FROM agencias WHERE unidadNegocioId = :idDeLaUnidad")
    suspend fun obtenerAgenciasPorUnidadNegocio(idDeLaUnidad: Int): List<Agencia>

    @Query("SELECT * FROM unidades_negocio WHERE id = :unidadNegocioId LIMIT 1")
    suspend fun obtenerUnidadNegocioPorId(unidadNegocioId: Int): UnidadNegocio?

    @Query("UPDATE equipos SET estadoId = :newStatusId WHERE id = :equipoId")
    suspend fun updateEquipoStatus(equipoId: String, newStatusId: Int)

    @Query("SELECT * FROM mantenimiento_fotos WHERE equipoId = :equipoId AND tipoFoto = :tipoFoto")
    suspend fun obtenerFotosPorEquipoYTipo(equipoId: String, tipoFoto: String): List<MantenimientoFoto>
}