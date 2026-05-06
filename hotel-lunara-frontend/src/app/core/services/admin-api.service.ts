import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  ApiResponse,
  AuditLog,
  DashboardSummary,
  RegisterPayload,
  UserProfile,
  UserRole,
} from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getDashboard(): Observable<DashboardSummary> {
    return this.http
      .get<ApiResponse<DashboardSummary>>(`${this.env.apiUrl}/api/admin/dashboard`)
      .pipe(map((response) => response.data));
  }

  getAuditLogs(): Observable<AuditLog[]> {
    return this.http
      .get<ApiResponse<AuditLog[]>>(`${this.env.apiUrl}/api/admin/audit-logs`)
      .pipe(map((response) => response.data));
  }

  getUsers(): Observable<UserProfile[]> {
    return this.http
      .get<ApiResponse<UserProfile[]>>(`${this.env.apiUrl}/api/admin/users`)
      .pipe(map((response) => response.data));
  }

  createReceptionist(payload: RegisterPayload): Observable<UserProfile> {
    return this.http
      .post<ApiResponse<UserProfile>>(`${this.env.apiUrl}/api/admin/receptionists`, payload)
      .pipe(map((response) => response.data));
  }

  changeUserRole(userId: string, role: UserRole): Observable<UserProfile> {
    return this.http
      .patch<ApiResponse<UserProfile>>(`${this.env.apiUrl}/api/admin/users/${userId}/role`, { role })
      .pipe(map((response) => response.data));
  }

  changeUserStatus(userId: string, activo: boolean): Observable<UserProfile> {
    return this.http
      .patch<ApiResponse<UserProfile>>(`${this.env.apiUrl}/api/admin/users/${userId}/status`, {
        activo,
      })
      .pipe(map((response) => response.data));
  }
}
