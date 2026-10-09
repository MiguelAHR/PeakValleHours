# PeakValle — Horas PEAK / VALLE de DeepSeek

App Android que te dice **en tiempo real** si estás en **HORA PEAK** (tarifa cara) o **HORA VALLE** (50% de descuento) de la API de DeepSeek, con **cuenta regresiva** hasta el próximo cambio, para que decidas si avanzar o detener tu proyecto.

> Diseño oscuro **neón** con la paleta oficial de DeepSeek.
> Objetivo: **Android 16** (Moto G 75 5G), `package com.peakvalle.hours`.

---

## 1. ¿Cómo funciona la tarifa de DeepSeek?

DeepSeek factura con dos ventanas horarias (valle = **50% más barato**):

| Tarifa | Bloques (UTC) | Días |
| ------ | ------------- | ---- |
| **PEAK** | `01:00–04:00` y `06:00–10:00` | Lunes a viernes |
| **VALLE** | Todas las horas restantes + **todo el fin de semana** | — |

La app calcula internamente en **UTC** (recomendación de DeepSeek para evitar desfases) y muestra tu **hora local**.

Ejemplo en **Perú (UTC−5, sin horario de verano)**:

- **PEAK:** Dom–Jue `20:00–23:00` y Lun–Vie `01:00–05:00`
- **VALLE:** el resto (incluye noches de viernes y todo el fin de semana)

---

## 2. Funcionalidades

- **Dashboard en vivo:** estado actual (PEAK/VALLE), cuenta regresiva `hh:mm:ss`, barra de progreso del bloque y próximo cambio.
- **Notificaciones:** aviso al **entrar a PEAK** y al **entrar a VALLE**, con **aviso anticipado** configurable.
- **Sonido de notificación configurable:** tono del sistema, archivo propio o silencio; vibración on/off.
- **Notificación persistente** con cuenta regresiva en vivo (`setChronometerCountDown`), sin gastar batería.
- **Widget** de pantalla de inicio (Jetpack Glance) con estado y tiempo restante.
- **Ajustes:** zona horaria (auto/manual), sonido, vibración, anticipación.

---

## 3. Paleta de marca (DeepSeek)

| Rol | Color | Hex |
| --- | ----- | --- |
| Marca | DeepSeek Blue | `#4D6BFE` |
| Marca oscura | — | `#2B3FB0` |
| VALLE (neón) | Verde/cian | `#00FFA3` / `#00E5FF` |
| PEAK (neón) | Magenta/naranja | `#FF2D78` / `#FF6B4D` |
| Fondo | — | `#0A0E1A` → `#111633` |
| Superficie | — | `#121826` |
| Texto | — | `#E6E9F5` |
| Texto secundario | — | `#8A93B2` |

---

## 4. Stack técnico

- **Kotlin + Jetpack Compose** (Material 3)
- **MVVM + Clean** (`core` / `domain` / `data` / `ui`)
- **compileSdk/targetSdk 35** (migración a 36 en Android 16), **minSdk 26**
- `kotlinx-datetime` para el motor UTC
- `AlarmManager` exacto + `WorkManager`
- `DataStore` para preferencias
- `Jetpack Glance` para el widget

### Estructura de paquetes

```
com.peakvalle.hours
├─ core/      (datetime, notifications, theme)
├─ domain/    (model, ScheduleEngine, usecases)
├─ data/      (datastore, alarm scheduler)
├─ ui/        (dashboard, settings, components, theme)
└─ widget/
```

---

## 5. Roadmap por fases

- [x] **Fase 0** — Planificación y README
- [x] **Fase 1** — Scaffold del proyecto + tema DeepSeek neón
- [ ] **Fase 2** — Motor de horario (UTC) + tests unitarios
- [ ] **Fase 3** — Dashboard con cuenta regresiva en vivo
- [ ] **Fase 4** — Notificaciones + alarmas exactas
- [ ] **Fase 5** — Ajustes + sonido configurable
- [ ] **Fase 6** — Widget (Glance)
- [ ] **Fase 7** — Pulido, i18n (es/en) y release firmado

---

## 6. Contribuir / ejecutar

1. Abre la carpeta en **Android Studio** (Gradle sync).
2. Ejecuta en tu dispositivo (Android 16) o emulador.
3. Compilar desde consola: `./gradlew :app:assembleDebug`

---

## Licencia

Pendiente. (DeepSeek es una marca de su respectivo propietario; este proyecto no está afiliado a DeepSeek.)
