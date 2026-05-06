declare global {
  interface Window {
    __API_URL__?: string;
  }
}

const runtimeApiUrl = typeof window !== 'undefined' ? window.__API_URL__ : undefined;

export const environment = {
  production: true,
  // If empty, the app uses same-origin `/api` and nginx proxies it to the backend.
    apiUrl: runtimeApiUrl ?? 'https://hotel-lunara-backend-476475787309.us-central1.run.app',
};
