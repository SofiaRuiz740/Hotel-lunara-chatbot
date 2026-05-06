# Deploy en Google Cloud

## 1. Crear proyecto en Google Cloud

1. Crea un proyecto nuevo en Google Cloud Console.
2. Asigna un `PROJECT_ID` sin espacios.
3. Configura facturacion para habilitar Cloud Run y Cloud SQL.

## 2. Habilitar APIs necesarias

Ejecuta:

```bash
gcloud services enable run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com
```

## 3. Crear instancia Cloud SQL PostgreSQL 15

1. Crea una instancia PostgreSQL 15 en Cloud SQL.
2. Crea la base de datos `hotel_lunara`.
3. Crea un usuario dedicado para la aplicacion.
4. Anota host privado o publico, nombre de base, usuario y password.

## 4. Crear secretos en Secret Manager

Crea estos secretos:

- `DB_HOST`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `OPENAI_API_KEY`
- `CORS_ALLOWED_ORIGINS`

Ejemplo:

```bash
echo -n "hotel_lunara" | gcloud secrets create DB_NAME --data-file=-
echo -n "postgres" | gcloud secrets create DB_USER --data-file=-
```

Para actualizar un secreto existente:

```bash
echo -n "nuevo-valor" | gcloud secrets versions add DB_NAME --data-file=-
```

## 5. Configurar Cloud Build

1. Sube este repositorio a GitHub o Cloud Source Repositories.
2. Crea un trigger de Cloud Build apuntando al branch principal.
3. Usa el archivo `cloudbuild.yaml` del repositorio.
4. Asegura permisos para la cuenta de servicio de Cloud Build sobre:
   - Cloud Run Admin
   - Service Account User
   - Secret Manager Secret Accessor
   - Storage Admin o Artifact Registry Writer

## 6. Deploy manual como alternativa

### Build y push manual

```bash
gcloud builds submit --tag gcr.io/$PROJECT_ID/hotel-lunara-backend
```

### Deploy manual a Cloud Run

```bash
gcloud run deploy hotel-lunara-backend \
  --image gcr.io/$PROJECT_ID/hotel-lunara-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,DB_PORT=5432,OPENAI_MODEL=gpt-4o-mini,SERVER_PORT=8080 \
  --set-secrets DB_HOST=DB_HOST:latest,DB_NAME=DB_NAME:latest,DB_USER=DB_USER:latest,DB_PASSWORD=DB_PASSWORD:latest,JWT_SECRET=JWT_SECRET:latest,OPENAI_API_KEY=OPENAI_API_KEY:latest,CORS_ALLOWED_ORIGINS=CORS_ALLOWED_ORIGINS:latest,JWT_EXPIRATION=JWT_EXPIRATION:latest,JWT_REFRESH_EXPIRATION=JWT_REFRESH_EXPIRATION:latest
```

## Notas operativas

- En producción la aplicación usa `ddl-auto=validate`, por lo que la base debe existir y estar alineada con el esquema esperado.
- `OPENAI_API_KEY` es obligatoria para el concierge IA.
- Si Cloud SQL usa IP privada, la conectividad con Cloud Run debe resolverse con VPC Connector o configuración equivalente.
