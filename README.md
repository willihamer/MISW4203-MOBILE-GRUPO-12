# Vinilos móvil — MISW4203 Grupo 12

App Android nativa en **Kotlin** para consultar el catálogo de álbumes, artistas y coleccionistas de Vinilos. Consume la API REST [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

Documentación del proyecto (backlog, diseño, arquitectura, actas): [wiki](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki).

## Requisitos

| Herramienta | Versión |
| :--- | :--- |
| Android Studio | Reciente, con el JDK que trae incluido (JDK 17 o superior) |
| Android SDK | Plataforma 37 (`compileSdk` y `targetSdk`); la app corre desde **API 24** (Android 7.0) |
| Docker | Para correr el backend localmente |

## 1. Levantar el backend

```bash
git clone https://github.com/TheSoftwareDesignLab/BackVynils.git
cd BackVynils
docker compose up -d --build
```

La API queda en `http://localhost:3000` y carga sola sus datos de prueba (álbumes como *Buscando América* y coleccionistas como *Manolo Bellon*). Para comprobarlo: `curl http://localhost:3000/albums`.

> **Si el contenedor de la base de datos se cae al arrancar:** el `docker-compose.yml` de BackVynils usa la imagen `postgres` sin versión, y desde Postgres 18 cambió la ruta de datos. Se soluciona fijando la versión con un archivo `docker-compose.override.yml` en la carpeta de BackVynils:
>
> ```yaml
> services:
>   db:
>     image: postgres:16
> ```
>
> Luego: `docker compose down`, borrar la carpeta `pgdata` y volver a `docker compose up -d`.

## 2. Configurar la URL del backend

La URL base está en `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
```

- **Emulador:** `10.0.2.2` es el `localhost` del computador; no hay que cambiar nada.
- **Dispositivo físico:** cambiar por la IP del computador en la red local (p. ej. `http://192.168.1.10:3000/`) y agregar esa IP en `app/src/main/res/xml/network_security_config.xml`, que solo permite HTTP hacia los hosts de desarrollo.

## 3. Compilar y ejecutar

Abrir la carpeta del repositorio en Android Studio, esperar la sincronización de Gradle y ejecutar la configuración `app` en un emulador. Desde la terminal:

```bash
./gradlew assembleDebug     # en Windows: gradlew.bat assembleDebug
./gradlew installDebug      # instala en el emulador o dispositivo conectado
```

## 4. Generar el APK

```bash
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## 5. Pruebas

| Tipo | Comando | Requisitos |
| :--- | :--- | :--- |
| Unitarias (JVM) | `./gradlew testDebugUnitTest` | Ninguno |
| E2E con Espresso | `./gradlew connectedDebugAndroidTest` | Emulador encendido y BackVynils corriendo con sus datos de prueba |

Las pruebas E2E están en `app/src/androidTest`, una clase por historia (`HU01AlbumCatalogTest`) más `ProfileSelectionTest`. Esperan a la red con un `IdlingResource` (sin `sleep`) y Gradle apaga las animaciones del sistema durante la ejecución.

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
│   ├── model/                  # Album, Performer, Collector, Profile
│   ├── network/                # VinilosApi, NetworkServiceAdapter, AppError, dto/, mapper/
│   └── repository/             # AlbumRepository, CollectorRepository, ProfileRepository
└── ui/
    ├── common/                 # UiState, ViewModelFactory, avatares, chip de perfil
    ├── profile/                # P01 Bienvenida y P01b ¿Quién eres?
    └── album/list/             # HU01 Catálogo de álbumes
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
