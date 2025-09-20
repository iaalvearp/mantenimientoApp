package com.alpes.mantenimientoapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ANDROID/ROOM: @Database le dice a Room que esta clase es una base de datos.
// 'entities': Aquí listamos todos nuestros "moldes" (las tablas que tendrá la base de datos).
// 'version': Es un número de versión. Si en el futuro cambiamos la estructura de las tablas,
//            tendremos que aumentar este número. Por ahora, es 1.
// 'exportSchema': Es una opción avanzada, la dejamos en 'false' por ahora.
@Database(
    entities = [
        Usuario::class, Tarea::class, Equipo::class, Estado::class,
        Cliente::class, Proyecto::class, Provincia::class, Rol::class,
        ActividadMantenimiento::class, PosibleRespuesta::class,
        // --- AÑADIMOS LAS NUEVAS TABLAS ---
        Ciudad::class, Agencia::class, UnidadNegocio::class,
        // --- FIN DE LAS NUEVAS TABLAS ---
        MantenimientoResultado::class
    ],
    version = 10, // <-- AUMENTAMOS LA VERSIÓN
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // KOTLIN: Esta función abstracta conecta la base de datos con nuestro "intérprete" (el DAO).
    abstract fun appDao(): AppDao

    // KOTLIN: Un 'companion object' es un lugar donde podemos definir funciones y propiedades
    // que pertenecen a la clase en general, no a una instancia específica. Lo usaremos
    // para asegurarnos de que solo exista UNA instancia de la base de datos en toda la app.
    // Esto se conoce como "patrón Singleton" y es crucial para el rendimiento.
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mantenimiento_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}