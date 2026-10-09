# PeakValle — Horas PEAK / VALLE de DeepSeek

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF)]()
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.06-4285F4)]()
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

App Android que te dice **en tiempo real** si estás en **HORA PEAK** (tarifa completa) o **HORA VALLE** (50% de descuento) de la API de DeepSeek, con **cuenta regresiva** hasta el próximo cambio, para que decidas si seguir con tu proyecto o detenerlo.

> Tema oscuro **neón** con la paleta oficial de DeepSeek.
> `package com.peakvalle.hours` · `minSdk 26` · `compileSdk/targetSdk 35`

---

## 1. ¿Cómo funciona la tarifa de DeepSeek?

DeepSeek factura con dos ventanas horarias (**VALLE = 50% más barato**):

| Tarifa | Bloques (UTC) | Días |
| ------ | ------------- | ---- |
| **PEAK** | `01:00–04:00` y `06:00–10:00` | Lunes a viernes |
| **VALLE** | Todas las horas restantes + **todo el fin de semana** | — |

La app calcula **siempre en UTC** (así lo recomienda DeepSeek para evitar desfases) y muestra tu **hora local**.

Ejemplo en **Perú (UTC−5, sin horario de verano)**:

- **PEAK:** Dom–Jue `20:00–23:00` y Lun–Vie `01:00–05:00`
- **VALLE:** el resto (incluye las noches de viernes y todo el fin de semana)

---

## 2. Funcionalidades

- **Dashboard en vivo** — estado actual (PEAK/VALLE), cuenta regresiva `hh:mm:ss`, barra de progreso del bloque y hora del próximo cambio.
- **Agenda de 24 h** — tira horizontal con las franjas PEAK/VALLE que te esperan.
- **Notificaciones exactas** — aviso al entrar a PEAK y a VALLE, con **alarma exacta** (`setExactAndAllowWhileIdle`).
- **Aviso anticipado** — opcional: 5, 10, 15 o 30 minutos antes del cambio.
- **Sonido configurable** — tono del sistema, silencioso o un archivo de audio propio.
- **Notificación persistente** — estado y cuenta atrás en la barra, con `setChronometerCountDown` (la cuenta la pinta el sistema, sin gastar batería).
- **Widget de pantalla de inicio** — con Jetpack Glance.
- **Ajustes** — notificaciones, anticipación, sonido y notificación persistente.
- **Bilingüe** — español e inglés.

---

## 3. Capturas

<!-- Añade aquí las capturas de pantalla: docs/dashboard.png, docs/settings.png, docs/widget.png -->

---

## 4. Paleta de marca (DeepSeek)

| Rol | Color | Hex |
| --- | ----- | --- |
| Marca | DeepSeek Blue | `#4D6BFE` |
| Marca oscura | — | `#2B3FB0` |
| VALLE (neón) | Verde / cian | `#00FFA3` / `#00E5FF` |
| PEAK (neón) | Magenta / naranja | `#FF2D78` / `#FF6B4D` |
| Fondo | — | `#0A0E1A` → `#111633` |
| Superficie | — | `#121826` |
| Texto | — | `#E6E9F5` |
| Texto secundario | — | `#8A93B2` |

---

## 5. Stack técnico

- **Kotlin 1.9.24** + **Jetpack Compose** (Material 3, BOM 2024.06.00)
- **MVVM** con `StateFlow` + `kotlinx-datetime 0.6.1`
- **AGP 8.6.0** / **Gradle 8.7** · `minSdk 26` · `compileSdk 35`
- **`AlarmManager`** exacto (sin servicio en primer plano)
- **DataStore Preferences** para los ajustes
- **Jetpack Glance 1.1.1** para el widget
- **JUnit 4** para los tests del motor de horario

### Estructura de paquetes

```
com.peakvalle.hours
├─ domain/
│  ├─ ScheduleEngine.kt        Motor PEAK/VALLE en UTC (sin dependencias de Android)
│  └─ model/                    RateStatus, RateWindow, CurrentRate, AppSettings
├─ data/
│  ├─ alarm/                    TransitionScheduler, TransitionReceiver, BootReceiver
│  ├─ notification/             NotificationChannels, Notifications
│  └─ settings/                 SettingsRepository (DataStore)
├─ ui/
│  ├─ dashboard/                DashboardScreen, DashboardViewModel
│  ├─ settings/                 SettingsScreen, SettingsViewModel
│  ├─ components/               NeonCard
│  ├─ util/                     TimeFormat
│  └─ theme/                    Color, Theme, Type
└─ widget/                      PeakValleWidget (Glance)
```

El motor de horario es **lógica pura**: no depende de Android, por eso se puede testear sin instrumentación.

---

## 6. Compilar y ejecutar

```bash
# Debug
./gradlew :app:assembleDebug

# Tests del motor de horario
./gradlew :app:testDebugUnitTest

# Release (minificado con R8)
./gradlew :app:assembleRelease
```

El APK de debug queda en `app/build/outputs/apk/debug/`.

En Android Studio: abrir la carpeta, *Gradle sync* y ejecutar en el dispositivo.

### Permisos

La app pide:

- **`POST_NOTIFICATIONS`** — para los avisos (Android 13+).
- **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`** — para que el aviso llegue en el minuto exacto. Si no está concedido, la app cae a una alarma inexacta y lo avisa en pantalla.

---

## 7. Tests

`app/src/test/java/com/peakvalle/hours/domain/ScheduleEngineTest.kt` cubre:

- Bloques PEAK de lunes a viernes y los huecos entre ellos.
- Viernes por la noche y todo el fin de semana (bloque VALLE largo).
- `windowsBetween` recortando la ventana en curso.
- Un **barrido de 7 días** que verifica que la ventana actual nunca se truca y siempre existe una siguiente.
- La **cadena de alarmas**: 60 transiciones seguidas, cada una con una siguiente válida.

---

## 8. Roadmap

- [x] **Fase 0** — Planificación y README
- [x] **Fase 1** — Scaffold del proyecto + tema DeepSeek neón
- [x] **Fase 2** — Motor de horario (UTC) + tests unitarios
- [x] **Fase 3** — Dashboard con cuenta regresiva en vivo
- [x] **Fase 4** — Notificaciones + alarmas exactas
- [x] **Fase 5** — Ajustes + sonido configurable (DataStore)
- [x] **Fase 6** — Widget de pantalla de inicio (Glance)
- [x] **Fase 7** — Notificación persistente, i18n (es/en) y build de release

### Ideas para más adelante

- Migrar `compileSdk/targetSdk` a 36 (Android 16).
- Firma de release con keystore propia.
- Glance 1.2+ para poder hacer el widget pulsable (`clickable` no existe en 1.1.1).

---

## Licencia

[MIT](LICENSE)

DeepSeek es una marca de su respectivo propietario. Este proyecto **no está afiliado ni respaldado por DeepSeek**; solo reproduce su horario público de tarifas publicado por la empresa.