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
        Usuario::class,
        Tarea::class,
        Equipo::class,
        Estado::class,
        // --- AÑADE ESTAS LÍNEAS ---
        Cliente::class,
        Proyecto::class,
        Provincia::class
    ],
    version = 2, // <-- IMPORTANTE: Aumenta la versión a 2
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
        // KOTLIN: '@Volatile' asegura que el valor de la variable INSTANCE
        // sea siempre el más actualizado y visible para todos los hilos de ejecución.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, la devolvemos. Si no, la creamos.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mantenimiento_database" // Este será el nombre del archivo de la base de datos en el dispositivo.
                ).build()
                INSTANCE = instance
                // devolvemos la instancia
                instance
            }
        }
    }
}