declare global {
  interface Window {
    __API_URL__?: string;
  }
}

const runtimeApiUrl = typeof window !== 'undefined' ? window.__API_URL__ : undefined;

export const environment = {
  production: true,
  // Cloud Run sets this at runtime via `assets/runtime-config.js`.
  apiUrl: runtimeApiUrl ?? 'https://hotel-lunara-backend-xxxx.run.app',
};
