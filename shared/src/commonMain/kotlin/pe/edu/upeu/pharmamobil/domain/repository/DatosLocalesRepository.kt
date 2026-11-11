package pe.edu.upeu.pharmamobil.domain.repository

/**
 * Guía 14 (hueco 8): lo que «Cerrar sesión» necesita de la base local.
 * Es una interfaz aparte de ProductoLocalRepository para que el cierre de
 * sesión no dependa de las operaciones de sincronización.
 */
interface DatosLocalesRepository {
    /** Filas creadas sin red que PharmaSoft todavía no tiene (en cola, agotadas o rechazadas). */
    fun contarSinEnviar(): Int

    /** Vacía el inventario local y la marca de última sincronización. */
    fun borrarTodo()
}
