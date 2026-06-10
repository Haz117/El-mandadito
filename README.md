# El Mandadito 

Aplicación móvil de entrega de comida a domicilio para Android, construida con Kotlin y Jetpack Compose.

---

## Características

- **Home** — Pantalla principal en Jetpack Compose, diseño premium blanco/negro inspirado en Uber Eats
- **Categorías** — Filtrado por tipo de comida: Tacos, Burgers, Pizza, Sushi, Pollo, Postres
- **Promociones** — Banner auto-rotativo con ofertas del día
- **Restaurantes** — Tarjetas con rating, tiempo de entrega, costo de envío y estado abierto/cerrado
- **Carrito** — Gestión del pedido con barra flotante animada y bottom sheet de pago
- **Favoritos** — Guardar y administrar restaurantes favoritos
- **Perfil** — Estadísticas, reputación con estrellas (1–5), historial de pedidos y programa de lealtad *Mandapoints*
- **Historial de búsqueda** — Búsquedas recientes persistentes con `SearchHistoryManager`
- **Negocios** — Registro y gestión de restaurantes propios (RegisterBusinessActivity, MyBusinessesActivity)
- **Tracking** — Pantalla de seguimiento de pedido con mapa de estado en tiempo real
- **Autenticación** — Registro e inicio de sesión con hash SHA-256, modo DEV para bypass durante el desarrollo

---

## UX & Animaciones

- Shimmer skeleton en listas de carga
- Haptic feedback en puntos clave del flujo
- Animaciones de entrada en Register, Cart, Tracking y detalle de restaurante
- Badge bounce en el ícono de carrito
- Scooter flotante y animación de éxito al confirmar pedido
- Bottom nav con efecto bounce
- Count-up en estadísticas de perfil

---

## Tech Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.0 |
| UI principal | Jetpack Compose + Material 3 |
| UI complementaria | XML / View Binding |
| Persistencia | SharedPreferences |
| Imágenes | Glide |
| Autenticación | SHA-256 via `MessageDigest` |
| Animaciones | `animateFloatAsState`, `spring`, `tween`, `AnimatedVisibility` |
| Dependencias | Compose BOM 2024.09.03 · Material Icons Extended |

---

## Estructura del proyecto

```
ElMandadito/
└── app/src/main/
    ├── java/com/elmandadito/app/
    │   ├── compose/          # HomeScreen en Jetpack Compose
    │   ├── data/             # Modelos, repositorios y managers
    │   │   ├── BusinessRepository.kt
    │   │   ├── SearchHistoryManager.kt
    │   │   ├── FavoritesManager.kt
    │   │   ├── OrderHistoryManager.kt
    │   │   └── UserAuthManager.kt
    │   └── ui/
    │       ├── auth/         # Login y Registro
    │       ├── business/     # Registro y gestión de negocios
    │       ├── cart/         # Carrito de compras
    │       ├── favorites/    # Favoritos
    │       ├── home/         # Adapters del home
    │       ├── profile/      # Perfil y reputación
    │       └── detail/       # Detalle de restaurante y menú
    └── res/
        ├── layout/           # XML layouts
        ├── drawable/         # Shapes e íconos
        ├── menu/             # Menús de toolbar
        └── values/           # Colores, strings y temas
```

---

## Instalación

1. Clona el repositorio
   ```bash
   git clone https://github.com/Haz117/El-mandadito.git
   ```
2. Abre la carpeta `ElMandadito/` en **Android Studio Hedgehog** o superior
3. Espera a que Gradle sincronice las dependencias
4. Ejecuta en un emulador o dispositivo físico (API 26+)

> **Modo desarrollo:** `SplashActivity` tiene `DEV_MODE = true` para saltar el login mientras la app está en construcción.

---

## Licencia

Privado — todos los derechos reservados.
