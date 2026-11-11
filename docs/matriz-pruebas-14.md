# Matriz de pruebas en dispositivos reales · Guía 14

Equipo 1 · PharmaMobil 1.0.0. Instala el APK firmado (`entrega/Equipo1_ProyectoFinal_Android.apk`)
con `adb install -r` en cada celular. PharmaSoft corre en la PC. El APK se compila con
`-PurlServicio=http://<IP-PC>:8080/api/v1/` (misma wifi) o con `http://localhost:8080/api/v1/`
si se usa `adb reverse` (red que aísla equipos).

## Dispositivos

| | Dispositivo 1 | Dispositivo 2 |
| --- | --- | --- |
| Marca y modelo | Samsung Galaxy S24 Ultra (SM-S928B) | Samsung Galaxy S25 Ultra (SM-S938B) |
| Versión de Android / One UI | Android 16 / One UI 8.5 | Android 17 / One UI 9.0 |
| Responsable | Adiel Lujan (Equipo 1) | Adiel Lujan (Equipo 1) |
| Fecha de la prueba | 11/11/2026, 15:28–15:36 | 11/11/2026, 15:54–15:58 |
| Conexión con PharmaSoft | Punto de acceso + depuración inalámbrica + `adb reverse` | Punto de acceso + depuración inalámbrica + `adb reverse` |

**Entorno.** La wifi de la universidad aísla a los equipos entre sí: desde el celular,
`http://10.40.29.85:8080` da `ERR_TOO_MANY_RETRIES` aunque en la PC `localhost:8080` responde 401
(ver O-01). Por eso el APK de prueba se compiló con
`-PurlServicio=http://localhost:8080/api/v1/` y cada celular llegó a la PC por
`adb reverse tcp:8080 tcp:8080` sobre depuración inalámbrica, con la PC y el celular conectados al
punto de acceso del otro celular. El navegador del celular confirmó el túnel (401 «Debe iniciar
sesión»). Usuario de prueba: `admin`.

## Recorrido

✅ pasa · ❌ falla (va al registro de defectos) · ➖ no ejecutado. Entre paréntesis, el número de
captura de cada dispositivo en la documentación (D1 = S24, D2 = S25).

| Paso | Qué hacer | Resultado esperado | Dispositivo 1 | Dispositivo 2 |
| --- | --- | --- | --- | --- |
| 1 | Instalar el APK y abrir la app por primera vez | Se ve el login, sin cierres. No pide permisos al arrancar | ✅ (`install` → Success; cap. 1) | ✅ (Success; cap. 1) |
| 2 | Iniciar sesión con un usuario válido y luego con una clave incorrecta | Con la clave buena entra al inicio. Con la incorrecta muestra «Usuario o contraseña incorrectos» | ✅ (cap. 2 clave incorrecta, cap. 3 inicio) | ✅ clave válida (cap. 1–2); la clave incorrecta solo se probó en D1 |
| 3 | Abrir Productos con red y registrar un producto | Lista de PharmaSoft, «Actualizado hace…» y registro confirmado | ✅ (cap. 4–6: 8 → 10 productos) | ✅ (cap. 3–4: «Adiel s25», 11 → 12) |
| 4 | Activar el modo avión y registrar un producto | Queda «Pendiente». **Un solo aviso:** «1 cambio por enviar» | ✅ (cap. 7–8) | ✅ (cap. 5–6) |
| 5 | Desactivar el modo avión | Se sincroniza; el pendiente toma su id del servidor y desaparece la banda | ✅ con D-01 (cap. 9) | ✅ con D-01 (cap. 7: 13 productos) |
| 6 | Registrar un producto con nombre repetido sin red, y reconectar | Queda como fallido con el motivo (409); aparecen **Reintentar ahora** y **Revisar** | ➖ no ejecutado | ➖ no ejecutado |
| 7 | Inicio ▸ Notificaciones de stock bajo ▸ **Activar** | Pide el permiso; al aceptarlo queda «Activadas» y se obtiene el token de Firebase Messaging | ✅ (cap. 10–11) | ✅ (cap. 8–9) |
| 8 | Con un pendiente sin enviar, abrir el menú y elegir **Cerrar sesión** | Aviso «Tienes 1 cambio sin enviar…» con **Cancelar** / **Cerrar sesión igual** | ✅ (cap. 12) | ✅ (cap. 10) |

En el paso 5, con `adb reverse` el modo avión también corta el túnel: al volver hubo que repetir
`adb connect` y `adb reverse` y pulsar Sincronizar (O-03). No es un defecto de la app.

## Registro de defectos

| ID | Paso | Dispositivo | Pasos para reproducir | Esperado | Obtenido | Gravedad | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| D-01 | 5 | D1 y D2 | Modo avión → registrar producto → quitar modo avión → sincronizar | Al confirmarse el envío, el mensaje «guardado sin conexión» se oculta o cambia a «enviado» | Sigue el mensaje verde «Producto … guardado sin conexión: se enviará al sincronizar» aunque el producto ya no está pendiente | Baja (visual) | Abierto: se corrige en una versión siguiente (`versionCode` 2) |

## Observaciones del entorno (no son defectos de la app)

| ID | Observación | Cómo se resolvió |
| --- | --- | --- |
| O-01 | La wifi de la universidad aísla clientes: el celular no alcanza `10.40.29.85:8080` (`ERR_TOO_MANY_RETRIES`) | Punto de acceso de un celular + depuración inalámbrica + `adb reverse`; APK con `urlServicio=http://localhost:8080/api/v1/` |
| O-02 | En D1 había una PharmaMobil anterior firmada con otra clave; mostraba «No se pudo conectar con el servidor. Revisa tu conexión», texto que no existe en 1.0.0 | `adb uninstall pe.edu.upeu.pharmamobil` y luego `adb install` del APK 1.0.0 → Success |
| O-03 | Con depuración inalámbrica, el modo avión corta el túnel `reverse` | Al volver: `adb connect` + `adb reverse` + Sincronizar. Con un servidor alcanzable por la red, la sincronización es automática |
| O-04 | R8 avisa «error when parsing kotlin metadata» (Kotlin 2.4.10 es más nuevo que el R8 del AGP) | Solo advertencia: el build termina y la app funciona en los dos celulares |

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
| SHA-256 del certificado | `2E:3B:05:82:AE:25:65:32:80:80:3B:F1:D6:C3:5F:A9:A6:EB:6F:C4:06:E3:A1:45:E9:B2:61:81:FE:3C:91:67` |
| Titular del certificado | `CN=Adiel Lujan, OU=Ingenieria de sistemas, O=UPeU, L=Lima, ST=Lima, C=PE` |
| Almacén de claves | `equipo1-entrega.jks`, fuera del repositorio (copia de respaldo aparte) |
| Tamaños | AAB 5 926 270 B · APK 2 527 180 B |
| Commit compilado | `f9bdf30` (merge del PR #2 en `develop`), etiqueta `v1.0-final` |
| URL del servicio usada | `http://10.40.29.85:8080/api/v1/` en el build de entrega; `http://localhost:8080/api/v1/` + `adb reverse` en la prueba (O-01) |
| minSdk / targetSdk | 24 / 36 |

Para obtener el SHA-256 del certificado:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs entrega\Equipo1_ProyectoFinal_Android.apk
```

Copia la línea `Signer #1 certificate SHA-256 digest`.
