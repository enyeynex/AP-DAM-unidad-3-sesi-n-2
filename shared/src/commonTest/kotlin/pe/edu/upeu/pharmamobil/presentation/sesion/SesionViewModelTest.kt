package pe.edu.upeu.pharmamobil.presentation.sesion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.FalloSesionException
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.DatosLocalesRepository
import pe.edu.upeu.pharmamobil.domain.sesion.AvisosDeSesion
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ContarCambiosSinEnviarUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SesionGuardadaUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    private val admin = Sesion("tok", "admin", "Carlos Ramirez Torres", setOf("CATALOGO_ESCRIBIR", "CLIENTES_LEER"))

    private class FakeAuthRepository(
        var guardada: Sesion? = null,
        var falloLogin: Throwable? = null,
        val sesionLogin: Sesion? = null
    ) : AuthRepository {
        var cerrada = false
        override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
            falloLogin?.let { throw it }
            return sesionLogin!!
        }
        override suspend fun sesionGuardada(): Sesion? = guardada
        override suspend fun cerrarSesion() { cerrada = true; guardada = null }
    }

    /** Base local simulada: cuenta filas sin enviar y registra si se vació. */
    private class FakeDatosLocales(var sinEnviar: Int = 0, var filas: Int = 3) : DatosLocalesRepository {
        var borrada = false
        override fun contarSinEnviar(): Int = sinEnviar
        override fun borrarTodo() { borrada = true; filas = 0; sinEnviar = 0 }
    }

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun nuevoViewModel(
        repositorio: FakeAuthRepository,
        avisos: AvisosDeSesion = AvisosDeSesion(),
        local: FakeDatosLocales = FakeDatosLocales()
    ) =
        SesionViewModel(
            iniciarSesion = IniciarSesionUseCase(repositorio),
            sesionGuardada = SesionGuardadaUseCase(repositorio),
            cerrarSesion = CerrarSesionUseCase(repositorio, local),
            avisos = avisos,
            contarCambiosSinEnviar = ContarCambiosSinEnviarUseCase(local)
        )

    @Test
    fun sinTokenGuardadoMuestraElLogin() = runTest {
        val estado = nuevoViewModel(FakeAuthRepository()).uiState.value
        assertFalse(estado.verificando)
        assertNull(estado.sesion)
    }

    @Test
    fun conTokenGuardadoEntraDirectoSinPedirCredenciales() = runTest {
        val estado = nuevoViewModel(FakeAuthRepository(guardada = admin)).uiState.value
        assertEquals(admin, estado.sesion)
    }

    @Test
    fun unLoginCorrectoAbreLaSesionYLimpiaLaClave() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(sesionLogin = admin))

        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("Admin123")
        viewModel.iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals(admin, estado.sesion)
        assertEquals("", estado.clave)
        assertFalse(estado.cargando)
    }

    @Test
    fun loginConCamposVaciosNoLlegaAlServidor() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(falloLogin = IllegalStateException("no deberia llamarse")))

        viewModel.iniciarSesion()

        assertEquals("El usuario es obligatorio", viewModel.uiState.value.error)
    }

    @Test
    fun credencialesIncorrectasMuestranUnError() = runTest {
        val viewModel = nuevoViewModel(
            FakeAuthRepository(falloLogin = FalloSesionException(ErrorSesion.CredencialesInvalidas))
        )

        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("mala")
        viewModel.iniciarSesion()

        assertEquals("Usuario o contraseña incorrectos", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.sesion)
    }

    @Test
    fun unAvisoDe401CierraLaSesionYVuelveAlLoginConMensaje() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val avisos = AvisosDeSesion()
        val viewModel = nuevoViewModel(repositorio, avisos)

        avisos.notificar(ErrorSesion.SesionExpirada)

        val estado = viewModel.uiState.value
        assertNull(estado.sesion)
        assertTrue(repositorio.cerrada)
        assertEquals("Tu sesión expiró. Vuelve a iniciar sesión", estado.aviso)
        // La interfaz queda limpia: no se muestra el usuario anterior.
        assertEquals("", estado.usuario)
    }

    @Test
    fun unTokenAlteradoExplicaElMotivoSinCulparALasCredenciales() = runTest {
        val avisos = AvisosDeSesion()
        val viewModel = nuevoViewModel(FakeAuthRepository(guardada = admin), avisos)

        avisos.notificar(ErrorSesion.CredencialesInvalidas, "El token no es válido")

        assertEquals(
            "Tu sesión terminó (HTTP 401). PharmaSoft respondió: «El token no es válido»",
            viewModel.uiState.value.aviso
        )
        assertNull(viewModel.uiState.value.sesion)
    }

    @Test
    fun cerrarSesionNoDejaElUsuarioAnteriorEnElLogin() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(sesionLogin = admin))
        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("Admin123")
        viewModel.iniciarSesion()

        viewModel.cerrarSesion()

        assertEquals("", viewModel.uiState.value.usuario)
        assertEquals("", viewModel.uiState.value.clave)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun cerrarSesionVuelveAlLoginSinAviso() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val viewModel = nuevoViewModel(repositorio)

        viewModel.cerrarSesion()

        assertNull(viewModel.uiState.value.sesion)
        assertNull(viewModel.uiState.value.aviso)
        assertTrue(repositorio.cerrada)
    }

    @Test
    fun cadaSesionNuevaCambiaElIdentificador() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(guardada = admin, sesionLogin = admin))
        val primera = viewModel.uiState.value.idSesion

        viewModel.cerrarSesion()
        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("Admin123")
        viewModel.iniciarSesion()

        assertNotNull(viewModel.uiState.value.sesion)
        assertTrue(viewModel.uiState.value.idSesion > primera)
    }

    // --- Guía 14, hueco 8: la base local al cerrar sesión ---

    @Test
    fun cerrarSesionSinCambiosPendientesVaciaLaBaseLocal() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val local = FakeDatosLocales(sinEnviar = 0)
        val viewModel = nuevoViewModel(repositorio, local = local)

        viewModel.cerrarSesion()

        assertNull(viewModel.uiState.value.sesion)
        assertNull(viewModel.uiState.value.cambiosPorPerder)
        assertTrue(repositorio.cerrada)
        assertTrue(local.borrada)
        assertEquals(0, local.filas)
    }

    @Test
    fun conCambiosSinEnviarPideConfirmacionYNoBorraNada() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val local = FakeDatosLocales(sinEnviar = 2)
        val viewModel = nuevoViewModel(repositorio, local = local)

        viewModel.cerrarSesion()

        assertEquals(2, viewModel.uiState.value.cambiosPorPerder)
        assertEquals(admin, viewModel.uiState.value.sesion)
        assertFalse(repositorio.cerrada)
        assertFalse(local.borrada)
    }

    @Test
    fun cancelarElDialogoMantieneLaSesionYLosCambios() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val local = FakeDatosLocales(sinEnviar = 2)
        val viewModel = nuevoViewModel(repositorio, local = local)

        viewModel.cerrarSesion()
        viewModel.cancelarCerrarSesion()

        assertNull(viewModel.uiState.value.cambiosPorPerder)
        assertEquals(admin, viewModel.uiState.value.sesion)
        assertEquals(2, local.sinEnviar)
        assertFalse(local.borrada)
    }

    @Test
    fun cerrarSesionIgualBorraLosCambiosYVuelveAlLogin() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val local = FakeDatosLocales(sinEnviar = 2)
        val viewModel = nuevoViewModel(repositorio, local = local)

        viewModel.cerrarSesion()
        viewModel.confirmarCerrarSesion()

        assertNull(viewModel.uiState.value.sesion)
        assertNull(viewModel.uiState.value.cambiosPorPerder)
        assertTrue(repositorio.cerrada)
        assertTrue(local.borrada)
    }

    @Test
    fun unCierrePor401ConservaLaBaseLocal() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val local = FakeDatosLocales(sinEnviar = 2)
        val avisos = AvisosDeSesion()
        val viewModel = nuevoViewModel(repositorio, avisos, local)

        avisos.notificar(ErrorSesion.SesionExpirada)

        assertNull(viewModel.uiState.value.sesion)
        assertTrue(repositorio.cerrada)
        // El usuario no eligió salir: sus cambios se envían al volver a entrar.
        assertFalse(local.borrada)
        assertEquals(2, local.sinEnviar)
        assertNull(viewModel.uiState.value.cambiosPorPerder)
    }
}
