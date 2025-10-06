package com.alpes.mantenimientoapp
import androidx.room.*

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarUsuario(usuario: Usuario)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarTarea(tarea: Tarea)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarEquipo(equipo: Equipo)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarEstado(estado: Estado)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarCliente(cliente: Cliente)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarProyecto(proyecto: Proyecto)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarProvincia(provincia: Provincia)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarActividadMantenimiento(actividad: ActividadMantenimiento): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarPosibleRespuesta(respuesta: PosibleRespuesta)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarRol(rol: Rol)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarUnidadNegocio(unidadNegocio: UnidadNegocio)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarAgencia(agencia: Agencia)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarCiudad(ciudad: Ciudad)
    @Insert suspend fun insertarResultado(resultado: MantenimientoResultado)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarFinalizacion(finalizacion: MantenimientoFinal)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertarMantenimientoFoto(foto: MantenimientoFoto)
    @Query("UPDATE equipos SET estadoId = :newStatusId WHERE id = :equipoId")
    suspend fun updateEquipoStatus(equipoId: String, newStatusId: Int)
    @Query("UPDATE equipos SET tareaId = :tareaId WHERE id = :equipoId")
    suspend fun vincularEquipoConTarea(equipoId: String, tareaId: Int)

    @Query("SELECT * FROM tareas WHERE usuarioId = :idDelUsuario")
    suspend fun obtenerTareasPorUsuario(idDelUsuario: Int): List<Tarea>
    @Query("SELECT * FROM equipos WHERE tareaId = :idDeLaTarea")
    suspend fun obtenerEquiposPorTareaAsignada(idDeLaTarea: Int): List<Equipo>
    @Query("SELECT * FROM equipos WHERE tareaId IN (:idsDeTareas)")
    suspend fun obtenerEquiposPorTareas(idsDeTareas: List<Int>): List<Equipo>
    @Query("SELECT * FROM equipos WHERE tareaId < 0 AND creadoPorUsuarioId = :idDelUsuario")
    suspend fun obtenerEquiposLocalesPorUsuario(idDelUsuario: Int): List<Equipo>
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
    @Transaction
    @Query("SELECT * FROM actividades_mantenimiento WHERE tipo = :tipo")
    suspend fun obtenerActividadesConRespuestas(tipo: String): List<ActividadConRespuestas>
    @Query("SELECT * FROM ciudades WHERE id = :ciudadId LIMIT 1")
    suspend fun obtenerCiudadPorId(ciudadId: Int): Ciudad?
    @Query("SELECT * FROM agencias WHERE id = :agenciaId LIMIT 1")
    suspend fun obtenerAgenciaPorId(agenciaId: Int): Agencia?
    @Query("SELECT * FROM unidades_negocio WHERE id = :unidadNegocioId LIMIT 1")
    suspend fun obtenerUnidadNegocioPorId(unidadNegocioId: Int): UnidadNegocio?
    @Query("SELECT * FROM mantenimiento_fotos WHERE equipoId = :equipoId AND tipoFoto = :tipoFoto")
    suspend fun obtenerFotosPorEquipoYTipo(equipoId: String, tipoFoto: String): List<MantenimientoFoto>
    @Query("SELECT * FROM clientes ORDER BY nombreCompleto ASC")
    suspend fun getAllClientes(): List<Cliente>
    @Query("SELECT * FROM proyectos WHERE clienteId = :idCliente ORDER BY nombre ASC")
    suspend fun getProyectosByCliente(idCliente: Int): List<Proyecto>
    @Query("SELECT * FROM provincias ORDER BY nombre ASC")
    suspend fun getAllProvincias(): List<Provincia>
    @Query("SELECT * FROM ciudades WHERE provinciaId = :idDeLaProvincia ORDER BY nombre ASC")
    suspend fun obtenerCiudadesPorProvincia(idDeLaProvincia: Int): List<Ciudad>
    @Query("SELECT * FROM unidades_negocio ORDER BY nombre ASC")
    suspend fun getAllUnidadesNegocio(): List<UnidadNegocio>
    @Query("SELECT * FROM agencias WHERE ciudadId = :idDeLaCiudad ORDER BY nombre ASC")
    suspend fun getAgenciasByCiudad(idDeLaCiudad: Int): List<Agencia>
    @Query("SELECT DISTINCT modelo FROM equipos WHERE modelo != '' ORDER BY modelo ASC")
    suspend fun getUniqueModelos(): List<String>
    @Query("SELECT nombre, caracteristicas FROM equipos WHERE modelo = :modelo LIMIT 1")
    suspend fun getDetailsForModel(modelo: String): EquipoDetalles?

    @Query("SELECT DISTINCT un.* FROM unidades_negocio un INNER JOIN agencias a ON un.id = a.unidadNegocioId WHERE a.ciudadId = :idDeLaCiudad ORDER BY un.nombre ASC")
    suspend fun getUnidadesNegocioByCiudad(idDeLaCiudad: Int): List<UnidadNegocio>

    @Query("SELECT * FROM agencias LIMIT 10")
    suspend fun DEBUG_getAllAgencias(): List<Agencia>

    @Query("SELECT MIN(id) FROM tareas")
    suspend fun getMinTareaId(): Int?
}