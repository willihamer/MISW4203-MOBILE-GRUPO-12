# Historial de prompts y revisiones en Stitch

Registro de la generación del mockup con Google Stitch (oct. 2026): prompts usados, revisiones de cada lote y decisiones. Las pantallas finales están en la [wiki](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Prototipo-UX-UI). Los textos están escritos como respuestas de la sesión de trabajo; los prompts van en inglés porque Stitch los interpreta mejor.

---

## 1. Super prompt por bloques (versión inicial)

Armé el prompt en bloques porque Stitch responde mejor así: primero se fija el estilo global y luego se generan las pantallas por lotes. Si mandas todo en un solo prompt, Stitch empieza a perder detalles después de 4 o 5 pantallas. El estilo y las instrucciones van en inglés, que Stitch interpreta mejor; los textos visibles de la app van en español.

**Antes de usarlo, tres cosas:**
- **Son 15 HU, no 16.** El curso lista HU01–HU15 y no encontré una HU16. Si existe, pásamela y la agrego. El prompt cubre las 15, pero recuerda que solo HU01–HU08 son obligatorias.
- **Desde HU09 se necesita saber qué coleccionista eres.** Comentar, favoritos y "mi colección" dependen de eso. Por eso, al elegir el perfil coleccionista, el usuario escoge quién es de una lista.
- **Solo el detalle del álbum es modal.** Artista y coleccionista quedan como pantallas completas. Si todo fuera modal, al abrir un álbum desde el detalle de un coleccionista quedaría un modal encima de otro, y eso es difícil de navegar y de prototipar.

---

##### Bloque 1 — Estilo global (pegar primero)

```
Design a native Android mobile app called "Vinilos": a music catalog of vinyl albums, artists (musicians and bands) and record collectors. Frame size 360x800 dp, Material Design 3 structure, but with a premium dark "liquid glass" aesthetic. All UI text must be in SPANISH.

VISUAL STYLE
- Background: deep midnight base (#0A0A14) with large, soft, blurred gradient orbs (violet #7C3AED, electric cyan #22D3EE, magenta #EC4899, warm amber #F59E0B used sparingly as a "vinyl warmth" accent). Orbs sit behind content and shift position between screens.
- Glassmorphism everywhere: cards, sheets, nav bar and inputs use frosted translucent surfaces (white at 8–12% opacity, backdrop blur ~24px, 1px border white at 15–20%, subtle top-left highlight and soft inner glow). Corners: 24dp for cards, 32dp for sheets, full pill for buttons and chips.
- Primary buttons: pill shape, violet→cyan gradient fill with soft outer glow. Secondary buttons: glass pill with gradient border. Icon buttons: circular glass with glow.
- Typography: modern geometric sans (Sora or Space Grotesk for headings, Inter for body). Large bold headings, generous spacing. Text sizes in sp.
- Vinyl motif: album covers show a black vinyl record with visible grooves peeking out from behind the cover; subtle groove-ring patterns as decorative background elements.
- Iconography: thin rounded line icons (Material Symbols Rounded).
- Accessibility: text contrast at least 4.5:1 against the glass (use near-white #F5F5FA text, never gray on glass); touch targets at least 48x48 dp; every icon button has a visible or implied label.
- Bottom navigation: floating glass pill bar with 3 tabs — "Álbumes" (album icon), "Artistas" (mic icon), "Coleccionistas" (people icon). Active tab has a gradient glow indicator.
- Top app bar: transparent with screen title, and on the right a circular avatar chip showing the current profile ("Visitante" or the collector's initials) that opens "Cambiar perfil".

DATA (use realistic content from the Latin/rock catalog): albums like "Buscando América" (Rubén Blades, Salsa, Elektra, 1984), "A Night at the Opera" (Queen, Rock, EMI, 1975), "Poeta del pueblo" (Rubén Blades, Salsa, Fania Records), "Mañana" (Folk). Genres are only: Classical, Salsa, Rock, Folk. Record labels are only: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records.

Every list and detail screen needs four states designed as variants: loading (glass skeleton shimmer), content, empty (illustration + message) and error (message + "Reintentar" button).

Two user roles: VISITANTE (read-only) and COLECCIONISTA (can create and edit). Write actions appear only in collector mode.
```

##### Bloque 2 — Entrada y álbumes (HU01, HU02, HU07, HU08, HU09, HU11)

```
Using the established Vinilos style, generate these screens:

1. "Bienvenida / Selección de perfil": hero with a large glowing vinyl record and gradient orbs, title "Vinilos", subtitle "Descubre, colecciona y comparte música". Two large glass cards: "Explorar como visitante" (eye icon) and "Entrar como coleccionista" (vinyl icon). Choosing collector opens a glass bottom sheet "¿Quién eres?" listing collectors (avatar initials, name, email) to pick from.

2. "Catálogo de álbumes" (HU01): title "Álbumes", glass search field "Buscar álbum o artista", horizontal genre chips (Todos, Salsa, Rock, Folk, Classical). 2-column grid of glass album cards: cover with vinyl peeking out, album name, artist, year. In collector mode: floating circular gradient FAB "+" labeled "Crear álbum".

3. "Detalle de álbum como MODAL" (HU02): tapping an album opens a large glass bottom sheet over the blurred catalog (not a new page). Drag handle at top, close "X". Content: big cover with vinyl spinning out to the right, album name, artist chips (tappable), info row (fecha de lanzamiento, género chip, sello discográfico), description, section "Tracks" with numbered rows (name + duration mm:ss), section "Comentarios" with star rating and text. Collector mode adds: gradient button "Agregar track", glass button "Agregar a mi colección", and "Comentar". Empty tracks state: "Este álbum aún no tiene tracks".

4. "Crear álbum" (HU07): full-screen glass form, title "Nuevo álbum", live cover preview card at top that updates from the URL. Fields: Nombre, URL de la portada, Fecha de lanzamiento (date picker), Descripción (multiline), Género (dropdown: Classical, Salsa, Rock, Folk), Sello discográfico (dropdown: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records). Show one field in error state ("Este campo es obligatorio"). Buttons "Cancelar" (glass) and "Guardar álbum" (gradient). Also a success toast "Álbum creado".

5. "Agregar track" (HU08): glass bottom sheet over the album modal. Fields: Nombre del track, Duración (mm:ss, with hint "Ej. 04:35"). Buttons "Cancelar" / "Agregar". Success toast "Track agregado".

6. "Comentar álbum" (HU09): glass bottom sheet with 5-star rating selector, multiline comment field, button "Publicar comentario".

7. "Agregar a mi colección" (HU11): glass bottom sheet with Precio field and Estado selector (Activo / Inactivo), button "Agregar a mi colección".
```

##### Bloque 3 — Artistas y premios (HU03, HU04, HU10, HU12, HU13, HU14, HU15)

```
Using the established Vinilos style, generate these screens:

1. "Listado de artistas" (HU03): title "Artistas", glass segmented tabs "Todos / Músicos / Bandas", search field. List of glass rows: circular artist photo with gradient ring, name, type chip ("Músico" or "Banda"), chevron. In collector mode a top-right icon button "Premios" (trophy).

2. "Detalle de artista" (HU04): full screen. Large hero photo fading into the gradient background, name, type chip, birth date (musician: "Nacimiento") or creation date (band: "Creación"), description. Section "Álbumes" as a horizontal carousel of album cards (tapping one opens the album MODAL). For bands, section "Integrantes" with musician avatars. Section "Premios" with glass trophy cards (prize name, organization, date). Collector mode adds: heart icon button "Agregar a favoritos" (HU10, show filled/active variant), and buttons "Agregar álbum" (HU15), "Asignar premio" (HU14) and, only for bands, "Agregar músico" (HU12).

3. "Agregar músico a la banda" (HU12): glass bottom sheet with searchable list of musicians with checkboxes, button "Agregar a la banda".

4. "Agregar álbum al artista" (HU15): glass bottom sheet with searchable list of albums (cover thumbnail + name), button "Asociar álbum".

5. "Premios" (HU13): list of glass trophy cards (name, organization, description) with FAB "+ Crear premio". Create form as glass sheet: Nombre, Organización, Descripción; buttons "Cancelar" / "Crear premio".

6. "Asignar premio al artista" (HU14): glass bottom sheet with dropdown "Premio" and date picker "Fecha de premiación", button "Asignar premio".
```

##### Bloque 4 — Coleccionistas (HU05, HU06)

```
Using the established Vinilos style, generate these screens:

1. "Listado de coleccionistas" (HU05): title "Coleccionistas", search field, list of glass cards: gradient avatar with initials, name, small stats ("12 álbumes · 4 artistas favoritos"), chevron.

2. "Detalle de coleccionista" (HU06): full screen. Glass profile header with large gradient avatar, name, contact rows with icons (teléfono, correo). Section "Artistas favoritos" as horizontal avatar carousel (tap opens artist detail). Section "Su colección" as album grid with price and status chip (Activo/Inactivo); tapping an album opens the album MODAL. Section "Comentarios" listing the collector's album reviews with stars.
```

---

##### Después, en Figma

1. Exporta desde Stitch con "Copy to Figma" o el plugin de Stitch, y renombra los frames con los IDs de la wiki (P01, P02…).
2. Arma el modal del álbum como **overlay** ("Open overlay", posición *Bottom*, animación *Move in* desde abajo, cierre con *Close overlay*). Así sirve igual desde el catálogo, el artista o el coleccionista.
3. Haz lo mismo con los bottom sheets de formularios (track, comentar, colección, premio).
4. Crea dos *flow starting points*, "Flujo visitante" y "Flujo coleccionista", para que en la demo se vea que el visitante no tiene acciones de escritura.

Como el detalle del álbum pasó de página a modal y entran pantallas nuevas (comentarios, favoritos, premios), la página [Prototipo-UX-UI](https://github.com/willihamer/MISW4203-MOBILE-GRUPO-12/wiki/Prototipo-UX-UI) de la wiki ya no coincide con este diseño. ¿Quieres que la actualice?

---

## 2. Uso de DESIGN.md en Stitch

Te dejé el archivo listo en [stitch-design-base.md](stitch-design-base.md). Lo puse fuera del repo y de la wiki porque es material de trabajo para Stitch, no un entregable. Incluye todo el Bloque 1 (colores, tipografía, glass, componentes, navegación con el modal del álbum, roles, estados, datos de la API y accesibilidad) en formato de tokens.

**Cómo llenar la pantalla de Stitch:**
1. **Upload a DESIGN.md file:** sube [stitch-design-base.md](stitch-design-base.md), o pega su contenido en el recuadro de arriba.
2. **Upload code, images, fonts and logos:** sube las 5 imágenes de referencia que me mostraste. Stitch las usa para captar el estilo visual mejor que cualquier descripción.
3. **GitHub repository:** déjalo vacío. El repo aún no tiene código y Stitch no ganaría nada con él.
4. **Additional instructions:** pega esto:
   ```
   Android mobile app, 360x800 frames, dark mode only. Use the attached images as mood references for the liquid glass, gradients and glow, not as layouts to copy. All UI text in Spanish. Album detail must always be a modal bottom sheet.
   ```
5. **Continue.** Luego genera las pantallas con los bloques 2, 3 y 4 del mensaje anterior, uno a la vez. El Bloque 1 ya no hace falta pegarlo porque ese contenido está en el DESIGN.md.

Si una pantalla sale con un estilo distinto, basta con decirle a Stitch "follow the DESIGN.md glass and gradient rules" en lugar de volver a describir el estilo.

---

## 3. Prompts 1–8 por pantalla

Cuando Stitch termine de generar el design system, pega estos prompts **en orden y de a uno**, esperando a que termine cada uno. Ya no repiten el estilo porque eso lo toma del design system. Al inicio de cada pantalla puse un ID (P01, P02…) para que en Figma los frames queden nombrados igual que en la wiki.

**Prompt 1: Bienvenida y perfil**
```
Following the Vinilos design system, create 2 screens:

P01 "Bienvenida": hero with a large glowing vinyl record over gradient orbs, title "Vinilos", subtitle "Descubre, colecciona y comparte música". Two large glass cards: "Explorar como visitante" (eye icon) and "Entrar como coleccionista" (vinyl icon).

P01b "¿Quién eres?": same background blurred with a glass bottom sheet titled "¿Quién eres?", listing 4 collectors (gradient avatar with initials, name, email) and a primary button "Continuar".
```

**Prompt 2: Catálogo de álbumes (HU01)**
```
Following the Vinilos design system, create 2 versions of the screen "Álbumes":

P02 "Catálogo – Visitante": top app bar "Álbumes" with profile chip "Visitante", glass search field "Buscar álbum o artista", horizontal genre chips (Todos, Salsa, Rock, Folk, Classical), 2-column grid of glass album cards (cover with vinyl peeking out, album name, artist, year). Floating glass bottom navigation: Álbumes (active), Artistas, Coleccionistas.

P02c "Catálogo – Coleccionista": identical, but the profile chip shows the collector's initials and there is a floating circular gradient FAB "+" labeled "Crear álbum".
```

**Prompt 3: Detalle de álbum como modal (HU02)**
```
Following the Vinilos design system, create the album detail as a MODAL BOTTOM SHEET over the blurred catalog (not a new page). Create 2 versions:

P03 "Detalle de álbum – Visitante": drag handle and close X. Big cover with the vinyl sliding out to the right, album name "Buscando América", artist chip "Rubén Blades" (tappable), info row: "1984", genre chip "Salsa", "Elektra". Description paragraph. Section "Tracks" with numbered rows (name + duration mm:ss). Section "Comentarios" with star ratings and short texts.

P03c "Detalle de álbum – Coleccionista": same content plus a primary gradient button "Agregar track" and glass buttons "Agregar a mi colección" and "Comentar".
```

**Prompt 4: Formularios de álbum (HU07, HU08, HU09, HU11)**
```
Following the Vinilos design system, create 4 screens:

P08 "Crear álbum": full-screen glass form titled "Nuevo álbum", live cover preview card at top. Fields: Nombre, URL de la portada, Fecha de lanzamiento (date picker), Descripción (multiline), Género (dropdown: Classical, Salsa, Rock, Folk), Sello discográfico (dropdown: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records). Show the "Nombre" field in error state: "Este campo es obligatorio". Buttons "Cancelar" (glass) and "Guardar álbum" (gradient).

P09 "Agregar track": glass bottom sheet over the blurred album modal. Fields: Nombre del track, Duración with hint "Ej. 04:35". Buttons "Cancelar" and "Agregar".

P10 "Comentar álbum": glass bottom sheet with a 5-star rating selector, multiline field "Escribe tu comentario", button "Publicar comentario".

P11 "Agregar a mi colección": glass bottom sheet with field "Precio" and segmented selector "Activo / Inactivo", button "Agregar a mi colección".
```

**Prompt 5: Artistas (HU03, HU04, HU10)**
```
Following the Vinilos design system, create 3 screens:

P04 "Artistas": top app bar "Artistas", glass segmented tabs "Todos / Músicos / Bandas", search field, list of glass rows (circular photo with gradient ring, name, chip "Músico" or "Banda", chevron). Bottom navigation with Artistas active. Top-right trophy icon button "Premios".

P05 "Detalle de músico – Coleccionista": full screen, large hero photo fading into the gradient background, name "Rubén Blades", chip "Músico", "Nacimiento: 16/07/1948", description. Heart icon button "Agregar a favoritos" in its active (filled) state. Section "Álbumes" as a horizontal carousel of album cards. Section "Premios" with glass trophy cards (name, organization, date). Buttons "Agregar álbum" and "Asignar premio".

P05b "Detalle de banda – Coleccionista": same layout for the band "Queen", chip "Banda", "Creación: 1970", extra section "Integrantes" with musician avatars, and an extra button "Agregar músico".
```

**Prompt 6: Acciones sobre artistas (HU12, HU13, HU14, HU15)**
```
Following the Vinilos design system, create 5 screens:

P12 "Agregar músico a la banda": glass bottom sheet with search field and a list of musicians with checkboxes, button "Agregar a la banda".

P13 "Asociar álbum al artista": glass bottom sheet with search field and a list of albums (cover thumbnail + name) with radio selection, button "Asociar álbum".

P14 "Premios": full screen list of glass trophy cards (name, organization, description) with a gradient FAB "+" labeled "Crear premio".

P15 "Crear premio": glass bottom sheet with fields Nombre, Organización, Descripción; buttons "Cancelar" and "Crear premio".

P16 "Asignar premio": glass bottom sheet with dropdown "Premio" and date picker "Fecha de premiación", button "Asignar premio".
```

**Prompt 7: Coleccionistas (HU05, HU06)**
```
Following the Vinilos design system, create 2 screens:

P06 "Coleccionistas": top app bar "Coleccionistas", search field, list of glass cards (gradient avatar with initials, name, stats "12 álbumes · 4 artistas favoritos", chevron). Bottom navigation with Coleccionistas active.

P07 "Detalle de coleccionista": full screen glass profile header with large gradient avatar, name "Manolo Bellon", contact rows with icons (teléfono, correo). Section "Artistas favoritos" as a horizontal avatar carousel. Section "Su colección" as an album grid showing price and a status chip (Activo / Inactivo). Section "Comentarios" listing the collector's reviews with stars.
```

**Prompt 8: Estados y feedback**
```
Following the Vinilos design system, create the state variants for the "Álbumes" catalog screen and the feedback components:

P02-loading: catalog with glass skeleton cards with shimmer.
P02-empty: illustration of an empty vinyl crate and the message "Aún no hay álbumes en el catálogo".
P02-error: message "No pudimos cargar los álbumes" and a gradient button "Reintentar".
Toasts: success "Álbum creado", success "Track agregado", error "No se pudo guardar. Intenta de nuevo".
```

---

**Si algo sale mal:**
- Si una pantalla sale con fondo claro o sin el efecto glass, responde `Apply the Vinilos design system: dark background with gradient orbs and glass surfaces`.
- Si el detalle del álbum sale como página completa, responde `Make it a modal bottom sheet over the blurred catalog, with drag handle and close X`.
- Si un prompt de 4 o 5 pantallas deja alguna a medias, pide solo la pantalla que faltó, por ejemplo `Now create only P15`.

Cuando tengas todo, exporta a Figma y conecta la navegación como te expliqué antes: el modal del álbum y los formularios como overlays, y dos flujos de inicio, uno de visitante y otro de coleccionista.

---

## 4. Revisión P01 y P01b

Sí, quedaron muy bien: el glass, los gradientes y el vinilo siguen bien el design system. Antes de seguir con el Prompt 2 vale la pena corregir cuatro detalles, porque Stitch tiende a repetir en las pantallas siguientes lo que ya generó:

1. **El badge "PRO"** hace pensar que ser coleccionista es una función de pago, y en Vinilos no lo es.
2. **"HI-FI AUDIO EXPERIENCE"** está en inglés, y toda la interfaz debe estar en español.
3. **Los subtítulos de las tarjetas se cortan** ("sin ini…", "agrega tracks …"). Texto truncado en la pantalla de entrada se ve descuidado y además resta en la revisión de accesibilidad del Sprint 3.
4. **En "¿Quién eres?"**, detrás del sheet no se ve la pantalla de bienvenida difuminada, y abajo hay una segunda barra que sobra.

La mitad inferior vacía de P01 está bien, deja respirar la pantalla. Si quieres, puedes pedir que las tarjetas bajen un poco.

**Prompt de ajuste (pégalo antes del Prompt 2):**
```
Refine P01 and P01b:
- P01: remove the "PRO" badge from the collector card. Replace "HI-FI AUDIO EXPERIENCE" with "CATÁLOGO DE VINILOS". Card subtitles must wrap to two lines instead of being truncated: "Navega por álbumes, artistas y coleccionistas" and "Crea álbumes, agrega tracks y gestiona tu colección".
- P01b: show the P01 welcome screen blurred behind the bottom sheet, and remove the extra bar below the "Continuar" button.
```

Cuando quede, sigue con el **Prompt 2 (Catálogo de álbumes)**. Lo más importante ahí es que la barra de navegación inferior y las tarjetas de álbum salgan bien, porque se reutilizan en casi todas las pantallas que vienen.

---

## 5. Revisión P02 y P02c

Van bien: las tarjetas con el vinilo asomando y el FAB "Crear álbum" quedaron muy logradas. Hay algunos detalles que conviene corregir ahora, porque estas dos pantallas son la base del resto.

**Lo que más importa: datos que el backend no tiene**
- En las tarjetas aparecen "12" LP", "2x LP", "33 RPM", "Ediciones 180g", "Vinilos catalogados: 148" y calificaciones con estrellas (4.9, 5.0). La API no devuelve nada de eso. Si queda en el mockup, después el equipo no lo podrá implementar o tendrá que inventarlo.
- Los chips "Salsa Clásica" y "Rock Sinfónico" no existen: los géneros son solo Salsa, Rock, Folk y Classical.
- El texto de la tarjeta del visitante promete "calificar pistas" y "armar cajas", que no son funcionalidades de la app.

**Consistencia entre las dos versiones**
- El encabezado es distinto en cada una ("Catálogo oficial" frente a "Bóveda privada / Catálogo acústico"). Deberían ser idénticas salvo el chip de perfil y el FAB; si no, en Figma parecen pantallas diferentes.
- La visitante muestra año y género, y la del coleccionista año y sello. Hay que escoger un solo formato.

**Detalles visuales**
- En el encabezado se cruzan el subtítulo pequeño y el chip de perfil, y el ícono de persona repite lo que ya dice el chip.
- La barra inferior no marca la pestaña activa, y "Coleccionistas" queda pegado al borde.
- En la portada de "A Night at the Opera" de la versión visitante aparece un pedazo de interfaz ("Catálogo - Visitante") dentro de la imagen.

**Prompt de ajuste:**
```
Refine P02 and P02c so both share exactly the same layout:
- Header: title "Álbumes" with subtitle "Explora el catálogo de vinilos" on its own line, no overlapping text. On the right only the search icon button and the profile chip ("Visitante" or "MG · Coleccionista"); remove the extra person icon.
- Genre chips must be exactly: Todos, Salsa, Rock, Folk, Classical.
- Album cards show only API data: cover with vinyl, album name, artist, and "año • género". Remove "12" LP", "2x LP", "33 RPM", star ratings, "Ediciones 180g" and the "Vinilos catalogados" counter.
- Bottom navigation: "Álbumes" active with the gradient glow indicator; reduce label size so "Coleccionistas" fits with margin.
- P02 visitor info card text: "Entra como coleccionista para crear álbumes, agregar tracks y comentar". Keep its button "Acceder como coleccionista".
- Fix the "A Night at the Opera" cover so it shows only the artwork.
```

Después sigue con el **Prompt 3 (detalle de álbum como modal)**. Ahí revisa sobre todo que salga como bottom sheet encima del catálogo difuminado y no como página completa.

---

## 6. Revisión P03: el modal se resuelve en Figma

Sí y no. Stitch genera pantallas estáticas, así que no puede hacer un modal "real" que se abra sobre otra pantalla. Lo que sí puede es **dibujar** el sheet encima de una versión difuminada del catálogo dentro de la misma pantalla. No lo hizo porque generó el sheet a pantalla completa, y en la versión visitante además perdió el tema oscuro.

**Mi recomendación: que el modal de verdad lo haga Figma, no Stitch.** En Figma, el detalle del álbum se arma como un frame solo con el sheet y se conecta desde el catálogo con *Open overlay*, posición *Bottom* y fondo oscurecido. Así detrás se ve la pantalla real desde la que se abrió: catálogo, artista o coleccionista. Una imagen fija de Stitch siempre mostraría el catálogo, aunque el álbum se abra desde otra pantalla. Lo único que necesitas de Stitch es que el sheet se vea bien y deje ver un poco del fondo arriba.

**Otros detalles que conviene corregir:**
- Volvió a salir "Salsa Clásica"; el género es solo "Salsa".
- Cada track muestra el artista y tiene menú "⋮", y el track 03 tiene una estrella. La API solo devuelve nombre y duración, y editar o borrar tracks no está en ninguna HU.
- La portada de la versión visitante es una captura de otra app de música, con botones de reproducir.
- Lo bueno: la versión coleccionista quedó muy bien en estructura (botones, comentarios con estrellas, "Dejar reseña"). Úsala como base.

**Prompt de ajuste:**
```
Redo P03 and P03c using P03c as the base layout, both in dark mode only (never light/white backgrounds):
- Make it a real bottom sheet: the sheet starts at about 12% from the top of the screen, with rounded 32dp top corners and a drag handle. Above and behind it, show the P02 "Álbumes" catalog screen blurred and darkened (black overlay ~50%), so the user can see the screen they came from.
- Sheet surface: dark glass (surface-glass-strong over the dark background), never white or light gray.
- Genre chip must be "Salsa" (not "Salsa Clásica").
- Track rows show only number, track name and duration. Remove the artist name under each track, the "⋮" menus and the star icon.
- The album cover must be only the album artwork (no app UI or buttons inside the image).
- P03 (visitor): same layout without "Agregar track al álbum", "Mi colección", "Comentar" and "Dejar reseña"; header chip "Modo visitante".
```

Si aun así Stitch no deja ver el fondo, no te preocupes: el efecto lo resuelves en Figma con el overlay. En ese caso basta con que el sheet salga oscuro y con los datos correctos, y puedes seguir con el **Prompt 4**.

---

## 7. P03 aprobado + Prompt 4 actualizado

Sí, quedó mucho mejor: el sheet es oscuro, el fondo se ve difuminado arriba, el género dice "Salsa" y los tracks muestran solo nombre y duración.

Una aclaración: lo que acabas de generar ya es el resultado del **Prompt 3** (detalle de álbum). El que sigue es el **Prompt 4** (formularios).

Un pendiente que no vale la pena seguir peleando con Stitch: **las portadas**. La del visitante sigue siendo una captura de otra app de música, y la del coleccionista trae texto encima ("P03c: Detalle de álbum…" y la lista de canciones). Es más rápido corregirlo en Figma: selecciona la imagen y reemplázala por la portada real del álbum (Fill → Image). Toma unos segundos por pantalla.

Te dejo el Prompt 4 actualizado con lo que aprendimos en los ajustes anteriores (modo oscuro, sheets sobre fondo difuminado, solo datos de la API), para evitar las mismas correcciones:

**Prompt 4: Formularios de álbum (HU07, HU08, HU09, HU11)**
```
Following the Vinilos design system, dark mode only (never white or light backgrounds), create 4 screens. Use only the fields listed; do not add extra data or decorative labels.

P08 "Crear álbum": full screen (not a sheet). Top app bar with back arrow and title "Nuevo álbum". Live cover preview card at top (placeholder vinyl until a URL is entered). Glass fields with labels above: Nombre, URL de la portada, Fecha de lanzamiento (date picker icon), Descripción (multiline), Género (dropdown with exactly: Classical, Salsa, Rock, Folk), Sello discográfico (dropdown with exactly: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records). Show the "Nombre" field in error state with red border and helper text "Este campo es obligatorio". Bottom buttons: "Cancelar" (glass) and "Guardar álbum" (gradient).

P09 "Agregar track": dark glass bottom sheet starting at about 45% of the screen height, over the P03c album sheet blurred and darkened behind it. Drag handle, title "Nuevo track", subtitle "Buscando América". Fields: "Nombre del track" and "Duración" with hint "Ej. 04:35". Buttons "Cancelar" and "Agregar track".

P10 "Comentar álbum": same dark glass bottom sheet style over the blurred album sheet. Title "Tu reseña", 5-star rating selector (3 stars selected), multiline field "Escribe tu comentario", button "Publicar comentario".

P11 "Agregar a mi colección": same dark glass bottom sheet style over the blurred album sheet. Title "Agregar a mi colección", field "Precio" with "$" prefix, segmented selector "Activo / Inactivo" (Activo selected), button "Agregar a mi colección".
```

Si el lote de 4 queda incompleto, pide las pantallas que falten por separado, por ejemplo `Now create only P10 and P11`.

---

## 8. Revisión formularios P08–P11 contra la API

Se ven bien en estilo. Comparé cada formulario con las validaciones del backend y hay varias cosas que no coinciden. Conviene corregirlas, porque en Sprint 3 el equipo va a implementar estos formularios tal como queden en el mockup.

**P08 — Crear álbum**
- **"Descripción" dice "Opcional", pero la API la exige.** Si se envía vacía, el `POST /albums` falla.
- El sello aparece como "Elektra Records"; en la API el valor es "Elektra".
- Volvió a aparecer "12" LP VINILO", y "Artista no asignado" sobra porque al crear un álbum no se escoge artista.
- El título "Nuevo álbum" y el chip "MG · Coleccionista" se parten en dos líneas.

**P09 — Agregar track**
- Está bien. Solo sobra "Tiempo estimado": la duración es un dato del track, no una estimación.
- Que el fondo se vea vacío no importa, en Figma lo resuelves con el overlay.

**P10 — Comentar álbum**
- **Salió cortado:** el campo de texto queda a medias y falta el botón "Publicar".
- "Prensado HQ" no tiene sentido en el formulario.
- Dice "5.0 • Obra maestra", pero las estrellas se ven vacías.

**P11 — Agregar a mi colección**
- **Falta el selector Activo / Inactivo, y la API lo exige.**
- **El precio dice "Opcional", pero también es obligatorio.** Además va en una sola moneda; "COP / USD" confunde.

**Prompt de ajuste:**
```
Refine P08, P10 and P11 (dark mode only, keep current style):

P08 "Crear álbum": title "Nuevo álbum" on one line; profile chip shortened to "MG" avatar only. Preview card: remove "12" LP VINILO" and "Artista no asignado"; show only cover placeholder, album name, genre chip and record label. All six fields are required: mark each label with "*" and remove the "Opcional" tag from Descripción. Record label options exactly: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records (show "Elektra", not "Elektra Records").

P10 "Comentar álbum": complete the bottom sheet so everything fits: title, album subtitle, label "Calificación" with 5 stars where 4 are filled in amber and the label reads "4 de 5", remove the "Prensado HQ" badge, a multiline field "Escribe tu comentario" (required), and buttons "Cancelar" and "Publicar comentario" at the bottom of the sheet.

P11 "Agregar a mi colección": field "Precio (COP) *" (required, remove "Opcional" and "USD"), then a required segmented selector "Estado *" with options "Activo" (selected) and "Inactivo", then buttons "Cancelar" and "Agregar a mi colección".
```

En **P09**, el texto "Tiempo estimado" lo puedes borrar directamente en Figma.

Cuando quede, sigue con el **Prompt 5 (Artistas)**.

---

## 9. P10 aprobado, exportación a Figma

Sí, el de la derecha ya está bien. Las estrellas están llenas, el campo de texto está completo y aparecen "Publicar comentario" y "Cancelar". Lo de "Publicando como Martín Gómez (Coleccionista)" quedó muy bien, porque deja claro qué coleccionista comenta.

**Sobre la diferencia con "Copy as PNG"**
- La pantalla está bien, el problema es la exportación. Arriba del frame aún dice "Generating Screen…", así que es probable que el PNG se haya capturado antes de que Stitch terminara de dibujar el sheet, y por eso sale cortado.
- Espera a que termine de generar y vuelve a copiar.
- Para pasar a Figma no uses PNG: usa **"Copy to Figma"** o el plugin. Así llegan capas editables en vez de una imagen plana, y puedes corregir textos, cambiar portadas y conectar la navegación.

**Tres textos que conviene borrar directamente en Figma** (no vale la pena otro prompt):
- El badge **"Prensado HQ"** y **"Audición en tornamesa"**: no corresponden a ningún dato ni acción de la app.
- **"Máx. 500 caract."** y el contador **"0 / 500"**: la API no tiene ese límite, así que en el mockup sería una regla que la app no aplica.

"5.0 • Obra maestra" puede quedarse: con 5 estrellas tiene sentido como etiqueta de la calificación.

Cuando P08 y P11 estén corregidos, sigue con el **Prompt 5 (Artistas)**.

---

## 10. Prompt de ajuste de formularios

Este prompt incluye todo lo que queda por corregir en los formularios: P08 y P11 completos, y en P09 y P10 solo los textos que sobran, para que no tengas que borrarlos a mano en Figma. Lo que ya quedó bien se mantiene.

```
Refine these screens, dark mode only, keeping the current style and everything else as it is:

P08 "Crear álbum":
- Title "Nuevo álbum" on a single line; replace the profile chip with only the "MG" avatar.
- Preview card: remove "12" LP VINILO" and "Artista no asignado". Show only the cover placeholder, album name, genre chip and record label.
- All six fields are required: add "*" to every label (Nombre, URL de la portada, Fecha de lanzamiento, Género, Sello discográfico, Descripción) and remove the "Opcional" tag from Descripción.
- Record label dropdown shows "Elektra" (not "Elektra Records"). Options: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records.

P09 "Agregar track":
- Remove the "Tiempo estimado" label next to "Duración". Both fields are required: add "*" to "Nombre del track" and "Duración".

P10 "Comentar álbum":
- Remove the "Prensado HQ" badge, the "Audición en tornamesa" text, the "Máx. 500 caract." label and the "0 / 500" counter.
- Keep the 5 filled stars, "5.0 • Obra maestra", the "Publicando como" row and both buttons.

P11 "Agregar a mi colección":
- Field label "Precio (COP) *" (required): remove "Opcional" and "USD".
- Below it add a required segmented selector "Estado *" with "Activo" (selected) and "Inactivo".
- Keep the buttons "Cancelar" and "Agregar a mi colección" at the bottom of the sheet.
```

Cuando termine de generar, revisa P11 primero: es el único al que se le agregan elementos nuevos y el que más probablemente quede incompleto. Si falta el selector de estado, pide solo `Now refine only P11`.

---

## 11. Regla de botones 50/50, Cancelar a la izquierda

Listo. Cambié la parte de P10 para que los botones queden lado a lado, mitad y mitad, con Cancelar a la izquierda, como en los otros tres formularios. Agregué también una regla general para que Stitch no vuelva a apilar los botones en las pantallas que siguen.

```
Refine these screens, dark mode only, keeping the current style and everything else as it is.

Global rule for all forms and bottom sheets: action buttons sit side by side at the bottom, each taking 50% of the width with a small gap; "Cancelar" (glass) always on the left and the primary gradient action always on the right. Never stack the buttons vertically.

P08 "Crear álbum":
- Title "Nuevo álbum" on a single line; replace the profile chip with only the "MG" avatar.
- Preview card: remove "12" LP VINILO" and "Artista no asignado". Show only the cover placeholder, album name, genre chip and record label.
- All six fields are required: add "*" to every label (Nombre, URL de la portada, Fecha de lanzamiento, Género, Sello discográfico, Descripción) and remove the "Opcional" tag from Descripción.
- Record label dropdown shows "Elektra" (not "Elektra Records"). Options: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records.

P09 "Agregar track":
- Remove the "Tiempo estimado" label next to "Duración". Both fields are required: add "*" to "Nombre del track" and "Duración".

P10 "Comentar álbum":
- Remove the "Prensado HQ" badge, the "Audición en tornamesa" text, the "Máx. 500 caract." label and the "0 / 500" counter.
- Keep the 5 filled stars, "5.0 • Obra maestra" and the "Publicando como" row.
- Replace the stacked buttons with two side-by-side buttons at 50% width each: "Cancelar" (glass) on the left and "Publicar comentario" (gradient) on the right.

P11 "Agregar a mi colección":
- Field label "Precio (COP) *" (required): remove "Opcional" and "USD".
- Below it add a required segmented selector "Estado *" with "Activo" (selected) and "Inactivo".
- Keep "Cancelar" on the left and "Agregar a mi colección" on the right at the bottom of the sheet.
```

En P10, "Publicar comentario" es el texto más largo de los cuatro formularios. Si al quedar al 50% se corta o se parte en dos líneas, cámbialo por "Publicar" directamente en Figma.

---

## 12. Formularios aprobados + Prompt 5 (artistas)

Quedaron bien los cuatro. Las cosas que había que corregir están todas: campos obligatorios, "Elektra", el selector de estado y los botones a 50% con Cancelar a la izquierda. En P11 Stitch agregó dos textos que sobran y puedes borrar en Figma: "Disponible en estantería para reproducción · Visible" y la tarjeta "Este álbum se vinculará a tu perfil…".

Sigue el **Prompt 5: Artistas (HU03, HU04, HU10)**. Lo ajusté con las reglas de las correcciones anteriores:

```
Following the Vinilos design system, dark mode only (never white or light backgrounds), create 3 screens. Use only the data listed; do not invent extra fields, badges, ratings or counters.

P04 "Artistas": same header pattern as P02 (title "Artistas", subtitle "Músicos y bandas del catálogo" on its own line, search icon button and profile chip "MG"). Glass segmented tabs "Todos / Músicos / Bandas" (Todos selected), search field "Buscar artista". List of glass rows: circular photo with gradient ring, artist name, chip "Músico" or "Banda", chevron. Include: Rubén Blades (Músico), Queen (Banda), Willie Colón (Músico), Fania All-Stars (Banda), Celia Cruz (Músico). Floating bottom navigation with "Artistas" active (gradient glow indicator). Top-right trophy icon button "Premios" next to the search icon.

P05 "Detalle de músico – Coleccionista": full screen (not a sheet). Back arrow at top left. Large hero photo of Rubén Blades fading into the gradient background, name "Rubén Blades", chip "Músico", row "Nacimiento: 16/07/1948", short description. Heart icon button "Agregar a favoritos" shown in its active (filled, gradient) state next to the name. Section "Álbumes" as a horizontal carousel of album cards (cover with vinyl, name, year). Section "Premios" with glass trophy cards (prize name, organization, date). At the bottom, two side-by-side buttons at 50% width: "Asignar premio" (glass) on the left and "Agregar álbum" (gradient) on the right.

P05b "Detalle de banda – Coleccionista": same layout for the band "Queen": chip "Banda", row "Creación: 01/01/1970", description, heart button in its inactive (outline) state. Extra section "Integrantes" between the description and "Álbumes", showing musician avatars with names (Freddie Mercury, Brian May, Roger Taylor, John Deacon). Sections "Álbumes" and "Premios" as in P05. A full-width gradient button "Agregar músico" above two side-by-side buttons at 50% width: "Asignar premio" (glass) on the left and "Agregar álbum" (gradient) on the right.
```

En el resultado, revisa que P05 y P05b salgan como **pantallas completas** con flecha atrás, no como sheet; en este bloque es lo que más fácil se confunde. Los dos estados del corazón (lleno en P05 y vacío en P05b) te sirven después en Figma para mostrar la acción de favoritos de HU10.

---

## 13. Revisión artistas + Prompt 6

Los dos detalles quedaron muy bien, y tiene sentido usar el hero de Queen como base para los dos. Hay varias cosas que corregir, sobre todo en el listado.

**P04 — Listado de artistas**
- **El encabezado está duplicado:** "Artistas" aparece dos veces, más un ícono de persona extra arriba a la derecha.
- **La barra inferior salió blanca.** Tiene que ser la misma barra oscura de vidrio de P02.
- El país debajo de cada nombre ("Latinoamérica", "Reino Unido"…) no viene en la API.
- Sobra el ícono de búsqueda, porque ya está el campo "Buscar artista".

**P05 — Rubén Blades**
- Pasarlo al estilo hero de Queen y quitar la barra "Detalle Artista" con el ícono de persona.
- Los nombres de los premios se cortan ("Grammy Award al Mejo…"); deberían ocupar dos líneas.

**P05b — Queen**
- **Hay dos flechas atrás:** la de la barra superior y la del hero. Debe quedar solo la del hero.
- Los roles de los integrantes ("Voz y piano", "Bajo") no vienen en la API, y "Freddie Me…" se corta.
- "3 ediciones" es un contador que sobra.
- El ícono de "Asignar premio" pasa a ser el trofeo de Rubén Blades.

Las portadas de "Buscando América" y "News of the World" vuelven a traer capturas de otra app. Esas cámbialas en Figma, como las anteriores.

**Prompt de ajuste:**
```
Refine P04, P05 and P05b, dark mode only, keeping the current style:

P04 "Artistas":
- Single header: remove the duplicated top "Artistas" bar and the extra person icon. Keep one title "Artistas" with subtitle "Músicos y bandas del catálogo", and on the right only the trophy button "Premios" and the "MG" avatar. Remove the search icon button (the search field stays).
- Rows show only photo, artist name, chip "Músico"/"Banda" and chevron. Remove the country subtitle under each name.
- Bottom navigation must be the same dark glass floating bar as P02 (never white), with "Artistas" active using the gradient glow indicator.

P05 "Detalle de músico":
- Use the same full-bleed hero layout as P05b: the photo fills the top of the screen and fades into the background, with the back arrow and the "MG" avatar floating over the image. Remove the "Detalle Artista" top bar and its person icon.
- Over the hero: chip "MÚSICO" and "Nacimiento: 16/07/1948", name "Rubén Blades", heart button in its active (filled gradient) state, description below.
- Prize names may wrap to two lines instead of being truncated.

P05b "Detalle de banda":
- Remove the "Detalle Artista" top bar; keep only the back arrow and "MG" avatar floating over the hero.
- "Integrantes" cards show only avatar and full name (no roles); names wrap instead of truncating.
- Remove the "3 ediciones" counter.
- The "Asignar premio" button uses the same outline trophy icon as P05.
```

Cuando termines, sigue con el **Prompt 6: Acciones sobre artistas (HU12, HU13, HU14, HU15)**:

```
Following the Vinilos design system, dark mode only, create 5 screens. Use only the fields listed. Global rule: action buttons sit side by side at 50% width each, "Cancelar" (glass) on the left and the primary gradient action on the right; never stack them.

P12 "Agregar músico a la banda": dark glass bottom sheet over the blurred P05b band screen. Title "Agregar músico", subtitle "Queen". Search field "Buscar músico", list of musicians (circular photo, name) with checkboxes, two of them checked. Buttons "Cancelar" and "Agregar a la banda".

P13 "Asociar álbum al artista": dark glass bottom sheet over the blurred P05 musician screen. Title "Asociar álbum", subtitle "Rubén Blades". Search field "Buscar álbum", list of albums (cover thumbnail, name, year) with radio selection, one selected. Buttons "Cancelar" and "Asociar álbum".

P14 "Premios": full screen with back arrow, title "Premios", subtitle "Reconocimientos del catálogo" and the "MG" avatar. List of glass trophy cards showing only prize name (may wrap to two lines), organization and description. Gradient FAB "+" labeled "Crear premio".

P15 "Crear premio": dark glass bottom sheet over the blurred P14 screen. Title "Nuevo premio". Required fields with "*": Nombre, Organización, Descripción (multiline). Buttons "Cancelar" and "Crear premio".

P16 "Asignar premio": dark glass bottom sheet over the blurred P05 screen. Title "Asignar premio", subtitle "Rubén Blades". Required fields with "*": dropdown "Premio" and date picker "Fecha de premiación". Buttons "Cancelar" and "Asignar premio".
```

Son 5 pantallas, así que es posible que el lote quede incompleto. Si pasa, pide las que falten, por ejemplo `Now create only P15 and P16`.

---

## 14. Revisión P12–P16 + Prompt 7

Tienes razón con las dos cosas, y al revisar encontré algo más que explica el texto negro.

**El texto negro son los nombres de los frames**
Stitch metió dentro de cada sheet una barra con el nombre del frame, en texto oscuro, y además con el nombre equivocado:
- "P12 Detalle De Album" en Agregar músico.
- "P13 Agregar Track" en Asociar álbum.
- "P15 Agregar Comentario" en Crear premio.
- "P16 Cambiar Perfil" en Asignar premio.
- En P14 aparece "P14 Premios" arriba y otra vez "Premios" debajo.

Hay que quitar esa barra en todos.

**El fondo**
Los sheets salieron gris claro, no del vidrio oscuro de P03c. Además el fondo de atrás debería verse más oscuro y difuminado.

**Datos inventados (mismo patrón de antes)**
- **P12:** subtítulos de los músicos ("Voz / Colaborador…") y "En gira y colaboraciones".
- **P13:** "Clásico de Oro" y el contador "4 opciones".
- **P14:** la tarjeta "Total galardones registrados · Oficial".
- **P15:** la tarjeta informativa, "Sugerencias rápidas" y "0/300".
- **P16:** "Maestro de la Salsa", "Reconocimiento oficial" y la tarjeta del álbum "Siembra". Esta última es la más importante: **el premio se asigna al artista, no a un álbum**, así que esa tarjeta da a entender algo que la app no hace.

**Prompt de ajuste:**
```
Refine P12, P13, P14, P15 and P16, dark mode only:

All bottom sheets (P12, P13, P15, P16):
- Remove the top bar showing the frame name ("P12 Detalle De Album", "P13 Agregar Track", "P15 Agregar Comentario", "P16 Cambiar Perfil"). Each sheet starts with the drag handle, then its title and the close X.
- Use exactly the same dark glass sheet surface as P03c (deep dark, never light gray). The screen behind is blurred and darkened with a ~50% black overlay.
- All text is white (#F5F5FA) or muted light gray (#C4C4D4); never black or dark text.

P12 "Agregar músico": subtitle only "Queen". Musician rows show only photo and name; remove the role/description lines and "Catálogo Queen".
P13 "Asociar álbum": subtitle only "Rubén Blades". Album rows show only cover, name and year; remove "Clásico de Oro", record label text and the "4 opciones" counter.
P14 "Premios": remove the "P14 Premios" top bar (keep a single header: back arrow, title "Premios", subtitle and "MG" avatar). Remove the "Total galardones registrados / Oficial" card. Prize cards show only organization, name and description.
P15 "Crear premio": title "Nuevo premio". Keep only the three required fields (Nombre, Organización, Descripción) and the buttons; remove the info card, the "Sugerencias rápidas" chips and the "0/300" counter.
P16 "Asignar premio": subtitle only "Rubén Blades". Keep only the dropdown "Premio *" and the date picker "Fecha de premiación *" and the buttons; remove "Maestro de la Salsa", "Reconocimiento oficial", the "Formato DD/MM/AAAA" hint and the "Siembra" album card (prizes are assigned to the artist, not to an album).
```

Cuando quede, sigue con el último de pantallas, el **Prompt 7: Coleccionistas (HU05, HU06)**:

```
Following the Vinilos design system, dark mode only, create 2 screens. All text white or light gray, never dark. Use only the data listed; do not invent extra fields, badges, ratings or counters. Do not show frame names inside the screens.

P06 "Coleccionistas": same header pattern as P04 (title "Coleccionistas", subtitle "Conoce a otros amantes del vinilo" on its own line, and the "MG" avatar on the right). Search field "Buscar coleccionista". List of glass cards: gradient avatar with initials, name, and below it the email; chevron on the right. Include: Martín Gómez, Sofía Londoño, Carlos Rodríguez, Ana Valencia, Manolo Bellon. Same dark glass floating bottom navigation as P02, with "Coleccionistas" active using the gradient glow indicator.

P07 "Detalle de coleccionista": full screen (not a sheet). Back arrow and "MG" avatar floating at the top. Hero header with a large gradient avatar "SL", name "Sofía Londoño", and two contact rows with icons: teléfono "+57 310 555 1234" and correo "sofia.vinyl@sound.co". Section "Artistas favoritos": horizontal carousel of circular artist photos with names (Rubén Blades, Queen, Celia Cruz). Section "Su colección": 2-column album grid (cover with vinyl, album name, price in COP and a status chip "Activo" or "Inactivo"). Section "Comentarios": glass cards with album name, star rating and comment text.
```

Después solo queda el **Prompt 8** (estados de carga, vacío y error, y los toasts).

---

## 15. Lista de arreglos manuales en Figma (P12–P16)

De acuerdo, se arregla en Figma, y es más rápido que pelear otra ronda con Stitch. Te dejo la lista por pantalla para que no se te escape nada cuando las tengas allá.

**En los cuatro sheets (P12, P13, P15, P16): el fondo**
1. Selecciona el sheet de P03c y copia sus propiedades con `⌘ + ⌥ + C`.
2. Selecciona el sheet gris de cada pantalla y pégalas con `⌘ + ⌥ + V`. Así quedan con el mismo vidrio oscuro.
3. El fondo de atrás no lo pintes en cada frame: lo pone el overlay, que oscurece y difumina la pantalla real desde donde se abre.

**Qué borrar o cambiar en cada pantalla**

| Pantalla | Borrar | Cambiar |
|---|---|---|
| **P12** Agregar músico | Título "P12 Detalle De Album", el subtítulo de cada músico ("Voz / Colaborador…"), "Catálogo Queen" | Subtítulo del sheet: solo "Queen" |
| **P13** Asociar álbum | Título "P13 Agregar Track", "4 opciones", "Clásico de Oro" | Subtítulo: solo "Rubén Blades". Debajo de cada álbum deja solo el año |
| **P14** Premios | La barra "P14 Premios" con el ícono de persona, la tarjeta "Total galardones registrados · Oficial" | Agrega la flecha atrás junto al título "Premios" |
| **P15** Crear premio | La tarjeta informativa, "0/300", "Sugerencias rápidas" con sus chips | El título "P15 Agregar Comentario" pasa a "Nuevo premio" |
| **P16** Asignar premio | "Maestro de la Salsa", "Reconocimiento oficial", "Formato DD/MM/AAAA", **la tarjeta del álbum "Siembra"** | El título "P16 Cambiar Perfil" pasa a "Asignar premio" |

La tarjeta de "Siembra" en P16 es la más importante de quitar. El premio se asigna al artista, y si la dejas, quien revise el prototipo va a entender que se asigna a un álbum.

Lo demás está bien: los botones lado a lado con Cancelar a la izquierda, los campos obligatorios con `*`, y en P14 las tarjetas de premios con organización, nombre y descripción.

Revisa el resultado del **Prompt 7** cuando termine. Después solo queda el **Prompt 8** (estados y toasts), y con eso ya tienes todas las pantallas para pasar a Figma.

---

## 16. Revisión P06, P07, estados y toasts

Van bien. P07 y la pantalla de carga son de lo mejor que ha salido. Hay tres patrones que se repiten, más un par de detalles por pantalla.

**Patrones que se repiten**
- **El nombre del frame vuelve a salir como título:** "P06 Coleccionistas" (con el avatar MG duplicado) y "P07 Detalle De Coleccionista".
- **Los géneros vuelven a inventarse** en los estados vacío y de error: "Salsa Clásica", "Jazz Fusión", "Salsa Brava", "Rock Clásico", "Folk Andino". Jazz ni siquiera existe en la API. Deben ser los mismos chips de P02: Todos, Salsa, Rock, Folk, Classical.
- **En la barra inferior de P06**, "Coleccionistas" sale en texto oscuro y cortado contra el borde.

**Detalles por pantalla**
- **P07:** las estrellas del comentario salen vacías y no se entiende la calificación. La portada de "Buscando América" trae otra vez una captura; esa cámbiala en Figma.
- **Vacío:** el botón dice "+ + Crear primer álbum" (doble +). La "Guía rápida de inicio" dice "3 pasos" pero muestra 2, y habla de "cara A y cara B", que la app no maneja.
- **Error:** "HTTP 503 · Servidor no disponible" es un detalle técnico que el usuario no debería ver. "Ir a Coleccionistas" y la tarjeta de "¿Estás en modo avión?" sobran.
- **Feedback:** no es una pantalla de la app sino una hoja de especificaciones con datos inventados ("Acetato 180G", "Edición Remasterizada"). Además, el toast de error se corta ("No se pudo guar…"). Para el prototipo solo necesitas los tres toasts.

**Prompt de ajuste:**
```
Refine these screens, dark mode only, all text white or light gray. Never show frame names (like "P06" or "P07") inside the screens.

P06 "Coleccionistas": remove the "P06 Coleccionistas" top bar and the duplicated "MG" avatar; keep a single header (title "Coleccionistas", subtitle, one "MG" avatar). Bottom navigation: the active "Coleccionistas" tab label must be white and fully visible inside the pill (reduce label size if needed).

P07 "Detalle de coleccionista": remove the "P07 Detalle De Coleccionista" title; keep only the back arrow and the "MG" avatar floating at the top. In "Comentarios", show 4 filled amber stars out of 5 and add a second comment card for "A Night at the Opera" with 5 filled stars.

P02 empty, loading and error states: the genre chips must be exactly the same as P02: Todos, Salsa, Rock, Folk, Classical (one line each, never wrapped).
- Empty: button text "Crear primer álbum" with a single "+" icon. Remove the "Guía rápida de inicio" section.
- Error: keep the broken vinyl illustration, title "No pudimos cargar los álbumes", message and the "Reintentar" button. Remove "HTTP 503 • Servidor no disponible", the "Ir a Coleccionistas" button and the "¿Estás en modo avión?" card.

Toasts: replace the feedback specification screen with a simple screen showing the P02 catalog dimmed in the background and three stacked toasts above the bottom navigation: success "Álbum creado", success "Track agregado", and error "No se pudo guardar" with subtitle "Intenta de nuevo" and a "Reintentar" action. No text truncation, no specification panels.
```

**Si Stitch no lo corrige, en Figma es rápido:**
- Borra los títulos "P06…" y "P07…".
- Copia la fila de chips de P02 y pégala sobre los estados vacío y de error.
- Del frame de feedback, quédate solo con los tres toasts como componentes y borra el resto.

**Un detalle para revisar en Figma:** la pestaña activa de la barra inferior ahora sale como píldora con gradiente (en P04, P06 y los estados), mientras que P02 tenía un brillo. Escoge uno de los dos estilos y aplícalo igual en todas las pantallas que tienen la barra. La píldora se ve más clara.

Con esto ya tienes las pantallas de las 15 HU. El siguiente paso es exportar a Figma, hacer las correcciones manuales que fuimos anotando y conectar la navegación.
