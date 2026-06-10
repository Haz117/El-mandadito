# El Mandadito 🛵

Aplicación de delivery de comida a domicilio para México. Proyecto full-stack compuesto por un backend Spring Boot y una app Android nativa.

---

## Estructura del repositorio

```
.
├── mandadito-backend/   # API REST — Spring Boot 3.2.5 + Kotlin
└── ElMandadito/         # App Android — Kotlin + Jetpack Compose + Views
```

---

## Backend

### Stack
- **Spring Boot 3.2.5** + Kotlin 1.9.23
- **PostgreSQL** (Flyway migrations)
- **Spring Security 6** stateless con JWT (jjwt 0.12.5)
- **springdoc-openapi 2.5.0** — Swagger UI en `/swagger-ui.html`

### Módulos
| Módulo | Endpoints |
|--------|-----------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` |
| Restaurants | `GET /api/restaurants`, `GET /api/restaurants/{id}`, `GET /api/restaurants/nearby` |
| Menu | `GET /api/restaurants/{id}/menu`, `POST/PUT/DELETE /api/menu-items` |
| Cart | `GET/POST/DELETE /api/cart`, `DELETE /api/cart/clear` |
| Orders | `POST /api/orders`, `GET /api/orders/my`, `GET /api/orders/{id}`, `POST /api/orders/{id}/cancel` |
| Payments | `POST /api/payments/create`, `POST /api/payments/{id}/confirm` |
| Notifications | `POST /api/notifications/register-token`, `DELETE /api/notifications/unregister-token` |
| Loyalty | `GET /api/loyalty/my`, `POST /api/loyalty/redeem` |
| Admin | `GET /api/admin/users`, `POST /api/admin/businesses/{id}/approve` |

### Roles
`USER` · `BUSINESS_OWNER` · `DRIVER` · `ADMIN`

### Configuración (variables de entorno)

```env
DB_URL=jdbc:postgresql://localhost:5432/mandadito
DB_USERNAME=postgres
DB_PASSWORD=tupassword
JWT_SECRET=clave-secreta-256-bits
JWT_EXPIRATION=86400000
PORT=8080
```

### Migraciones Flyway
| Versión | Contenido |
|---------|-----------|
| V1 | `users` |
| V2 | `businesses`, `restaurants`, `menu_items` |
| V3 | `addresses`, `carts`, `cart_items`, `orders`, `order_items` |
| V4 | `favorites`, `reviews` |
| V5 | `device_tokens`, `payments` |
| V6 | `order_events`, `loyalty_points`, `promotions` |

### Ejecutar localmente

```bash
cd mandadito-backend
./mvnw spring-boot:run
```

---

## Android

### Stack
- **Kotlin** + **Jetpack Compose** (UI principal) + XML Views (legacy fragments)
- **Hilt** — inyección de dependencias
- **ViewModel + StateFlow** — arquitectura MVVM
- **Retrofit 2.11 + OkHttp** — capa de red
- **DataStore Preferences** — persistencia del JWT
- **Coil 2.7** — carga de imágenes (content:// URIs + URLs de red)
- **Firebase Cloud Messaging** — notificaciones push
- **Material 3**

### Arquitectura

```
ui/
  auth/          AuthViewModel  →  LoginActivity, RegisterActivity
  home/          HomeViewModel  →  ComposeHomeFragment → HomeScreen (Compose)
  detail/        RestaurantDetailViewModel  →  RestaurantDetailActivity
  cart/          CartViewModel  →  CartFragment
  profile/       ProfileViewModel  →  ProfileFragment
  common/        UiState<T>  (Idle | Loading | Success | Error)
network/
  api/           AuthApi, RestaurantApi, OrderApi
  repository/    AuthRepository, RestaurantNetworkRepository, OrderNetworkRepository
  dto/           DTOs + RestaurantMapper (RestaurantResponse → Restaurant)
di/
  AppModule      @Singleton providers para todos los repositories
data/
  SampleData     Datos de muestra (fallback cuando el backend no está disponible)
  Models         Restaurant, MenuItem, CartItem, etc.
```

### Configuración

El backend corre en `http://10.0.2.2:8080/` por defecto (emulador → localhost).
Para producción cambia `BASE_URL` en `RetrofitClient.kt`.

### Flujo de autenticación
1. `SplashActivity` consulta `AuthRepository.isLoggedIn()` (DataStore)
2. Si hay token → `MainActivity`; si no → `LoginActivity`
3. `LoginActivity` llama `AuthViewModel.login()` → `AuthRepository` → backend
4. Token se guarda en DataStore; nombre/email en `UserPrefsManager` para la UI local

### Restaurantes
- `HomeViewModel` carga restaurantes del backend al iniciar
- Si el backend no responde, `HomeScreen` muestra `SampleData` como fallback
- Al hacer clic en un restaurante de red, se pasa `restaurant_id_long` a `RestaurantDetailActivity`
- `RestaurantDetailViewModel` carga detalles y menú en paralelo

---

## Features implementadas

- [x] Registro e inicio de sesión con JWT real
- [x] Splash con routing de auth
- [x] Home con restaurantes del backend (fallback a SampleData)
- [x] Pull-to-refresh en home
- [x] Detalle de restaurante + menú desde la red
- [x] Carrito local con validación de restaurante único
- [x] Checkout con selección de método de pago
- [x] Historial de pedidos con calificación por estrellas
- [x] Favoritos
- [x] Perfil con sistema de reputación (estrellas)
- [x] Mandapoints (loyalty) — backend listo, UI usa datos locales
- [x] Búsqueda con historial
- [x] Registro de negocios (local)
- [x] Notificaciones push — FCM completo (Android + backend Firebase Admin SDK)
- [x] Imágenes de restaurantes con Coil (URIs locales + URLs de red)
- [x] Panel de admin — backend listo

## Pendiente

- [ ] Migrar CartFragment a CartViewModel (red)
- [ ] Migrar historial de pedidos a OrderNetworkRepository
- [ ] Subida de imágenes para restaurantes y perfil
- [ ] Mapa con tracking del repartidor

---

## Despliegue en beta

### 1. Firebase (notificaciones push)

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com)
2. Agrega una app Android con package name `com.elmandadito.app`
3. Descarga `google-services.json` y colócalo en `ElMandadito/app/`
4. En **Project Settings → Service Accounts** genera una clave privada (JSON)
5. Sube ese JSON al servidor backend y configura la variable de entorno:

```env
FIREBASE_CREDENTIALS_PATH=/ruta/al/firebase-service-account.json
```

### 2. Backend — variables de entorno en producción

```env
DB_URL=jdbc:postgresql://<host>:5432/mandadito
DB_USERNAME=<usuario>
DB_PASSWORD=<password>
JWT_SECRET=<cadena-aleatoria-mínimo-256-bits>
JWT_EXPIRATION=86400000
FIREBASE_CREDENTIALS_PATH=/ruta/al/firebase-service-account.json
PORT=8080
```

> Genera `JWT_SECRET` con: `openssl rand -base64 32`

### 3. Hosting del backend

Opciones recomendadas para beta:

| Plataforma | Tier gratis | Notas |
|------------|-------------|-------|
| **Railway** | 500 hrs/mes | `railway up` desde el repo, soporta PostgreSQL incluido |
| **Render** | Sí (sleep en inactividad) | Conecta el repo de GitHub, agrega variables de entorno en el dashboard |
| **Fly.io** | 3 VMs compartidas | `fly launch` desde `mandadito-backend/`, más control |

Pasos con Railway (el más rápido):
```bash
npm install -g @railway/cli
railway login
railway init
railway add postgresql       # crea la DB y exporta DATABASE_URL automáticamente
railway up                   # despliega el backend
```

### 4. URL del backend en la app Android

En `ElMandadito/app/src/main/java/com/elmandadito/app/network/RetrofitClient.kt` cambia:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"  // emulador
// →
private const val BASE_URL = "https://tu-app.railway.app/"
```

### 5. Google Play — beta cerrada

1. Crea una cuenta de desarrollador en [play.google.com/console](https://play.google.com/console) ($25 única vez)
2. Crea la app → **Pruebas → Prueba interna / Cerrada**
3. Genera el APK firmado:
   ```
   Android Studio → Build → Generate Signed Bundle/APK → Android App Bundle
   ```
   (crea un keystore nuevo y guárdalo seguro — lo necesitas para todas las actualizaciones)
4. Sube el `.aab` al track de prueba interna
5. Agrega probadores por email desde la consola
