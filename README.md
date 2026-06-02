# 💸 PAGAPP

**PagaApp** es una aplicación móvil Android que combina la **gestión de gastos compartidos entre amigos** con un sistema de **entrega de efectivo a domicilio en tiempo real** (modelo cliente ↔ repartidor), apoyado en mapas, sensores del dispositivo, cámara y notificaciones push.

La app nace para resolver dos problemas cotidianos: dividir cuentas grupales sin discusiones y conseguir efectivo de forma rápida y segura sin tener que desplazarse hasta un cajero.

> Proyecto académico — Introducción a la Computación Móvil (ICM-3448), Pontificia Universidad Javeriana, Bogotá.

---

## 🚀 Novedades de esta versión (rama `feature/mapas-sensores`)

Esta entrega evoluciona PagaApp de una app de gastos hacia una plataforma de servicios financieros de proximidad. Lo nuevo:

| Funcionalidad | Descripción |
|---|---|
| 🗺️ **Mapas con Google Maps** | Integración de Google Maps Compose para visualizar puntos de efectivo cercanos y el seguimiento en vivo del repartidor. |
| 📍 **Geolocalización en tiempo real** | Uso del `FusedLocationProviderClient` para capturar la ubicación precisa del cliente al solicitar efectivo y la del repartidor durante la entrega. |
| 👥 **Flujo multiusuario cliente ↔ repartidor** | Dos roles diferenciados (`cliente` / `repartidor`) con navegación y pantallas propias, sincronizados en tiempo real mediante Firestore. |
| 💵 **Solicitud y entrega de efectivo** | El cliente solicita un monto; el repartidor lo acepta, marca "en camino" y finaliza la entrega. Todos los estados se reflejan al instante en ambos dispositivos. |
| 📷 **Evidencia de entrega (Cámara + Galería)** | El repartidor adjunta una foto como prueba de entrega usando la cámara o la galería; la imagen se sube a Firebase Storage. |
| 🌗 **Sensor de luz → modo nocturno automático** | El sensor de luminosidad (`TYPE_LIGHT`) cambia el mapa a estilo oscuro y muestra una alerta de conducción cuando hay poca luz. |
| 🔔 **Notificaciones push (Firebase Cloud Messaging)** | Notificaciones del sistema vía FCM más un historial de notificaciones in-app que se actualiza con cada cambio de estado del pedido. |
| 🧾 **Registro de pagos con comprobante** | Pantalla para registrar el pago de una deuda adjuntando evidencia fotográfica. |

---

## 🎯 Objetivo del proyecto

Desarrollar una solución móvil que permita a los usuarios:

- Registrar y dividir gastos compartidos entre amigos.
- Gestionar deudas pendientes y registrar pagos con comprobante.
- Solicitar efectivo y recibirlo a domicilio mediante un repartidor.
- Hacer seguimiento de la entrega en un mapa en tiempo real.
- Localizar puntos de efectivo cercanos (cajeros, corresponsales, aliados).
- Recibir notificaciones de cada evento relevante.

---

## ✨ Funcionalidades principales

### Autenticación y roles
Inicio de sesión con **Firebase Authentication**. Tras autenticarse, la app consulta el rol del usuario en Firestore y lo enruta a la experiencia de **cliente** (panel principal) o de **repartidor** (panel de pedidos).

### Panel principal (Home)
Muestra el balance general, lo que el usuario debe y le deben, accesos rápidos a "Cash Points" y "Cash Delivery", deudas pendientes y un panel de notificaciones con contador de no leídas.

### Gastos y deudas (Expenses)
Listados de "lo que debes" y "lo que te deben", con estados (pendiente/pagado) y montos totales calculados automáticamente.

### Registro de pagos
Permite saldar una deuda registrando el método de pago y adjuntando una foto del comprobante (cámara o galería).

### Puntos de efectivo cercanos (Location)
Mapa centrado en la ubicación del usuario con un listado de cajeros, corresponsales bancarios, tiendas y aliados, cada uno con distancia y enlace directo a "Cómo llegar" en Google Maps.

### Solicitud de efectivo (Request Cash)
El cliente ingresa un monto, la app captura su ubicación precisa y crea una solicitud en Firestore. Inmediatamente pasa a la pantalla de seguimiento.

### Seguimiento del pedido (Tracking)
Mapa en vivo con la posición del cliente y, una vez asignado, la del repartidor. Un panel inferior muestra el estado actual (`pendiente → aceptado → en_camino → entregado`) y notifica cada cambio.

### Panel del repartidor
Lista las solicitudes **disponibles** y las **en curso**. El repartidor acepta un pedido, abre el mapa de entrega, marca "en camino" y finaliza adjuntando la evidencia fotográfica.

### Mapa de entrega (Delivery Map)
Vista del repartidor con su ubicación y la del cliente, controles de estado, captura de evidencia y **modo nocturno automático** según el sensor de luz.

### Historial y perfil
Consulta de movimientos de ingresos/egresos y pantalla de perfil con estadísticas y ajustes.

---

## 🏗️ Arquitectura

PagaApp sigue el patrón **MVVM** sobre **Jetpack Compose**, con un backend **serverless** apoyado en Firebase.

```
UI (Composables)  ←→  ViewModel (StateFlow / UiState)  ←→  Firebase (Auth · Firestore · Storage · FCM)
        ↑                                                          ↑
   Sensores · Cámara · GPS  ──────────────────────────────────────┘
```

- **Capa de UI:** pantallas declarativas en Jetpack Compose + Material 3, navegadas con Navigation Compose.
- **Capa de presentación:** un `ViewModel` por pantalla expone un `StateFlow<UiState>` inmutable.
- **Capa de datos / servicios:** Firebase Authentication (sesión y rol), Cloud Firestore (solicitudes en tiempo real con `addSnapshotListener`), Firebase Storage (evidencias) y Firebase Cloud Messaging (notificaciones).
- **Capa de dispositivo:** `FusedLocationProviderClient` (GPS), `SensorManager` (sensor de luz) y `ActivityResultContracts` para cámara/galería.

### Entidades / colecciones clave

- **users** — perfil y `role` (`cliente` | `repartidor`).
- **solicitudesEfectivo** — documento central del flujo de entrega: `clienteId`, `clienteNombre`, `monto`, `clienteLatitud/Longitud`, `estado`, `repartidorId`, `repartidorNombre`, `repartidorLatitud/Longitud`, `evidenciaUrl`, `timestamp`, `fechaEntrega`.

---

## 🛠️ Tecnologías utilizadas

| Categoría | Stack |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Arquitectura | MVVM, StateFlow / Coroutines |
| Backend | Firebase Authentication, Cloud Firestore, Firebase Storage, Firebase Cloud Messaging |
| Mapas y ubicación | Google Maps Compose, Play Services Maps, Play Services Location |
| Sensores | `SensorManager` (sensor de luz) |
| Multimedia | Cámara / Galería (`ActivityResultContracts`), Coil para carga de imágenes |
| Build | Gradle (Kotlin DSL), `compileSdk 35`, `minSdk 24` |

---

## 📁 Estructura del proyecto

```
app/src/main/java/com/example/pagaapp/
├── MainActivity.kt                 # Entry point, permiso de notificaciones, token FCM
├── navigation/                     # Routes, NavHost y bottom bar
│   ├── Routes.kt
│   ├── AppNavigation.kt
│   └── AppBottomBar.kt
├── ui/
│   ├── screens/
│   │   ├── login/                  # Autenticación + enrutado por rol
│   │   ├── home/                   # Panel principal + notificaciones
│   │   ├── expenses/               # Gastos, deudas y registro de pago
│   │   ├── location/               # Mapa de puntos de efectivo
│   │   ├── cash/                   # Solicitud y seguimiento de efectivo (cliente)
│   │   ├── repartidor/             # Panel y mapa de entrega (repartidor)
│   │   ├── tracking/               # Seguimiento general
│   │   ├── history/                # Historial de transacciones
│   │   └── profile/                # Perfil del usuario
│   └── theme/                      # Color, Theme, Type
└── utils/
    ├── MyFirebaseMessagingService.kt   # Recepción de notificaciones FCM
    └── NotificationHelper.kt           # Historial de notificaciones in-app
```

---

## 🔐 Permisos requeridos

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — ubicación del cliente y del repartidor.
- `CAMERA` — captura de evidencia de entrega y comprobantes de pago.
- `POST_NOTIFICATIONS` — notificaciones push (Android 13+).
- `INTERNET` — comunicación con Firebase y Google Maps.

---

## ▶️ Cómo ejecutar el proyecto

1. Clonar el repositorio y abrir la carpeta en **Android Studio** (Giraffe o superior).
2. Configurar un proyecto en **Firebase** y descargar tu propio `google-services.json` en `app/`.
3. Habilitar en Firebase: Authentication (Email/Password), Firestore, Storage y Cloud Messaging.
4. Crear una **API Key de Google Maps** y configurarla en el `meta-data` del `AndroidManifest.xml`.
5. Crear usuarios de prueba en Authentication y, en la colección `users`, asignar el campo `role` (`cliente` o `repartidor`).
6. Sincronizar Gradle, conectar un dispositivo/emulador con Google Play Services y ejecutar.

> ⚠️ **Seguridad:** no publiques tu `google-services.json` ni tus API keys en un repositorio público. Usa `local.properties` o variables de entorno y revoca cualquier clave que haya quedado expuesta en el historial.

---

## 🗺️ Diagramas

El proyecto incluye los siguientes modelos en **PlantUML** (carpeta `diagramas/`):

- **Diagrama de clases** — entidades, ViewModels y estados de UI.
- **Diagrama de casos de uso** — actores Cliente y Repartidor.
- **Diagrama de secuencia** — flujo completo de solicitud y entrega de efectivo.
- **Diagrama de arquitectura / componentes** — capas de la aplicación y servicios Firebase.

---

## 👥 Equipo

Proyecto desarrollado por el equipo del curso Introducción a la Computación Móvil (ICM-3448) — Pontificia Universidad Javeriana.
