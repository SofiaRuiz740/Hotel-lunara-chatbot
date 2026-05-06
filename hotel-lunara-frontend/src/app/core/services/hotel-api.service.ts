import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse, HotelPublicInfo } from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class HotelApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getHotelInfo(): Observable<HotelPublicInfo> {
    return this.http
      .get<ApiResponse<HotelPublicInfo>>(`${this.env.apiUrl}/api/hotel/info`)
      .pipe(map((response) => response.data));
  }
}
