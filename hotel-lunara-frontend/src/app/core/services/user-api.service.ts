import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse, UpdateProfilePayload, UserProfile } from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getMyProfile(): Observable<UserProfile> {
    return this.http
      .get<ApiResponse<UserProfile>>(`${this.env.apiUrl}/api/users/me`)
      .pipe(map((response) => response.data));
  }

  updateMyProfile(payload: UpdateProfilePayload): Observable<UserProfile> {
    return this.http
      .patch<ApiResponse<UserProfile>>(`${this.env.apiUrl}/api/users/me`, payload)
      .pipe(map((response) => response.data));
  }
}
