# El Mandadito 

Aplicación móvil de entrega de comida a domicilio para Android, construida con Kotlin y Jetpack Compose.

---

## Características

- **Home** — Pantalla principal en Jetpack Compose, diseño premium blanco/negro inspirado en Uber Eats
- **Categorías** — Filtrado por tipo de comida: Tacos, Burgers, Pizza, Sushi, Pollo, Postres
- **Promociones** — Banner auto-rotativo con ofertas del día
- **Restaurantes** — Tarjetas con rating, tiempo de entrega, costo de envío y estado abierto/cerrado
- **Carrito** — Gestión del pedido con barra flotante animada
- **Favoritos** — Guardar y administrar restaurantes favoritos
- **Perfil** — Estadísticas, reputación con estrellas (1–5), historial de pedidos y programa de lealtad *Mandapoints*
- **Autenticación** — Registro e inicio de sesión con hash SHA-256, modo DEV para bypass durante el desarrollo

---

## Tech Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.0 |
| UI principal | Jetpack Compose + Material 3 |
| UI complementaria | XML / View Binding |
| Persistencia | SharedPreferences |
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
    │   └── ui/
    │       ├── auth/         # Login y Registro
    │       ├── cart/         # Carrito de compras
    │       ├── favorites/    # Favoritos
    │       ├── profile/      # Perfil y reputación
    │       └── detail/       # Detalle de restaurante
    └── res/
        ├── layout/           # XML layouts
        ├── drawable/         # Shapes e íconos
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
