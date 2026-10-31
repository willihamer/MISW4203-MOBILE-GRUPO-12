# Vinilos móvil — MISW4203 Grupo 12

App Android nativa en **Kotlin** para consultar el catálogo de álbumes, artistas y coleccionistas de Vinilos. Consume la API REST [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

Documentación del proyecto (backlog, diseño, arquitectura, actas): [wiki](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki).

## Alcance de esta versión

HU01 catálogo de álbumes, HU02 detalle de álbum, HU03 listado de artistas y HU04 detalle de músico o banda. Coleccionistas y las operaciones de escritura pertenecen a historias posteriores.

## Requisitos

- Android Studio y Android SDK Platform 37, con Platform Tools.
- Dispositivo o emulador compatible con el mínimo configurado en `app/build.gradle.kts` (API 24).
- Un Java de arranque compatible con Gradle (se verificó con JDK 17). El wrapper usa Gradle y el criterio de JVM versionados en `gradle/`; la primera ejecución puede descargar la JVM 25 y las dependencias. No hace falta instalar Gradle globalmente.
- Acceso a internet para descargar dependencias y consultar la API.

## Instalación y ejecución

```bash
git clone https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12.git
cd MISW4203-MOBILE-GRUPO-12
```

Seleccionar la rama o etiqueta que se desea evaluar. Abrir esta carpeta en Android Studio, instalar el SDK solicitado y esperar a que termine la sincronización. Elegir un emulador o dispositivo y ejecutar `app`.

La URL predeterminada es el backend compartido por HTTPS:

`https://vinilos-api-926692321600.us-east4.run.app/`

No se requieren Docker ni credenciales de base de datos para ejecutar la app. Verificar disponibilidad con:

```bash
curl https://vinilos-api-926692321600.us-east4.run.app/albums
```

Desde PowerShell, con `JAVA_HOME` apuntando a una instalación Java válida:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

En macOS/Linux usar `./gradlew` en lugar de `.\gradlew.bat`. Si no se encuentra el SDK, configurarlo desde Android Studio o establecer `sdk.dir` en `local.properties` (archivo local, no versionado).

### Backend local opcional

Preparar [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) siguiendo las instrucciones de su repositorio y cargar sus datos semilla. Comprobar que `http://localhost:3000/albums` responda antes de iniciar la app.

Para compilar contra el backend del computador desde el emulador:

```powershell
.\gradlew.bat assembleDebug -PVINILOS_BASE_URL=http://10.0.2.2:3000/
```

También puede definirse `VINILOS_BASE_URL` en el archivo personal `~/.gradle/gradle.properties`, para Android Studio. La URL debe terminar en `/`. En un dispositivo físico, usar la IP accesible del computador y permitir ese host de desarrollo en `app/src/main/res/xml/network_security_config.xml` si usa HTTP. No habilitar HTTP globalmente.

## Pruebas y APK

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

La segunda orden necesita un emulador o dispositivo conectado (`adb devices`). Las pruebas de integración contra el servidor requieren los datos semilla de BackVynils: Buscando América (100), Poeta del pueblo (101), A Night at the Opera (102), A Day at the Races (103), Rubén Blades y Queen. No modifican esos datos. La suite usa `NetworkIdlingResource` para esperar las consultas y Gradle desactiva las animaciones.

- APK instalable de evaluación: `app/build/outputs/apk/debug/app-debug.apk` (firmado para depuración).
- Unitarias: `app/build/reports/tests/testDebugUnitTest/index.html`.
- Espresso: `app/build/outputs/androidTest-results/connected/debug/` (resultados y logcat por escenario en AGP 9.5). El inventario y las evidencias se consultan en la wiki.
- Lint: `app/build/reports/lint-results-debug.html`.
- [Arquitectura y diagramas](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Sprint-1#arquitectura).
- [Inventario, resultados y limitaciones de QA](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Sprint-1#artefactos-de-prueba).

### APK automático del release

Publicar el release en **Releases → Draft a new release → Publish release**. La versión etiquetada debe incluir `.github/workflows/release-apk.yml`. El flujo ejecuta `assembleDebug`, `testDebugUnitTest` y `lintDebug`; si pasan, adjunta `vinilos-debug.apk` y su archivo SHA-256 al release. El progreso y los errores se consultan en **Actions → APK del release**.

El APK está firmado para depuración. Guardar un borrador o crear solo la etiqueta no inicia el flujo. Las pruebas Espresso se ejecutan por separado con un dispositivo o emulador.

## Arquitectura

**MVVM + Repository + Service Adapter** sobre Android Jetpack, con una sola Activity y Navigation Component.

```
Vista (Fragment + XML) → ViewModel (LiveData<UiState>) → Repository (caché) → NetworkServiceAdapter (Retrofit) → BackVynils
```

```
app/src/main/java/co/edu/uniandes/vinilos/
├── VinilosApplication.kt, MainActivity.kt
├── di/AppContainer.kt          # inyección de dependencias manual
├── data/
│   ├── model/                  # Album, Track, Performer, Artist, Collector, Profile
│   ├── network/                # VinilosApi, NetworkServiceAdapter, AppError, dto/, mapper/
│   └── repository/             # AlbumRepository, ArtistRepository, CollectorRepository, ProfileRepository
└── ui/
    ├── common/                 # UiState, ViewModelFactory, avatares, chip de perfil
    ├── profile/                # P01 Bienvenida y P01b ¿Quién eres?
    ├── album/list/             # HU01 Catálogo de álbumes
    ├── album/detail/           # HU02 Detalle de álbum
    ├── artist/list/            # HU03 Listado de artistas
    └── artist/detail/          # HU04 Detalle de artista
```

| Librería | Uso |
| :--- | :--- |
| Material Components, ConstraintLayout, RecyclerView | Vistas en XML |
| Navigation Component | Navegación entre Fragments |
| Lifecycle (ViewModel, LiveData), corrutinas | Estado y concurrencia |
| Retrofit + OkHttp + Gson | Cliente HTTP |
| Glide | Carga de portadas con caché |
| `desugar_jdk_libs` | `java.time` en API 24 y 25 |
| JUnit, Espresso | Pruebas |
| MockWebServer | Respuestas HTTP controladas en pruebas instrumentadas |

## Flujo de entrega

Trabajar en `feature/*`, abrir PR hacia `develop`, obtener revisión de un compañero y usar squash and merge. Preparar la versión en `release`, ejecutar las comprobaciones acordadas e integrar a `main`. Crear la etiqueta y publicar el GitHub Release sobre el commit evaluado; GitHub Actions genera y adjunta el APK automáticamente. Una rama llamada `release` por sí sola no publica una versión.

## Design system

Los tokens del design system *Vinilos Liquid Dark* (`docs/diseno/DESIGN.md`) están en `app/src/main/res/values/`:

| Archivo | Contenido |
| :--- | :--- |
| `colors.xml` | Lienzo, acentos (violeta, cian, magenta, ámbar), texto y vidrio |
| `type.xml` | Escala tipográfica: Sora para títulos, Inter para cuerpo (fuentes en `res/font`) |
| `dimens.xml` | Espaciado en módulos de 8 dp, radios y tamaños de componentes |
| `styles.xml` | Botón primario y secundario, botón de ícono, chip, campo de texto, insignia y pestaña |
| `themes.xml` | Tema `Theme.Vinilos` (solo modo oscuro) y tema de los bottom sheets |

Los fondos de vidrio, gradientes e ilustraciones están en `res/drawable` (`bg_*` e `ic_*`). Las fuentes Sora e Inter tienen licencia SIL Open Font License.
