package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ContarCambiosSinEnviarUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Guía 14 (hueco 8) sobre una base SQLite real en memoria: cerrar sesión
 * a mano deja vacías las tablas producto y sincronizacion; un cierre por
 * 401 las conserva.
 */
class LimpiezaAlCerrarSesionTest {

    private class AuthSimulado : AuthRepository {
        var cerrada = false
        override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = error("no se usa")
        override suspend fun sesionGuardada(): Sesion? = null
        override suspend fun cerrarSesion() { cerrada = true }
    }

    private fun conDatos(): ProductoLocal {
        val local = ProductoLocal(baseEnMemoria())
        local.conciliar(listOf(Producto(7L, "Paracetamol", 12.5, 40)))
        local.guardarPendiente(Producto(0, "Ibuprofeno", 4.5, 20))
        local.guardarPendiente(Producto(0, "Amoxicilina", 9.0, 10))
        local.registrarSincronizacion(1_700_000_000_000L)
        return local
    }

    @Test
    fun `cuenta como sin enviar toda fila pendiente aunque este rechazada`() = runTest {
        val local = conDatos()
        local.marcarFallida(local.pendientes().first().id, "409 duplicado")

        assertEquals(2, local.contarSinEnviar())
        assertEquals(2, ContarCambiosSinEnviarUseCase(local)())
    }

    @Test
    fun `cerrar sesion a mano vacia producto y sincronizacion`() = runTest {
        val local = conDatos()
        val auth = AuthSimulado()

        val resultado = CerrarSesionUseCase(auth, local)()

        assertTrue(resultado.isSuccess)
        assertTrue(auth.cerrada)
        assertTrue(local.observar().first().isEmpty())
        assertEquals(0, local.contarSinEnviar())
        assertNull(local.ultimaSincronizacion().first())
    }

    @Test
    fun `un cierre por 401 conserva el inventario y la cola`() = runTest {
        val local = conDatos()
        val auth = AuthSimulado()

        CerrarSesionUseCase(auth, local)(conservarDatosLocales = true)

        assertTrue(auth.cerrada)
        assertEquals(3, local.observar().first().size)
        assertEquals(2, local.contarSinEnviar())
    }
}
