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
- **Glide** — carga de imágenes
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
- [x] Detalle de restaurante + menú desde la red
- [x] Carrito local con validación de restaurante único
- [x] Checkout con selección de método de pago
- [x] Historial de pedidos
- [x] Favoritos
- [x] Perfil con sistema de reputación (estrellas)
- [x] Mandapoints (loyalty) — backend listo, UI usa datos locales
- [x] Búsqueda con historial
- [x] Registro de negocios (local)
- [x] Notificaciones push — infraestructura lista, pendiente Firebase SDK
- [x] Panel de admin — backend listo

## Pendiente

- [ ] Integrar Firebase Admin SDK para notificaciones push reales
- [ ] Migrar CartFragment a CartViewModel (red)
- [ ] Migrar historial de pedidos a OrderNetworkRepository
- [ ] Subida de imágenes para restaurantes y perfil
- [ ] Mapa con tracking del repartidor
