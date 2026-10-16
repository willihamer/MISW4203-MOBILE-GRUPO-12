# Memoria del proyecto — Vinilos móvil (MISW4203, Grupo 12)

Contexto acumulado para retomar el trabajo desde otra máquina o en otra sesión de trabajo. Última actualización: **15 de octubre de 2026** (semana 2, cierre de Inception).

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

Páginas: `Home`, `Equipo-y-Acuerdos`, `Acta-Semana-1`, `Inception`, `Backlog-de-Producto`, `Tablero-Milestones-y-Labels`, `Prototipo-UX-UI`, `Sprint-1`, `Sprint-2`, `Sprint-3`, más `_Sidebar` y `_Footer`. Imágenes del prototipo en `images/prototipo/` (24 pantallas + 2 referencias en `referencias/`).

- **Semana 1:** reunión el **sábado 10 de octubre de 2026**, evidencia = [Acta semana 1](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Acta-Semana-1) (contrato transcrito a markdown).
- **Home:** tiene bitácora semanal (semanas 1–8, columna "Evidencia de reunión"), alcance del MVP y enlace a Figma.
- **Backlog:** el MVP es **HU01–HU08** (HU01–HU06 visitante, HU07–HU08 coleccionista). HU09–HU15 quedan fuera del alcance.
- **Sprints (fechas tentativas, confirmar con Coursera):**

| Sprint | Fechas | HU |
| :--- | :--- | :--- |
| Sprint 1 | 19 oct – 1 nov 2026 | HU01, HU02 + arquitectura base |
| Sprint 2 | 2 – 15 nov 2026 | HU03, HU04, HU05 |
| Sprint 3 | 16 – 29 nov 2026 | HU06, HU07, HU08 + accesibilidad y pruebas |

**Pendientes de Inception (semana 2):**
- [x] Prototipo navegable en Figma (Juan Diego): flujos conectados y ajustes terminados, ver sección 5.
- [x] Publicar el enlace del prototipo (`/proto/`) en `Prototipo-UX-UI` y `Home`.
- [ ] Responsables en cada issue (todos).
- [ ] Incorporar la retroalimentación de Uniandes del avance 1.
- [ ] Evidencia de la reunión de la semana 2 (acta o video) en la bitácora.
- [ ] Retrospectiva de Inception.

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

---

## 6. Cómo retomar en otra máquina

```bash
git clone https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12.git
git clone https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12.wiki.git
```

Para una nueva sesión con un asistente: pedirle que lea primero este archivo y la wiki (`Home`, `Inception` y `Prototipo-UX-UI`).
