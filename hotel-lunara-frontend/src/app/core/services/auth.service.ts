import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { map, tap } from 'rxjs';
import {
  ApiResponse,
  AuthResponse,
  AuthSession,
  JwtClaims,
  LoginPayload,
  RegisterPayload,
  UserProfile,
  UserRole,
} from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';
import { getRoleDashboardRoute } from '../utils/media.utils';

const LOCAL_STORAGE_KEY = 'hotel-lunara-session';
const SESSION_STORAGE_KEY = 'hotel-lunara-session-temporary';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly env = inject(ENVIRONMENT);

  private readonly sessionSignal = signal<AuthSession | null>(this.restoreSession());

  readonly session = computed(() => this.sessionSignal());
  readonly currentUser = computed<UserProfile | null>(() => this.sessionSignal()?.user ?? null);
  readonly token = computed(() => this.sessionSignal()?.accessToken ?? null);
  readonly refreshTokenValue = computed(() => this.sessionSignal()?.refreshToken ?? null);
  readonly currentRole = computed<UserRole | null>(() => this.sessionSignal()?.claims.role ?? null);
  readonly isLoggedIn = computed(() => {
    const session = this.sessionSignal();
    return !!session && session.claims.exp * 1000 > Date.now();
  });

  login(payload: LoginPayload, rememberSession = true) {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.env.apiUrl}/api/auth/login`, payload)
      .pipe(
        map((response) => response.data),
        tap((response) => this.persistSession(response, rememberSession ? 'local' : 'session')),
      );
  }

  register(payload: RegisterPayload) {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.env.apiUrl}/api/auth/register`, payload)
      .pipe(
        map((response) => response.data),
        tap((response) => this.persistSession(response, 'local')),
      );
  }

  refreshToken() {
    const refreshToken = this.refreshTokenValue();
    if (!refreshToken) {
      return null;
    }

    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.env.apiUrl}/api/auth/refresh`, {
        refreshToken,
      })
      .pipe(
        map((response) => response.data),
        tap((response) => this.persistSession(response, this.sessionSignal()?.storage ?? 'local')),
      );
  }

  logout(redirectTo = '/home'): void {
    localStorage.removeItem(LOCAL_STORAGE_KEY);
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
    this.sessionSignal.set(null);
    void this.router.navigateByUrl(redirectTo);
  }

  hasRole(role: UserRole): boolean {
    return this.currentRole() === role;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const currentRole = this.currentRole();
    return !!currentRole && roles.includes(currentRole);
  }

  redirectAfterLogin(returnUrl?: string | null): void {
    if (returnUrl) {
      void this.router.navigateByUrl(returnUrl);
      return;
    }

    void this.router.navigateByUrl(getRoleDashboardRoute(this.currentRole() ?? undefined));
  }

  patchCurrentUser(user: UserProfile): void {
    const currentSession = this.sessionSignal();
    if (!currentSession) {
      return;
    }

    const nextSession: AuthSession = {
      ...currentSession,
      user,
    };

    this.sessionSignal.set(nextSession);
    this.persistStoredSession(nextSession);
  }

  private persistSession(response: AuthResponse, storage: 'local' | 'session'): void {
    const claims = this.decodeJwt(response.accessToken);
    const session: AuthSession = {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      claims,
      user: response.user,
      storage,
    };

    this.sessionSignal.set(session);
    this.persistStoredSession(session);
  }

  private persistStoredSession(session: AuthSession): void {
    localStorage.removeItem(LOCAL_STORAGE_KEY);
    sessionStorage.removeItem(SESSION_STORAGE_KEY);

    if (session.storage === 'local') {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(session));
      return;
    }

    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
  }

  private restoreSession(): AuthSession | null {
    try {
      const candidates: Array<{ raw: string | null; storage: 'local' | 'session' }> = [
        { raw: localStorage.getItem(LOCAL_STORAGE_KEY), storage: 'local' },
        { raw: sessionStorage.getItem(SESSION_STORAGE_KEY), storage: 'session' },
      ];

      for (const candidate of candidates) {
        if (!candidate.raw) {
          continue;
        }

        const parsed = JSON.parse(candidate.raw) as AuthSession;
        if (!parsed.accessToken || !parsed.refreshToken) {
          this.clearStorage(candidate.storage);
          continue;
        }

        const normalizedSession: AuthSession = {
          ...parsed,
          storage: parsed.storage ?? candidate.storage,
        };

        if (normalizedSession.claims.exp * 1000 <= Date.now()) {
          this.clearStorage(candidate.storage);
          continue;
        }

        return normalizedSession;
      }
    } catch {
      this.clearStorage('local');
      this.clearStorage('session');
    }

    return null;
  }

  private decodeJwt(token: string): JwtClaims {
    const payload = token.split('.')[1];
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decodedPayload) as JwtClaims;
  }

  private clearStorage(storage: 'local' | 'session'): void {
    if (storage === 'local') {
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      return;
    }

    sessionStorage.removeItem(SESSION_STORAGE_KEY);
  }
}
