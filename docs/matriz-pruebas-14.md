# Matriz de pruebas en dispositivos reales · Guía 14

Equipo 1 · PharmaMobil 1.0.0. Instala el APK firmado (`entrega/Equipo1_ProyectoFinal_Android.apk`)
con `adb install -r` en cada celular. PharmaSoft corre en la PC, en la misma wifi. El APK debe estar
compilado con `-PurlServicio=http://<IP-PC>:8080/api/v1/`.

## Dispositivos

| | Dispositivo 1 | Dispositivo 2 |
| --- | --- | --- |
| Marca y modelo | (completar) | (completar) |
| Versión de Android | (completar) | (completar) |
| Pantalla (pulgadas / resolución) | (completar) | (completar) |
| Responsable | (completar) | (completar) |
| Fecha de la prueba | (completar) | (completar) |

## Recorrido

Marca ✅ si pasa, ❌ si falla (y anótalo en el registro de defectos) o ➖ si no aplica. Agrega la
captura en `docs/capturas-14/` con el número de paso y de dispositivo, por ejemplo `p4-d1.png`.

| Paso | Qué hacer | Resultado esperado | Dispositivo 1 | Dispositivo 2 |
| --- | --- | --- | --- | --- |
| 1 | Instalar el APK y abrir la app por primera vez | Se ve el login, sin cierres. En Android 13+ no pide permisos al arrancar | (completar) | (completar) |
| 2 | Iniciar sesión con un usuario válido y luego con una clave incorrecta | Con la clave buena entra al inicio. Con la incorrecta muestra «Usuario o contraseña incorrectos» | (completar) | (completar) |
| 3 | Abrir Productos con red | Lista de PharmaSoft y «Actualizado hace…» | (completar) | (completar) |
| 4 | Activar el modo avión y registrar un producto | Queda «Pendiente». **Un solo aviso:** «Sin conexión · 1 cambio por enviar» | (completar) | (completar) |
| 5 | Desactivar el modo avión | Se sincroniza solo; el pendiente toma su id del servidor y desaparece la banda | (completar) | (completar) |
| 6 | Registrar un producto con nombre repetido sin red, y reconectar | Queda como fallido con el motivo (409); aparecen **Reintentar ahora** y **Revisar** | (completar) | (completar) |
| 7 | Inicio ▸ Notificaciones de stock bajo ▸ **Activar** y luego **Probar notificación** | Pide el permiso; al aceptarlo, llega la notificación | (completar) | (completar) |
| 8 | Con un pendiente sin enviar, abrir el menú y elegir **Cerrar sesión** | Aviso «Tienes 1 cambio sin enviar…». **Cancelar** mantiene todo; **Cerrar sesión igual** vuelve al login. Al entrar de nuevo, la lista se carga desde cero | (completar) | (completar) |

## Registro de defectos

| ID | Paso | Dispositivo | Lo observado | Gravedad | Decisión | Commit |
| --- | --- | --- | --- | --- | --- | --- |
| D-01 *(ejemplo)* | 4 | Dispositivo 2 | En pantalla de 5", el aviso «Sin conexión · 1 cambio por enviar» corta el texto en dos líneas y tapa el botón Sincronizar | Baja | Se corrige: el texto se ajusta y el botón baja | `fix(productos): ...` (versionCode 2) |
| D-02 | (completar) | (completar) | (completar) | (completar) | (completar) | (completar) |

Gravedad: **Alta** si bloquea el recorrido o pierde datos, **Media** si hay que rodearlo y **Baja**
si es solo visual. Después del congelamiento (`ficha-cierre-14.md`) solo entran correcciones, y cada
una sube el `versionCode`.

## Ficha de versión

| Campo | Valor |
| --- | --- |
| Aplicación | PharmaMobil |
| applicationId / bundle id | `pe.edu.upeu.pharmamobil` |
| versionName / MARKETING_VERSION | 1.0.0 |
| versionCode / build | 1 |
| Etiqueta Git | `v1.0-final` |
| Rama | `feature/builds-14` → `develop` (PR) |
| Android App Bundle | `Equipo1_ProyectoFinal_Android.aab` |
| APK de prueba | `Equipo1_ProyectoFinal_Android.apk` |
| iOS | Sin `.ipa`: ver `ios-firma-14.md` |
| Firma (alias) | `equipo1` |
| SHA-256 del certificado | (completar con apksigner) |
| SHA-256 del APK | (completar con `Get-FileHash entrega\Equipo1_ProyectoFinal_Android.apk`) |
| URL del servicio usada | (completar, p. ej. `http://10.40.29.85:8080/api/v1/`) |
| minSdk / targetSdk | 24 / 36 |

Para obtener el SHA-256 del certificado:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs entrega\Equipo1_ProyectoFinal_Android.apk
```

Copia la línea `Signer #1 certificate SHA-256 digest`.
