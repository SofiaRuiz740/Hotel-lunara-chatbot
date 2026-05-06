# Hotel Lunara Backend

Backend empresarial para Hotel Lunara construido con Java 17, Spring Boot 3.2, Spring Security 6, JWT, JPA/Hibernate, PostgreSQL, Swagger/OpenAPI y concierge IA con OpenAI.

## 1. Qué incluye

- Autenticación JWT con `access token` y `refresh token`
- Roles `GUEST`, `RECEPTIONIST`, `ADMIN`
- Reservas de habitaciones con validación de solapamientos
- Reservas de restaurante con control de turnos y disponibilidad real
- Solicitudes de servicios del hotel con control de capacidad
- Concierge IA anónimo y autenticado con contexto real del hotel y del huésped
- Dashboard administrativo
- Audit log con captura automática de IP
- Seeder de datos iniciales
- Docker, Docker Compose y Cloud Build
- Colección Postman lista para pruebas manuales

## 2. Arranque local

### Opción A: Docker Compose

1. Crea `.env` a partir de `.env.example`.
2. Ajusta al menos `OPENAI_API_KEY` si vas a probar el concierge.
3. Ejecuta:

```bash
docker compose up --build
```

La API queda en `http://localhost:8080`.

### Opción B: Maven local

1. Asegura PostgreSQL 15 corriendo.
2. Crea `.env` o exporta las variables requeridas.
3. Ejecuta:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## 3. Credenciales seed

Si la base está vacía al iniciar, se cargan estas cuentas:

| Rol | Email | Password |
|---|---|---|
| ADMIN | `admin@hotellunara.com` | `Admin1234!` |
| RECEPTIONIST | `recepcion@hotellunara.com` | `Recep1234!` |
| GUEST | `huesped@hotellunara.com` | `Guest1234!` |

## 4. Endpoints útiles

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/docs`
- Health: `http://localhost:8080/api/actuator/health`
- Hotel info pública: `http://localhost:8080/api/hotel/info`

## 5. Postman

En la carpeta [postman](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/postman>) quedaron:

- [Hotel-Lunara.postman_collection.json](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/postman/Hotel-Lunara.postman_collection.json>)
- [Hotel-Lunara-Local.postman_environment.json](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/postman/Hotel-Lunara-Local.postman_environment.json>)

### Importación

1. Importa ambos archivos en Postman.
2. Selecciona el environment `Hotel Lunara Local`.
3. Ejecuta primero:
   - `01 - Public`
   - `02 - Auth`
4. Después prueba los flujos por rol.

Las requests de login guardan automáticamente los tokens en variables de entorno.

## 6. Orden recomendado de prueba

### 6.1 Público

Ejecuta:

1. `Health`
2. `Get Hotel Info`
3. `List Rooms`
4. `Room Availability`
5. `List Services`
6. `Service Availability`
7. `Restaurant Availability`
8. `Anonymous Concierge Chat`

Esto valida que una persona no autenticada pueda:

- ver información general del hotel
- ver habitaciones y disponibilidad
- ver servicios
- consultar restaurante
- usar el concierge IA sin contexto personal

### 6.2 Huésped

Ejecuta:

1. `Login Guest`
2. `Get My Profile`
3. `Update My Profile`
4. `Authenticated Concierge Chat`
5. `Get Concierge History`
6. `Create Room Reservation`
7. `Get My Reservations`
8. `Create Restaurant Reservation`
9. `Get My Restaurant Reservations`
10. `Create Service Request`
11. `Get My Service Requests`
12. `Cancel Restaurant Reservation` (opcional, ejecútalo después de probar recepción)
13. `Cancel Room Reservation` (opcional, ejecútalo después de probar recepción)

Notas:

- La reserva de habitación genera fechas dinámicas para quedar apta para pruebas operativas de recepción.
- La creación de reservas y solicitudes guarda automáticamente IDs en variables de Postman.
- La cancelación de restaurante por huésped falla si faltan menos de 2 horas.
- La cancelación de habitación por huésped falla si faltan menos de 48 horas.

### 6.3 Recepción

Ejecuta:

1. `Login Receptionist`
2. `List All Reservations`
3. `Today Operations`
4. `Check-in Reservation`
5. `Check-out Reservation`
6. `List Restaurant Reservations`
7. `Update Restaurant Reservation Status`
8. `List Service Requests`
9. `Update Service Request Status`
10. `Change Room Status`

Esto valida:

- operación diaria
- check-in/check-out
- filtros operativos
- gestión de solicitudes
- actualización del estado de habitaciones

Importante:

- Si vas a probar `check-in`, `check-out` y los cambios de estado de recepción, no ejecutes antes las cancelaciones del folder `03 - Guest`.
- La reserva de restaurante usada por recepción tampoco debe quedar cancelada antes de probar `COMPLETADA` o `NO_SHOW`.

### 6.4 Administrador

Ejecuta:

1. `Login Admin`
2. `Dashboard`
3. `Audit Logs`
4. `List Users`
5. `Create Receptionist`
6. `Change User Role`
7. `Change User Status`
8. `Create Room`
9. `Update Created Room`
10. `Deactivate Created Room`
11. `Create Hotel Service`
12. `Update Created Service`
13. `Disable Created Service`

## 7. Revisión de cumplimiento funcional

### Cumple

- Seguridad JWT con `Authorization: Bearer {token}`
- Refresh token público
- Roles `GUEST`, `RECEPTIONIST`, `ADMIN`
- Habitaciones públicas y búsqueda de disponibilidad
- Reservas de habitación con código único, snapshot de precio y control de solapamientos
- Check-in y check-out operativos
- Servicios del hotel con control de capacidad
- Concierge IA anónimo y autenticado con contexto de hotel y huésped
- Historial de conversación
- Dashboard administrativo
- Soft delete de habitaciones
- Activación/desactivación de usuarios
- Audit log persistente

### Ajustado en esta revisión

- Se agregó `GET /api/hotel/info` para que el frontend tenga un endpoint público de datos generales del hotel.
- Se corrigió el path de health para cumplir el contrato esperado: `GET /api/actuator/health`.
- Se agregó filtro por `RoomType` en disponibilidad de habitaciones.
- Se agregó la regla de cancelación tardía de restaurante para huésped: menos de 2 horas no permitido.
- Se agregaron filtros operativos para recepcionista/admin en:
  - reservas de habitaciones
  - reservas de restaurante
  - solicitudes de servicio
- Se agregó `GET /api/reservations/today-operations` para panel operativo del día.
- El audit log ahora captura IP automáticamente desde el request.
- La confirmación o paso a `EN_PROCESO` de una solicitud de servicio revalida capacidad del slot.
- Se bloquearon reservas y solicitudes de servicio en horarios pasados.

### Residuales no bloqueantes para empezar frontend

- No hay pasarela de pagos ni preautorización de tarjetas.
- `emailVerificado` existe en modelo, pero no hay flujo de verificación por correo.
- No hay migraciones Flyway/Liquibase; el esquema depende de Hibernate por perfil.
- La información pública del hotel es config-driven, no editable desde admin.
- No hay rate limiting ni circuit breaker para OpenAI.

Nada de lo anterior bloquea el inicio del frontend si el alcance sigue siendo el que definiste.

## 8. Comandos de verificación usados

```bash
mvn -q -DskipTests compile
mvn -q test
mvn -q -DskipTests package
```

## 9. Archivos clave

- App principal: [HotelLunaraApplication.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/HotelLunaraApplication.java>)
- Seguridad: [SecurityConfig.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/config/SecurityConfig.java>)
- Reservas: [ReservationService.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/reservation/ReservationService.java>)
- Restaurante: [RestaurantService.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/restaurant/RestaurantService.java>)
- Servicios: [ServiceRequestService.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/hotelservice/ServiceRequestService.java>)
- Concierge: [ConciergeService.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/concierge/ConciergeService.java>)
- Seeder: [DataInitializer.java](</C:/Users/MI PC/Desktop/Programacion2/Preparcial/src/main/java/com/hotellunara/common/seeder/DataInitializer.java>)
