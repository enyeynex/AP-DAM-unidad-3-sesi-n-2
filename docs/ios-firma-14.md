# Firma de iOS y límite documentado · Guía 14

Equipo 1 · PharmaMobil 1.0.0 (build 1) · bundle id `pe.edu.upeu.pharmamobil`.

## Estado

- El código compartido compila para iOS: `./gradlew :shared:compileKotlinIosSimulatorArm64` pasa en
  la rama `feature/builds-14`.
- **No se entrega `Equipo1_ProyectoFinal_iOS.ipa`.** El equipo no tiene una Mac con Xcode y Apple
  solo permite firmar y exportar desde Xcode en macOS.
- `iosApp/Configuration/Config.xcconfig` ya está listo para la entrega: `MARKETING_VERSION=1.0.0`,
  `CURRENT_PROJECT_VERSION=1` y `PRODUCT_BUNDLE_IDENTIFIER=pe.edu.upeu.pharmamobil`, el mismo
  identificador que en Android.

## Pasos en Xcode (cuando haya una Mac)

1. **Abrir el proyecto:** abre `iosApp/iosApp.xcodeproj` y compila una vez con el simulador para
   que Gradle genere el framework `Shared` (fase *Compile Kotlin Framework*).
2. **Firma:** en el destino *iosApp*, ve a *Signing & Capabilities*, activa *Automatically manage
   signing* y elige el *Team* (cuenta de Apple Developer). Revisa que el *Bundle Identifier* sea
   `pe.edu.upeu.pharmamobil`.
3. **Versión:** en *General*, confirma *Version* 1.0.0 y *Build* 1 (salen de `Config.xcconfig`).
4. **Archivar:** selecciona *Any iOS Device (arm64)* y ve a *Product ▸ Archive*. El archivo aparece
   en el *Organizer*.
5. **Exportar:** en el *Organizer*, *Distribute App*. Usa *Ad Hoc* o *Development* para instalar en
   iPhones registrados, o *App Store Connect* para TestFlight. Renombra el resultado a
   `Equipo1_ProyectoFinal_iOS.ipa`.

## Requisitos que faltan

| Requisito | Por qué | Estado |
| --- | --- | --- |
| Mac con Xcode (versión actual) | Firmar, archivar y exportar solo es posible en macOS | No disponible |
| Apple Developer Program (pago anual) | Con un Apple ID gratuito se puede instalar en un iPhone propio desde Xcode (perfil de 7 días), pero **no se puede exportar un `.ipa`** ni usar TestFlight | No disponible |
| SDK de Firebase para iOS y `GoogleService-Info.plist` | Crashlytics y Messaging están configurados solo en Android. En iOS hace falta registrar la app en el proyecto `pharmamobil-1c729`, agregar el SDK con Swift Package Manager, inicializar `FirebaseApp.configure()` y, para push, una clave APNs | Pendiente (hueco 10 de `ficha-cierre-14.md`) |

## Qué sí se verificó

- Compilación de `shared` para `iosSimulatorArm64` en cada commit de la rama.
- Las pruebas de `commonTest` corren en la JVM (`testAndroidHostTest`). En iOS, la tarea
  `iosSimulatorArm64Test` necesita macOS y Gradle la deshabilita en Linux y Windows.
