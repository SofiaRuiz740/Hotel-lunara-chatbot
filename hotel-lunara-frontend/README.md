# Hotel Lunara Frontend

Frontend Angular 21 para Hotel Lunara, conectado al backend Spring Boot en `http://localhost:8080`.

## Requisitos

- Node.js 20 o superior
- Backend de Hotel Lunara corriendo en `http://localhost:8080`

## Levantar en desarrollo

```bash
npm install
npm run start
```

La app usa por defecto:

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`

## Rutas principales

- `/home`
- `/habitaciones`
- `/habitaciones/:id`
- `/auth/login`
- `/auth/registro`
- `/concierge`
- `/mi-cuenta`
- `/mi-cuenta/reservas`
- `/mi-cuenta/restaurante`
- `/mi-cuenta/servicios`
- `/mi-cuenta/perfil`
- `/recepcion`
- `/admin`

## Usuarios de prueba

Usa las credenciales ya sembradas por el backend:

- `admin@hotellunara.com / Admin1234!`
- `recepcion@hotellunara.com / Recep1234!`
- `huesped@hotellunara.com / Guest1234!`

## Flujos cubiertos

- Navegacion publica del hotel
- Disponibilidad y detalle de habitaciones
- Registro y login con JWT
- Dashboard del huesped
- Mis reservas con cancelacion
- Reserva de restaurante
- Solicitud de servicios
- Concierge IA anonimo y autenticado
- Panel de recepcion
- Panel administrativo

## Build de produccion

```bash
npm run build -- --configuration=production
```

La salida queda en:

`dist/hotel-lunara-frontend/browser`

## Docker

Construir imagen:

```bash
docker build -t hotel-lunara-frontend .
```

Ejecutar contenedor:

```bash
docker run --rm -p 8081:8080 hotel-lunara-frontend
```

## Nota de despliegue

`src/environments/environment.prod.ts` mantiene una URL placeholder para Cloud Run.
Antes de desplegar en produccion, reemplaza esa URL por la del backend real.
