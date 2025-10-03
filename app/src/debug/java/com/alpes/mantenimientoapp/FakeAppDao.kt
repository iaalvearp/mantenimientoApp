// Archivo: app/src/debug/java/com/alpes/mantenimientoapp/FakeAppDao.kt
package com.alpes.mantenimientoapp

import androidx.room.Transaction

// Esta es nuestra única y centralizada implementación falsa del DAO para todas las vistas previas.
// No es 'private', para que todos puedan usarla.
class FakeAppDao : AppDao {
    override suspend fun insertarUsuario(usuario: Usuario) {}
    override suspend fun insertarTarea(tarea: Tarea) {}
    override suspend fun insertarEquipo(equipo: Equipo) {}
    override suspend fun insertarEstado(estado: Estado) {}
    override suspend fun insertarCliente(cliente: Cliente) {}
    override suspend fun insertarProyecto(proyecto: Proyecto) {}
    override suspend fun insertarProvincia(provincia: Provincia) {}
    override suspend fun insertarActividadMantenimiento(actividad: ActividadMantenimiento) {}
    override suspend fun insertarPosibleRespuesta(respuesta: PosibleRespuesta) {}
    override suspend fun insertarRol(rol: Rol) {}
    override suspend fun insertarUnidadNegocio(unidadNegocio: UnidadNegocio) {}
    override suspend fun insertarAgencia(agencia: Agencia) {}
    override suspend fun insertarCiudad(ciudad: Ciudad) {}
    override suspend fun insertarResultado(resultado: MantenimientoResultado) {}
    override suspend fun insertarFinalizacion(finalizacion: MantenimientoFinal) {}
    override suspend fun updateEquipoStatus(equipoId: String, newStatusId: Int) {}
    override suspend fun insertarMantenimientoFoto(foto: MantenimientoFoto) {}
    override suspend fun vincularEquipoConTarea(equipoId: String, tareaId: Int) {}
    override suspend fun obtenerTareasPorUsuario(idDelUsuario: Int): List<Tarea> = emptyList()
    override suspend fun obtenerEquiposPorTareaAsignada(idDeLaTarea: Int): List<Equipo> = emptyList()
    override suspend fun obtenerEquiposPorTareas(idsDeTareas: List<Int>): List<Equipo> = emptyList()
    override suspend fun obtenerEquiposLocalesPorUsuario(idDelUsuario: Int): List<Equipo> = emptyList()
    override suspend fun obtenerUsuarioPorId(userId: Int): Usuario? = null
    override suspend fun obtenerRolPorId(rolId: Int): Rol? = null
    override suspend fun obtenerUsuarioPorCredenciales(email: String, password: String): Usuario? = null
    override suspend fun obtenerEquipoPorId(equipoId: String): Equipo? = null
    override suspend fun obtenerTareaPorId(tareaId: Int): Tarea? = null
    override suspend fun obtenerClientePorId(clienteId: Int): Cliente? = null
    override suspend fun obtenerProyectoPorId(proyectoId: Int): Proyecto? = null
    override suspend fun obtenerProvinciaPorId(provinciaId: Int): Provincia? = null
    @Transaction override suspend fun obtenerActividadesConRespuestas(tipo: String): List<ActividadConRespuestas> = emptyList()
    override suspend fun obtenerCiudadPorId(ciudadId: Int): Ciudad? = null
    override suspend fun obtenerAgenciaPorId(agenciaId: Int): Agencia? = null
    override suspend fun obtenerUnidadNegocioPorId(unidadNegocioId: Int): UnidadNegocio? = null
    override suspend fun obtenerFotosPorEquipoYTipo(equipoId: String, tipoFoto: String): List<MantenimientoFoto> = emptyList()
    override suspend fun getAllClientes(): List<Cliente> = emptyList()
    override suspend fun getProyectosByCliente(idCliente: Int): List<Proyecto> = emptyList()
    override suspend fun getAllProvincias(): List<Provincia> = emptyList()
    override suspend fun obtenerCiudadesPorProvincia(idDeLaProvincia: Int): List<Ciudad> = emptyList()
    override suspend fun getAllUnidadesNegocio(): List<UnidadNegocio> = emptyList()
    override suspend fun getAgenciasByCiudad(idDeLaCiudad: Int): List<Agencia> = emptyList()
    override suspend fun getUniqueModelos(): List<String> = emptyList()
    override suspend fun getDetailsForModel(modelo: String): EquipoDetalles? = null
    override suspend fun getUnidadesNegocioByCiudad(idDeLaCiudad: Int): List<UnidadNegocio> = emptyList()

}