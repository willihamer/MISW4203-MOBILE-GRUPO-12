# Memoria del proyecto — Vinilos móvil (MISW4203, Grupo 12)

Contexto acumulado para retomar el trabajo desde otra máquina o en otra sesión de trabajo. Última actualización: **25 de octubre de 2026** (semana 3, inicio del Sprint 1).

**Orden de lectura para arrancar:** este archivo → wiki [Backlog de producto](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Backlog-de-Producto) → [Diseño por historia](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Diseno-por-Historia) → [Equipo y acuerdos](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Equipo-y-Acuerdos) (GitFlow y DoD) → sección 6 de este archivo para crear el proyecto Android.

---

## 1. Proyecto y equipo

- **Curso:** MISW4203 — Ingeniería de Software para Aplicaciones Móviles (Maestría en Ingeniería de Software, Uniandes).
- **Producto:** app Android nativa en **Kotlin**, arquitectura **MVVM + Repository + Service Adapter + Jetpack**, que consume la API REST [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).
- **Repositorio:** https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12 (dueño: Hamer).
- **Wiki:** https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki (se clona aparte: `MISW4203-MOBILE-GRUPO-12.wiki.git`, rama `master`).
- **Tablero:** https://github.com/users/willihamer/projects/5
- **Figma:** https://www.figma.com/design/catkjQFyU8dTXPvyGDPYbF/Vinilos-APP?node-id=0-1

| Integrante | Correo | GitHub | Responsabilidades conocidas |
| :--- | :--- | :--- | :--- |
| Juan Diego Bolaños Peña | jd.bolanosp1@uniandes.edu.co | @juandiego-uniandes | Wiki, **mockups / prototipo UX/UI (Stitch + Figma)** |
| Diego Alejandro Buitrago Quintero | d.buitragoq@uniandes.edu.co | @diegobuit | Tablero, milestones, labels, issues y backlog con puntos |
| Guerel García Quispe | g.garciaq@uniandes.edu.co | @guerelgq | — |
| William Hamer Patarroyo | w.patarroyo@uniandes.edu.co | @willihamer | Repositorio (main, develop, release) |

**Reunión obligatoria:** lunes, 8:30–9:30 p. m. (GMT-5), Google Meet.

---

## 2. Reglas de trabajo (no romper)

- **Sin atribución a herramientas de IA** en commits, PR ni wiki: nada de `Co-Authored-By: Claude` ni "Generated with…". Es trabajo académico evaluado.
- **Commits:** cuando Juan Diego pide "solo commit", no se hace push; él lo hace.
- **Documentos en markdown**, nunca PDF en la wiki.
- **GitFlow:** `main` (releases, sin commits directos) · `release` · `develop` (integración) · `feature/HUxx-descripcion` desde `develop`. Todo entra a `develop` por PR revisado por un par, con **Squash and merge**.
- **Evidencia de reunión:** basta acta **o** video. No escribir en la wiki que "no hay video"; solo enlazar el acta.
- Idioma de la wiki y de la app: **español**.

---

## 3. Estado de la wiki

Páginas: `Home`, `Equipo-y-Acuerdos`, `Acta-Semana-1`, `Acta-Semana-2`, `Inception`, `Backlog-de-Producto`, `Diseno-por-Historia`, `Tablero-Milestones-y-Labels`, `Prototipo-UX-UI`, `Sprint-1`, `Sprint-2`, `Sprint-3`, más `_Sidebar` y `_Footer`. Imágenes del prototipo en `images/prototipo/` (24 pantallas + 2 referencias en `referencias/`).

- **Semana 1:** reunión el **sábado 10 de octubre de 2026**, evidencia = [Acta semana 1](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Acta-Semana-1) (contrato transcrito a markdown).
- **Semana 2:** reunión el **miércoles 14 de octubre de 2026**, evidencia = [Acta semana 2](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Acta-Semana-2) (la hizo Guerel).
- **Home:** tiene bitácora semanal (semanas 1–8, columna "Evidencia de reunión"), alcance del MVP y enlace a Figma.
- **Backlog:** el MVP es **HU01–HU08** (HU01–HU06 visitante, HU07–HU08 coleccionista). HU09–HU15 quedan fuera del alcance. 65 tareas como issues, cada una con milestone, labels y responsable.
- **Diseño por historia:** pantallas, componentes y recorrido de cada HU; cada HU del backlog enlaza a su sección.
- **Sprint 1/2/3:** cada página tiene secciones vacías de arquitectura, estrategia de pruebas, artefactos, entregables, reuniones, retroalimentación y retrospectiva, que se llenan durante el sprint.

**Responsables** (cada HU, con sus 6 tareas de diseño, red, repositorio, ViewModel, vista y pruebas, la hace una sola persona):

| HU | Responsable | HU | Responsable |
| :--- | :--- | :--- | :--- |
| HU01 Catálogo | Guerel | HU05 Coleccionistas | Guerel |
| HU02 Detalle de álbum | Juan Diego | HU06 Detalle de coleccionista | Juan Diego |
| HU03 Artistas | Hamer | HU07 Crear álbum | Hamer |
| HU04 Detalle de artista | Diego | HU08 Asociar tracks | Diego |

Transversales: **TR-01 a TR-06** (proyecto base, red, navegación, Espresso, README, arquitectura y pruebas en la wiki) y los releases **TR-10, TR-14, TR-18** son de **Juan Diego**; las retrospectivas TR-19, TR-20, TR-22 son de Diego. Hamer quedó con GitFlow, protección de ramas e integración continua (IN-02).
- **Sprints (fechas tentativas, confirmar con Coursera):**

| Sprint | Fechas | HU |
| :--- | :--- | :--- |
| Sprint 1 | 19 oct – 1 nov 2026 | HU01, HU02 + arquitectura base |
| Sprint 2 | 2 – 15 nov 2026 | HU03, HU04, HU05 |
| Sprint 3 | 16 – 29 nov 2026 | HU06, HU07, HU08 + accesibilidad y pruebas |

**Inception cerrado:** prototipo navegable, responsables en las 65 tareas, retroalimentación del avance 1 incorporada (tabla en la página `Inception`), acta de la semana 2 y retrospectiva de Inception, todo en la wiki.

**Acciones acordadas para el Sprint 1** (retrospectiva de Inception): revisar el recorrido del prototipo antes de cada entrega, protección de ramas y solo *Squash and merge* (Hamer), tablero al día, evidencia de cada reunión semanal en la wiki e incorporar la retroalimentación de la semana 2 cuando llegue.

---

## 4. Diseño (Stitch → Figma)

### Decisiones

- **Estilo:** *Vinilos Liquid Dark*, solo modo oscuro, glassmorphism y gradientes. Fondo `#0A0A14`; acentos violeta `#7C3AED`, cian `#22D3EE`, magenta `#EC4899`, ámbar `#F59E0B`; texto `#F5F5FA` / `#C4C4D4`. Tipografía Sora (títulos) + Inter (cuerpo). Frames de Figma de 390 × 844 con scroll vertical.
- **Perfil:** la app abre en *Bienvenida* (visitante o coleccionista). El coleccionista escoge **quién es** (sheet "¿Quién eres?"), porque HU09+ necesitan saber qué coleccionista actúa.
- **Detalle de álbum con aspecto de modal** (bottom sheet). En el prototipo se abre con *Navigate* (Move in hacia arriba) para que haga scroll, y la X / handle regresan con *Back*. Artista y coleccionista son pantallas completas, para no apilar modales.
- **Formularios cortos = bottom sheets.** Solo *Crear álbum* es pantalla completa.
- **Botones de formulario:** 50 % / 50 %, **Cancelar a la izquierda** (vidrio), acción principal a la derecha (gradiente).
- **Solo datos de la API.** Nada de LP, RPM, calificaciones de álbum, país, roles de integrantes, contadores, etc.
- Valores cerrados de la API: géneros `Classical, Salsa, Rock, Folk`; sellos `Sony Music, EMI, Discos Fuentes, Elektra, Fania Records`.
- Campos obligatorios: en *Crear álbum* son los seis (incluida la descripción); en *Mi colección*, precio (COP) y estado Activo / Inactivo.
- El hero de artista sigue el estilo de Queen (P05b). El ícono de "Asignar premio" es el trofeo de la pantalla de Rubén Blades.

### Pantallas (IDs)

P01 Bienvenida · P01b ¿Quién eres? · P02/P02c Catálogo (HU01) · P03/P03c Detalle de álbum, modal (HU02) · P04 Artistas (HU03) · P05 Músico / P05b Banda (HU04, HU10 favoritos) · P06 Coleccionistas (HU05) · P07 Detalle de coleccionista (HU06) · P08 Crear álbum (HU07) · P09 Agregar track (HU08) · P10 Comentar (HU09) · P11 Mi colección (HU11) · P12 Agregar músico (HU12) · P13 Asociar álbum (HU15) · P14 Premios y P15 Crear premio (HU13) · P16 Asignar premio (HU14) · estados de P02 (cargando, vacío, error) · toasts.

Inventario completo, transiciones y galería: [wiki › Prototipo UX/UI](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Prototipo-UX-UI).

### Archivos de esta carpeta

| Archivo | Qué es |
| :--- | :--- |
| [diseno/DESIGN.md](diseno/DESIGN.md) | Design system *Vinilos Liquid Dark* generado por Stitch (tokens de color, tipografía, espaciado). Base para el `Theme` de Compose / `colors.xml`. |
| [diseno/stitch-design-base.md](diseno/stitch-design-base.md) | DESIGN.md de entrada que se le dio a Stitch (reglas de estilo, componentes, roles, datos). |
| [diseno/historial-stitch.md](diseno/historial-stitch.md) | Todos los prompts usados y las revisiones de cada lote, en orden. |

Los `code.html` exportados de Stitch se borraron porque quedaron desactualizados frente a Figma. La referencia visual vigente son las capturas de Figma en la wiki (`images/prototipo/`) y el propio archivo de Figma.

---

## 5. Estado del prototipo en Figma

**Hecho** (detalle en la wiki, sección "Ajustes en Figma"):
- Navegación conectada. Los sheets (P01b, P09–P13, P15, P16) se abren con *Open overlay* y cierran con *Close overlay*; el detalle de álbum se abre con *Navigate* y regresa con *Back*.
- Dos *flow starting points*: **Flujo visitante** en P01 y **Flujo coleccionista** en P02c (Figma no permite dos flujos en el mismo frame).
  - [Flujo visitante](https://www.figma.com/proto/catkjQFyU8dTXPvyGDPYbF/Vinilos-APP?page-id=0%3A1&node-id=1-2&starting-point-node-id=1%3A2)
  - [Flujo coleccionista](https://www.figma.com/proto/catkjQFyU8dTXPvyGDPYbF/Vinilos-APP?page-id=0%3A1&node-id=1-228&starting-point-node-id=1%3A228)
- Pantallas de 390 × 844 con scroll; barra de estado, header, barra inferior y FAB fijos.
- Misma barra inferior, barra de estado y chip de perfil del coleccionista en todas las pantallas.
- Hero de artista a ancho completo (P05 igual a P05b) y tarjetas de álbum de altura uniforme.
- Sheets P12, P13, P15 y P16 con el vidrio oscuro de P03c; "Elektra" en P03 y P03c; P11 sin textos sobrantes; toasts con la barra inferior de la app.
- La posición de los overlays (*Bottom*) y el fondo oscurecido no se pueden configurar por API; como los frames de los sheets ya traen el fondo pintado y miden lo mismo que la pantalla, se dejaron así.
- Nota: la portada de *News of the World* en P05b es la que generó Stitch; no hay portada real en el archivo para reemplazarla.

Las capturas de la wiki (`images/prototipo/`) se exportan directamente de Figma; si se cambia una pantalla, hay que volver a exportarla.

**Editar Figma con un asistente (conector MCP):** el archivo está en el equipo **Uniandes** (plan Educación). Solo se puede escribir con asiento **Full**; con asiento View el conector queda en modo de solo lectura y con 20 llamadas al mes.

---

## 6. Arranque del proyecto Android (Sprint 1)

### Estado del repositorio

- El proyecto Android está en `feature/TR-01-proyecto-android`, creada desde `feature/IN-01-prototipo-ux-ui` (que trae este archivo y `docs/diseno/`). El esqueleto inicial de Android Studio venía con Jetpack Compose y se migró a **XML + Fragments** para seguir la arquitectura de referencia del curso y Espresso.
- La rama incluye la base (TR-01 a TR-05), el design system, la selección de perfil (P01 y P01b) y el catálogo de la HU01. Cómo correr el backend, la app y las pruebas: [README](../README.md).

### Tareas transversales del Sprint 1 (Juan Diego)

| ID | Tarea |
| :--- | :--- |
| TR-01 | Crear el proyecto Android (Kotlin, Gradle, dependencias de Jetpack, red e imágenes) |
| TR-02 | Cliente HTTP y Service Adapter base con manejo de errores |
| TR-03 | Navegación: selección de perfil y bottom navigation (Navigation Component) |
| TR-04 | Configurar Espresso y la estructura de pruebas |
| TR-05 | README con setup, dependencias y generación del APK |
| TR-06 | Documento de arquitectura y estrategia de pruebas en la wiki |

TR-01 a TR-03 bloquean a HU01 (Guerel) y HU02 (Juan Diego), así que van primero.

### Arquitectura

**MVVM + Repository + Service Adapter + Jetpack**, en una sola dirección:

```
Vista (Fragment) → ViewModel → Repository → Service Adapter → API REST
                                   └→ caché local (memoria o Room + DAO)
```

**Ya decidido** (sale del backlog y de la DoD de [Equipo y acuerdos](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Equipo-y-Acuerdos)):
- Kotlin, vistas con **Fragments + layouts XML + `RecyclerView`**, navegación con **Navigation Component** y bottom navigation.
- Un ViewModel por pantalla con estados **cargando / éxito / error** (ej. `AlbumListViewModel`, `AlbumDetailViewModel`).
- Un Repository por entidad, con caché (`AlbumRepository`, `ArtistRepository` que combina músicos y bandas, `CollectorRepository`). Crear un álbum invalida la caché del catálogo.
- La vista no tiene lógica de negocio, el ViewModel no guarda `Context`, y nada de red ni disco en el hilo principal.
- Pruebas E2E con **Espresso** por cada HU, registradas en el inventario de pruebas.
- Perfil visitante / coleccionista al abrir la app; crear álbum y agregar track solo en modo coleccionista.

**Decidido en TR-01** (falta registrarlo en la sección *Arquitectura* de la página `Sprint-1`):

| Tema | Decisión | Nota |
| :--- | :--- | :--- |
| Vistas | XML + Fragments + View Binding | Arquitectura de referencia del curso y Espresso. |
| Cliente HTTP | Retrofit + OkHttp + corrutinas | El Service Adapter traduce las fallas a `AppError`. |
| Imágenes | Glide | Portadas con caché, *placeholder* y error. |
| Caché | En memoria en el repositorio | Room se agrega detrás del repositorio en el análisis de desempeño. |
| Observables | LiveData con `UiState` | Cargando, contenido, vacío y error. |
| SDK mínimo | API 24, con *desugaring* para `java.time` | Decisión del acta de la semana 3. |
| Paquete | `co.edu.uniandes.vinilos` | Estructura: `data/` (model, network, repository), `di/`, `ui/` por funcionalidad. |

### Backend (BackVynils)

- Repositorio: https://github.com/TheSoftwareDesignLab/BackVynils (NestJS + PostgreSQL). No hay un backend desplegado compartido: el entorno de Postman de Heroku que trae el repo está vacío, así que cada quien lo corre localmente.
- Correrlo: `docker compose up` en la carpeta del backend. Levanta la API en el puerto **3000** y PostgreSQL en el 5432 (base `vinyls`, usuario y clave `postgres`).
- Datos de prueba: `sql/data.sql` (álbumes como *Buscando América*, artistas y coleccionistas). Se carga a mano contra la base; no se ejecuta solo.
- Colecciones de Postman en `collections/` para probar cada endpoint.
- **Desde el emulador**, la URL base es `http://10.0.2.2:3000/`. Como es HTTP, la app necesita `usesCleartextTraffic` o un `network_security_config` para permitirlo.

Endpoints del MVP:

| HU | Endpoint |
| :--- | :--- |
| HU01 | `GET /albums` |
| HU02 | `GET /albums/{id}` (incluye tracks, performers y comments) |
| HU03 | `GET /musicians`, `GET /bands` (no hay endpoint único de artistas) |
| HU04 | `GET /musicians/{id}`, `GET /bands/{id}` |
| HU05 | `GET /collectors` |
| HU06 | `GET /collectors/{id}`, `GET /collectors/{id}/albums` |
| HU07 | `POST /albums` |
| HU08 | `POST /albums/{id}/tracks` |

Campos de los modelos (según las entidades del backend):

| Modelo | Campos |
| :--- | :--- |
| `Album` | `id`, `name`, `cover`, `releaseDate`, `description`, `genre`, `recordLabel`, `tracks`, `performers`, `comments` |
| `Track` | `id`, `name`, `duration` (texto `mm:ss`) |
| `Performer` (base) | `id`, `name`, `image`, `description`, `albums` |
| `Musician` | lo de `Performer` + `birthDate` |
| `Band` | lo de `Performer` + `creationDate`, `musicians` |
| `Collector` | `id`, `name`, `telephone`, `email`, `comments`, `favoritePerformers`, `collectorAlbums` |
| `CollectorAlbum` | `id`, `price`, `status` (`Active` / `Inactive`) |
| `Comment` | `id`, `description`, `rating` |

`genre` y `recordLabel` son enums cerrados (ver sección 4).

---

## 7. Cómo retomar en otra máquina

```bash
git clone https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12.git
git clone https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12.wiki.git
git clone https://github.com/TheSoftwareDesignLab/BackVynils.git
```

Herramientas: Android Studio (con un emulador configurado), Docker para el backend y Postman (opcional).

Para una nueva sesión con un asistente: pedirle que lea primero este archivo y la wiki (`Home`, `Inception`, `Backlog-de-Producto`, `Diseno-por-Historia` y `Prototipo-UX-UI`). Recordarle las reglas de la sección 2, sobre todo **sin atribución a IA** en commits, PR y wiki.
