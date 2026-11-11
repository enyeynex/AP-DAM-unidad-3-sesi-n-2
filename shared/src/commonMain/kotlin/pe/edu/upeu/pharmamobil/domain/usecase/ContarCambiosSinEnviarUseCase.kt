package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.DatosLocalesRepository

/**
 * Guía 14 (hueco 8): cuántos cambios se perderían si el usuario cierra
 * sesión ahora. Con 0 se cierra sin preguntar; con N > 0 la interfaz pide
 * confirmación.
 */
class ContarCambiosSinEnviarUseCase(
    private val datosLocales: DatosLocalesRepository
) {
    suspend operator fun invoke(): Int =
        resultadoDe { datosLocales.contarSinEnviar() }.getOrDefault(0)
}
