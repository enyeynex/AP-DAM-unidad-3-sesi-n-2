# Reglas de R8 propias de PharmaMobil (Guía 14).
# Solo se agrega lo que hace falta; cada regla dice por qué.
# Se revisaron con androidApp/build/outputs/mapping/release/mapping.txt.

# --- Crashlytics ------------------------------------------------------------
# Sin estos atributos las trazas del release llegan sin número de línea y
# Crashlytics no puede ubicar el fallo, aunque tenga el mapping.txt.
-keepattributes SourceFile,LineNumberTable
# El nombre real del archivo no hace falta (lo resuelve el mapping); así no
# se publican los nombres de los archivos fuente dentro del APK.
-renamesourcefileattribute SourceFile
# Firebase recomienda conservar las excepciones propias (FalloSesionException,
# LoginInvalidoException, ProductoInvalidoException...) para que el título de
# cada incidencia en la consola sea legible.
-keep public class * extends java.lang.Exception

# --- kotlinx.serialization: SIN reglas propias ---------------------------------
# La biblioteca trae sus reglas (META-INF/proguard) y conservan, en cada clase
# @Serializable, el campo Companion, su método serializer() y la clase
# $$serializer. Ktor busca el serializador por reflexión con ese nombre.
# Comprobado en mapping.txt: los 11 DTO de data/remote/dto conservan
# Companion.serializer() (PaginaDto también la variante genérica).

# --- Koin: SIN reglas propias ----------------------------------------------------
# Los módulos se declaran con lambdas (single { }, viewModel { }), sin
# reflexión ni anotaciones: R8 ve todas las llamadas a los constructores.

# --- SQLDelight y Ktor: SIN reglas propias -------------------------------------
# SQLDelight genera código Kotlin normal. El motor de Ktor se pasa explícito
# (crearHttpClient(motor = ...)), sin ServiceLoader ni reflexión.
