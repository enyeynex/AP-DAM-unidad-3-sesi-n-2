package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.DatosLocalesRepository

/**
 * Cierra la sesión. Primero borra el token: si la limpieza local fallara,
 * igual nadie podría seguir hablando con PharmaSoft con esa sesión.
 *
 * Guía 14 (hueco 8):
 * - Cierre manual (el usuario pulsó «Cerrar sesión»): se vacía la base local,
 *   para que el siguiente usuario del teléfono no vea el inventario ni la cola
 *   del anterior.
 * - Cierre por un 401 (conservarDatosLocales = true): el usuario no eligió
 *   salir; el token venció o fue revocado. Se conservan los cambios sin enviar
 *   para que, al volver a entrar, la sincronización los envíe y no se pierda
 *   trabajo hecho sin red.
 */
class CerrarSesionUseCase(
    private val authRepository: AuthRepository,
    private val datosLocales: DatosLocalesRepository
) {
    suspend operator fun invoke(conservarDatosLocales: Boolean = false): Result<Unit> = resultadoDe {
        authRepository.cerrarSesion()
        if (!conservarDatosLocales) {
            datosLocales.borrarTodo()
        }
    }
}
