# El Mandadito

Aplicación Android de delivery de comida a domicilio para municipios de México. Kotlin nativo con Jetpack Compose, backend en Supabase (PostgreSQL + PostgREST + Auth).

---

## Stack

### Android
| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 + XML Views (fragmentos legacy) |
| Arquitectura | MVVM — ViewModel + StateFlow |
| Inyección de dependencias | Hilt |
| Red | Retrofit 2 + OkHttp + Gson (`LOWER_CASE_WITH_UNDERSCORES`) |
| Sesión | DataStore Preferences (JWT) + SharedPreferences (estado UI) |
| Imágenes | Coil 2.7 (URIs locales + URLs de red) |
| Push | Firebase Cloud Messaging (FCM) |
| minSdk | 26 (Android 8) |

### Backend
| Servicio | Uso |
|----------|-----|
| **Supabase Auth** | Registro, login, recuperación de contraseña (JWT) |
| **Supabase PostgREST** | CRUD de restaurantes, menú, pedidos, reseñas, tokens FCM |
| **Row Level Security** | Cada usuario solo accede a sus propios datos |

---

## Arquitectura Android

```
ui/
  auth/       AuthViewModel     → LoginActivity, RegisterActivity
  home/       HomeViewModel     → ComposeHomeFragment → HomeScreen (Compose)
  detail/     RestaurantDetail… → RestaurantDetailActivity
  cart/       CartViewModel     → CartFragment
  profile/    ProfileViewModel, ReviewViewModel → ProfileFragment
  common/     UiState<T>  (Idle | Loading | Success | Error)

network/
  api/        AuthApi, RestaurantApi, OrderApi, ReviewApi, NotificationApi
  repository/ AuthRepository, RestaurantNetworkRepository,
              OrderNetworkRepository, ReviewNetworkRepository,
              NotificationNetworkRepository
  dto/        DTOs + RestaurantMapper (RestaurantResponse → Restaurant)

di/
  AppModule   @Singleton providers para todos los repositorios

data/
  DataStoreExt     Context.dataStore extension (JWT session)
  SampleData       Datos locales de muestra (fallback / restaurantes sin Supabase ID)
  CartRepository   Carrito con LiveData + SharedPreferences
  OrderHistoryManager  Historial local + merge con pedidos de red
  Models           Restaurant, MenuItem, CartItem, OrderRecord, …
```

---

## Configuración inicial

### 1. Clave anon de Supabase

Crea (o edita) el archivo `local.properties` en la raíz del proyecto:

```properties
sdk.dir=C\:\\Users\\<tu-usuario>\\AppData\\Local\\Android\\Sdk
supabase.url=https://<tu-proyecto>.supabase.co/
supabase.anon.key=<tu-clave-anon>
```

> `local.properties` está en `.gitignore` — nunca se sube al repositorio.

### 2. Seed de base de datos

Ejecuta `supabase_seed.sql` en **Supabase → SQL Editor**. El script:
- Inserta 6 restaurantes y 50 platillos de muestra
- Crea las políticas RLS necesarias (SELECT público en restaurantes/menú, INSERT/SELECT propio en pedidos/reseñas)
- Añade triggers para auto-completar `user_id` en `reviews` y `device_tokens`

### 3. Firebase (notificaciones push)

1. Crea una app Android en [Firebase Console](https://console.firebase.google.com) con package `com.elmandadito.app`
2. Descarga `google-services.json` y colócalo en `app/`
3. El archivo de stub actual en el repo permite compilar pero no entregará push reales

---

## Flujo de autenticación

```
SplashActivity
  ├─ UserPrefsManager.isLoggedIn() = true  →  MainActivity
  └─ false                                  →  LoginActivity

LoginActivity / RegisterActivity
  └─ AuthRepository (Supabase Auth /auth/v1/token, /auth/v1/signup)
       ├─ Éxito: guarda JWT en DataStore + nombre/email en SharedPreferences
       │          registra token FCM en device_tokens
       └─ Error: detecta "email_not_confirmed", "already registered", etc.

Recuperar contraseña
  └─ AuthRepository.recoverPassword()  →  POST /auth/v1/recover
```

Expiración de sesión: `SessionManager.sessionExpired` (StateFlow) limpia DataStore, SharedPreferences y carrito, y redirige al login.

---

## Flujo de pedidos

```
RestaurantDetailActivity
  └─ CartRepository.addItem(menuItem, restaurantName, category, networkRestaurantId)

CartFragment  →  CartViewModel.placeOrder()
  ├─ networkRestaurantId > 0  →  OrderNetworkRepository.createOrder()
  │     ├─ POST /rest/v1/orders        (crea el pedido)
  │     └─ POST /rest/v1/order_items   (guarda los items)
  └─ networkRestaurantId = 0  →  pedido local (sin llamada de red, para restaurantes sin ID de Supabase)

OrderTrackingActivity
  ├─ Animación de progreso local (4 pasos)
  └─ Polling cada 15s a /rest/v1/orders?id=eq.{id}  →  sincroniza estado real
       └─ Al entregar: ReviewViewModel.submitReview()  →  POST /rest/v1/reviews
```

---

## Features implementadas

- [x] Registro e inicio de sesión (Supabase Auth con JWT)
- [x] Recuperación de contraseña por email
- [x] Manejo de confirmación de email (`CONFIRM_EMAIL:` prefix)
- [x] Splash con routing automático según sesión
- [x] Home con restaurantes desde Supabase + fallback a SampleData
- [x] Pull-to-refresh en home
- [x] Detalle de restaurante y menú desde Supabase
- [x] Filtro de restaurantes por categoría y búsqueda
- [x] Carrito con validación de restaurante único, swipe-to-delete, undo
- [x] Código de promoción (MANDADITO20, BIENVENIDO, PROMO10)
- [x] Progreso hacia envío gratis
- [x] Checkout con selección de método de pago (efectivo / tarjeta / OXXO)
- [x] Pedidos registrados en Supabase con sus `order_items`
- [x] Tracking de pedido con polling de estado real desde Supabase
- [x] Calificación con estrellas (bottom sheet) enviada a Supabase
- [x] Historial de pedidos local + merge con pedidos de red
- [x] Sistema de reputación (estrellas) por sanciones
- [x] Mandapoints (loyalty) — cálculo local basado en pedidos
- [x] Favoritos
- [x] Perfil con edición de nombre y gestión de direcciones
- [x] Registro de negocios propio (local)
- [x] Notificaciones push con FCM
- [x] Notificación local al entregar el pedido
- [x] Banner offline cuando no hay conexión
- [x] Sesión expirada → logout automático y redirect al login
- [x] Imágenes con Coil (URIs locales y URLs de red)
- [x] Modo oscuro compatible (colores definidos en theme)

## Pendiente para producción

- [ ] `google-services.json` real (sustituir el stub del repo)
- [ ] Subida de imágenes para restaurantes y perfil (Supabase Storage)
- [ ] Mapa con tracking GPS del repartidor
- [ ] Panel de administración
- [ ] Keystore de release para firmar el APK/AAB

---

## Despliegue en beta

### App Android

1. En Android Studio: **Build → Generate Signed Bundle/APK → Android App Bundle**
2. Crea un keystore nuevo (guárdalo seguro — lo necesitas para todas las actualizaciones)
3. Sube el `.aab` a Google Play Console → **Pruebas → Prueba interna**
   - La cuenta de desarrollador tiene un costo único de $25 USD

### Supabase en producción

El proyecto ya está en Supabase cloud. Para ir a producción:

1. **Deshabilitar confirmación de email** (solo si quieres registro inmediato):
   Dashboard → Authentication → Settings → Email → desactiva "Confirm email"

2. **Rotar la service role key** si fue expuesta en algún momento:
   Settings → API → Regenerate

3. **Política de contraseñas**:
   Authentication → Settings → Password → mínimo 8 caracteres

4. **Backups automáticos**: activos por defecto en el plan Pro de Supabase

---

## Estructura de tablas en Supabase

| Tabla | Descripción |
|-------|-------------|
| `restaurants` | Catálogo de restaurantes (nombre, categoría, tiempos, fees) |
| `menu_items` | Platillos por restaurante (nombre, precio, categoría, disponible) |
| `orders` | Pedidos de usuarios (status, totales, método de pago) |
| `order_items` | Items de cada pedido (nombre, precio, cantidad, subtotal) |
| `reviews` | Calificaciones de pedidos (1–5 estrellas, comentario) |
| `device_tokens` | Tokens FCM por usuario para notificaciones push |

Todas las tablas tienen RLS habilitado. Los datos de restaurantes son públicos (SELECT anon); pedidos, reseñas y tokens solo son accesibles por el usuario autenticado dueño del registro.
