package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.sesion.AvisoSesion
import pe.edu.upeu.pharmamobil.domain.sesion.AvisosDeSesion
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ContarCambiosSinEnviarUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.LoginInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.SesionGuardadaUseCase

/**
 * Dueño de la sesion en la interfaz. Decide si se ve el login o la app, y es
 * el unico que reacciona a un 401: cierra la sesion y vuelve al login con un
 * aviso. Un 403 no llega aqui; lo muestra la pantalla que lo provoco.
 */
class SesionViewModel(
    private val iniciarSesion: IniciarSesionUseCase,
    private val sesionGuardada: SesionGuardadaUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
    private val avisos: AvisosDeSesion,
    private val contarCambiosSinEnviar: ContarCambiosSinEnviarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val guardada = sesionGuardada()
            _uiState.update {
                it.copy(
                    sesion = guardada,
                    verificando = false,
                    idSesion = if (guardada != null) it.idSesion + 1 else it.idSesion
                )
            }
        }

        viewModelScope.launch {
            avisos.sesionTerminada.collect { aviso ->
                if (_uiState.value.sesion != null) {
                    // Un 401 conserva la base local: ver CerrarSesionUseCase.
                    terminarSesion(aviso = textoDelAviso(aviso), conservarDatosLocales = true)
                }
            }
        }
    }

    fun onUsuarioChange(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, error = null) }
    }

    fun onClaveChange(clave: String) {
        _uiState.update { it.copy(clave = clave, error = null) }
    }

    fun iniciarSesion() {

        if (_uiState.value.cargando) return

        viewModelScope.launch {

            _uiState.update { it.copy(cargando = true, error = null, aviso = null) }

            val estado = _uiState.value

            iniciarSesion(estado.usuario, estado.clave).fold(
                onSuccess = { sesion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            clave = "",
                            sesion = sesion,
                            idSesion = it.idSesion + 1
                        )
                    }
                },
                onFailure = { fallo ->
                    val mensaje = when (fallo) {
                        is LoginInvalidoException -> fallo.errores.usuario ?: fallo.errores.clave
                        else -> fallo.message
                    }
                    _uiState.update {
                        it.copy(cargando = false, error = mensaje ?: "No se pudo iniciar sesión")
                    }
                }
            )
        }
    }

    /**
     * Accion «Cerrar sesion»: borra el token, vacia la base local y devuelve
     * al login. Guía 14 (hueco 8): si hay cambios sin enviar, antes pide
     * confirmacion (cambiosPorPerder) porque se perderian.
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            val sinEnviar = contarCambiosSinEnviar()
            if (sinEnviar > 0) {
                _uiState.update { it.copy(cambiosPorPerder = sinEnviar) }
            } else {
                terminarSesion(aviso = null, conservarDatosLocales = false)
            }
        }
    }

    /** «Cerrar sesión igual» en el dialogo: el usuario acepta perder los cambios. */
    fun confirmarCerrarSesion() {
        viewModelScope.launch { terminarSesion(aviso = null, conservarDatosLocales = false) }
    }

    /** «Cancelar» en el dialogo: la sesion sigue y los cambios esperan a la red. */
    fun cancelarCerrarSesion() {
        _uiState.update { it.copy(cambiosPorPerder = null) }
    }

    /**
     * Cerrar sesion deja la interfaz limpia: ni el usuario anterior ni datos
     * de la sesion previa quedan en el login. Solo se conserva el contador
     * idSesion, que no se muestra.
     */
    private suspend fun terminarSesion(aviso: String?, conservarDatosLocales: Boolean) {
        cerrarSesion(conservarDatosLocales)
        _uiState.update {
            SesionUiState(
                verificando = false,
                aviso = aviso,
                idSesion = it.idSesion
            )
        }
    }

    /**
     * Un 401 a mitad de la sesion no es un error de credenciales: el usuario
     * no escribio nada. Se explica que la sesion termino y se muestra el
     * motivo literal que dio el servidor (token vencido, token alterado,
     * usuario deshabilitado...). Solo un 401 llega por AvisosDeSesion.
     */
    private fun textoDelAviso(aviso: AvisoSesion): String = when {
        aviso.detalle != null ->
            "Tu sesión terminó (HTTP 401). PharmaSoft respondió: «${aviso.detalle}»"
        aviso.error == ErrorSesion.SesionExpirada ->
            "Tu sesión expiró. Vuelve a iniciar sesión"
        else ->
            "Tu sesión ya no es válida. Vuelve a iniciar sesión"
    }
}
