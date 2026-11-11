# Ficha de cierre del integrador · Guía 14

Equipo 1 · PharmaMobil · rama `feature/builds-14` (desde `feature/integrador-13`).
Esta ficha cierra la tabla de `docs/huecos-integrador-13.md`. Además agrega los huecos 9 y 10, que
aparecieron al repetir el recorrido completo en la sesión 14.

## Estado final de los huecos

| N.º | Hueco | Estado | Fecha | Evidencia |
| --- | --- | --- | --- | --- |
| 1 | Primera apertura sin sesión | Descartado: ya era correcto | 04/11 | Recorrido de la Guía 13 |
| 2 | Login con credenciales incorrectas o sin red | Descartado: ya era correcto | 04/11 | `SesionViewModelTest` |
| 3 | Listado con la base local vacía | Descartado: ya era correcto | 04/11 | Estado vacío «Aún no hay datos guardados» |
| 4 | Creación en modo avión sin reintento ni motivo | **Cerrado** | 04/11 | `62c6709`, `2993321` |
| 5 | Sin sincronización automática ni indicador de conexión | **Cerrado** | 04/11 | `57cabca` |
| 6 | Error de validación o 5xx sobre un pendiente | **Cerrado** | 04/11 | `2993321`, `57cabca` |
| 7 | Token vencido a mitad de un formulario | **Limitación declarada** | 11/11 | Ver abajo |
| 8 | Cerrar sesión deja la base local en el equipo | **Cerrado** | 11/11 | `fix(sesion): limpiar la base local al cerrar sesion` |
| 9 | Dos avisos de «sin conexión» en Productos | **Cerrado** | 11/11 | `fix(productos): un solo aviso de estado sin conexion` |
| 10 | Notificaciones push sin servidor que las envíe | **Limitación declarada** | 11/11 | Ver abajo |

### Hueco 8: cierre de sesión

- El cierre manual borra el token y vacía las tablas `producto` y `sincronizacion` mediante
  `CerrarSesionUseCase` y `DatosLocalesRepository`.
- Si quedan cambios sin enviar (en cola, agotados o rechazados), primero aparece este aviso:
  «Tienes N cambios sin enviar. Si cierras sesión se perderán.», con **Cancelar** y
  **Cerrar sesión igual**.
- Un cierre por **401** conserva la base local. Esta vez el usuario no eligió salir: el token
  venció o lo revocaron. Así, al volver a entrar, la sincronización envía lo que hizo sin red.
  Si entra otro usuario, el inventario sincronizado se reemplaza en la primera sincronización. Los
  pendientes se envían con el token de quien inició sesión.
- Pruebas: `LimpiezaAlCerrarSesionTest` (SQLite en memoria) y cinco casos nuevos en
  `SesionViewModelTest`.

### Hueco 9: un solo aviso sin conexión

- Sin red, la pantalla mostraba la banda de la Guía 13 («Sin conexión · N cambios por enviar») y
  además el aviso antiguo («Sin conexión: mostrando datos guardados» con «N productos pendientes»).
- Ahora solo queda la banda. El aviso propio se reserva para lo que la banda no explica: un rechazo
  de PharmaSoft o un error del servidor (por ejemplo, HTTP 500).

### Limitaciones declaradas

| N.º | Motivo por el que no entra | Qué haría falta |
| --- | --- | --- |
| 7 | Guardar el borrador al reautenticar exige persistir el formulario por pantalla y restaurarlo después del login. Eso toca cada ViewModel con formulario y no se puede probar a fondo antes de la entrega. Lo ya guardado como pendiente **no** se pierde. | Persistir `FormularioProducto` en `SavedStateHandle` o en una tabla `borrador`, sin borrarlo en el cierre por 401, y restaurarlo al volver a la pantalla. |
| 10 | La app obtiene el token de Firebase Messaging (`TokenPushFirebase`), pero PharmaSoft no tiene un endpoint para registrarlo ni un emisor de mensajes. Las notificaciones de stock bajo son locales (`Notificador`). En iOS no hay SDK de Firebase configurado. | Un endpoint `POST /dispositivos` en PharmaSoft que guarde el token por usuario, un envío con la API HTTP v1 de FCM y `GoogleService-Info.plist` + APNs en iOS. |

## Alcance congelado

Desde este commit, la versión **1.0.0 (versionCode 1)** se congela con lo siguiente:

- Login con JWT guardado en el almacén cifrado, cierre de sesión manual y por 401.
- Inventario offline-first (SQLDelight), registro sin red, sincronización con reintentos, estado
  visible y sincronización automática al reconectar.
- Clientes según el permiso `CLIENTES_LEER`.
- Permiso de notificaciones con tres estados y aviso local de stock bajo.
- Crashlytics detrás de `Telemetria`.

Después del congelamiento solo entran **correcciones de defectos** que aparezcan en la matriz de
pruebas en dispositivos reales (`docs/matriz-pruebas-14.md`). Cada una va en su propio commit
`fix(...)`, sube el `versionCode` y se anota en el registro de defectos. Las funciones nuevas no
entran.

## Trabajo futuro

- Hueco 7: borrador del formulario al reautenticar.
- Hueco 10: push del servidor (registro del token y envío por FCM v1).
- Pedidos: el módulo sigue como «en construcción».
- Clave de idempotencia en PharmaSoft para que reintentar un POST nunca duplique (ver README,
  «Idempotencia»).
- Build de iOS firmado (`docs/ios-firma-14.md`).
