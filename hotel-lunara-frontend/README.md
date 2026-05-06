# Hotel Lunara Frontend

Frontend Angular 21 para Hotel Lunara.
En desarrollo y producción, el navegador consume `/api` en la misma origin y esa ruta se reenvía al backend desplegado en `https://hotel-lunara-backend-476475787309.us-central1.run.app`.

## Requisitos

- Node.js 20 o superior
- Acceso al backend desplegado o a otro backend compatible si quieres cambiar el proxy

## Levantar en desarrollo

```bash
npm install
npm run start
```

La app usa por defecto:

- Frontend: `http://localhost:4200`
- Backend real: `https://hotel-lunara-backend-476475787309.us-central1.run.app`
- Base consumida por el navegador: `/api`

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

- En desarrollo, `ng serve` usa `proxy.conf.json` para reenviar `/api` al backend desplegado.
- En producción, nginx reenvía `/api` al backend desplegado.
- Si necesitas un backend distinto, puedes definir `API_BASE_URL` en el contenedor frontend.
