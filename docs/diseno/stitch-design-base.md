---
name: Vinilos
description: Native Android music catalog of vinyl albums, artists and record collectors. Dark "liquid glass" aesthetic with vivid gradients.
platform: android
frame: 360x800dp
language: es
colors:
  background: "#0A0A14"
  surface-glass: "rgba(255,255,255,0.10)"
  surface-glass-strong: "rgba(255,255,255,0.16)"
  border-glass: "rgba(255,255,255,0.18)"
  primary: "#7C3AED"
  secondary: "#22D3EE"
  tertiary: "#EC4899"
  accent-warm: "#F59E0B"
  on-surface: "#F5F5FA"
  on-surface-muted: "#C4C4D4"
  success: "#34D399"
  error: "#F87171"
  gradient-primary: "linear-gradient(135deg, #7C3AED 0%, #22D3EE 100%)"
  gradient-accent: "linear-gradient(135deg, #EC4899 0%, #F59E0B 100%)"
typography:
  font-heading: "Sora"
  font-body: "Inter"
  display: { size: 32sp, weight: 700, line-height: 40sp }
  headline: { size: 24sp, weight: 700, line-height: 32sp }
  title: { size: 18sp, weight: 600, line-height: 24sp }
  body: { size: 15sp, weight: 400, line-height: 22sp }
  label: { size: 13sp, weight: 500, line-height: 18sp }
rounded:
  card: 24dp
  sheet: 32dp
  input: 16dp
  pill: 999dp
spacing:
  base: 8dp
  screen-margin: 16dp
  section-gap: 24dp
elevation:
  glass-blur: 24px
  glow: "0 0 24px rgba(124,58,237,0.45)"
components:
  button-primary: "pill, gradient-primary fill, on-surface text, soft violet glow, min height 48dp"
  button-secondary: "pill, surface-glass fill, 1px gradient border, on-surface text"
  icon-button: "circular glass 48dp, thin rounded icon, glow on active"
  card: "surface-glass, border-glass 1px, rounded.card, backdrop blur, top-left highlight"
  bottom-sheet: "surface-glass-strong, rounded.sheet top corners, drag handle, close X, background content blurred"
  input: "surface-glass, rounded.input, label above, helper/error text below; error state uses error color border"
  chip: "pill, glass; selected chip uses gradient-primary"
  bottom-nav: "floating glass pill bar, 3 tabs, active tab with gradient glow indicator"
  toast: "glass pill at bottom with success or error icon"
---

# Vinilos — Design System

## Overview
Vinilos is a native Android app (Kotlin, Material 3 structure) to browse vinyl albums, artists (musicians and bands) and record collectors, built on the BackVynils REST API. The look is a premium dark **liquid glass** style: frosted translucent surfaces floating over a deep midnight background with large, soft, blurred gradient orbs (violet, cyan, magenta, with a warm amber accent that evokes vinyl). **All UI text is in Spanish.**

## Colors
- Base background `#0A0A14` with 2–3 blurred gradient orbs (primary, secondary, tertiary) behind the content; orbs shift position between screens to create depth.
- Glass surfaces are white at 10–16% opacity with a 1px white border at 18% and a 24px backdrop blur.
- Text is always `#F5F5FA` (or `#C4C4D4` for secondary text). Never use mid-gray text on glass.
- Gradients are reserved for primary actions, selected states and highlights; do not fill large areas with them.

## Typography
Sora for headings (bold, generous letter spacing), Inter for body and labels. All sizes in `sp` so the layout scales with the system font size.

## Shapes and elevation
Cards 24dp corners, bottom sheets 32dp top corners, buttons and chips full pill. Depth comes from blur, translucency and soft colored glows, not from heavy drop shadows.

## Imagery and motif
- Album covers show a black vinyl record with visible grooves peeking out from behind the cover.
- Artist photos are circular with a gradient ring.
- Collectors use gradient avatars with their initials.
- Subtle concentric groove rings may appear as decorative background elements.
- Icons: Material Symbols Rounded, thin stroke.

## Navigation
- Floating glass bottom navigation with three tabs: **Álbumes**, **Artistas**, **Coleccionistas**.
- Transparent top app bar with the screen title and, on the right, a profile chip ("Visitante" or the collector's initials) that opens "Cambiar perfil".
- **Album detail always opens as a modal bottom sheet** over the blurred previous screen, never as a new page. Artist and collector details are full screens.
- Secondary forms (agregar track, comentar, agregar a mi colección, asignar premio, agregar músico, asociar álbum) open as glass bottom sheets.

## Roles
- **Visitante:** read-only browsing.
- **Coleccionista:** chosen from a list of collectors at the start; sees write actions (FAB "+", "Agregar track", "Comentar", "Agregar a mi colección", favoritos, premios). Write actions are hidden in visitor mode.

## States
Every list and detail screen has four variants: **cargando** (glass skeleton shimmer), **contenido**, **vacío** (illustration and message) and **error** (message and a "Reintentar" button). Forms show a field in error state ("Este campo es obligatorio") and a success toast.

## Domain data
- Album: nombre, portada (URL), fecha de lanzamiento, descripción, género, sello discográfico, tracks (nombre, duración mm:ss), artistas, comentarios (estrellas + texto).
- Género (closed list): Classical, Salsa, Rock, Folk.
- Sello discográfico (closed list): Sony Music, EMI, Discos Fuentes, Elektra, Fania Records.
- Musician: nombre, imagen, descripción, fecha de nacimiento. Band: same, with fecha de creación and integrantes.
- Collector: nombre, teléfono, correo, artistas favoritos, álbumes de su colección (precio, estado Activo/Inactivo), comentarios.
- Prize: nombre, organización, descripción; assigned to an artist with a fecha de premiación.
- Sample content: "Buscando América" (Rubén Blades, Salsa, Elektra, 1984), "A Night at the Opera" (Queen, Rock, EMI, 1975), "Poeta del pueblo" (Rubén Blades, Salsa, Fania Records).

## Accessibility
- Text contrast at least 4.5:1 against glass.
- Touch targets at least 48×48 dp.
- Every icon button has a label (it becomes the Android `contentDescription`).
- Layouts tolerate large font sizes.

## Do's and Don'ts
- **Do** keep one primary gradient action per screen.
- **Do** blur the content behind modals and sheets.
- **Don't** put low-contrast gray text on glass.
- **Don't** use light mode.
- **Don't** open the album detail as a full page.
