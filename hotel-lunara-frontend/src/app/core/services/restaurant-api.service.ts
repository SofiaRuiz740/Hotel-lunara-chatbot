import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  ApiResponse,
  RestaurantAvailability,
  RestaurantReservation,
  RestaurantReservationFilters,
  RestaurantReservationPayload,
  RestaurantReservationStatus,
} from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';
import { buildHttpParams } from '../utils/http-params.util';

@Injectable({ providedIn: 'root' })
export class RestaurantApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getAvailability(fecha: string, hora: string, personas: number): Observable<RestaurantAvailability> {
    return this.http
      .get<ApiResponse<RestaurantAvailability>>(`${this.env.apiUrl}/api/restaurant/availability`, {
        params: buildHttpParams({ fecha, hora, personas }),
      })
      .pipe(map((response) => response.data));
  }

  createReservation(payload: RestaurantReservationPayload): Observable<RestaurantReservation> {
    return this.http
      .post<ApiResponse<RestaurantReservation>>(`${this.env.apiUrl}/api/restaurant/reservations`, payload)
      .pipe(map((response) => response.data));
  }

  getMyReservations(): Observable<RestaurantReservation[]> {
    return this.http
      .get<ApiResponse<RestaurantReservation[]>>(`${this.env.apiUrl}/api/restaurant/reservations/me`)
      .pipe(map((response) => response.data));
  }

  getAllReservations(filters: RestaurantReservationFilters = {}): Observable<RestaurantReservation[]> {
    return this.http
      .get<ApiResponse<RestaurantReservation[]>>(`${this.env.apiUrl}/api/restaurant/reservations`, {
        params: buildHttpParams(filters),
      })
      .pipe(map((response) => response.data));
  }

  cancelReservation(id: number): Observable<RestaurantReservation> {
    return this.http
      .patch<ApiResponse<RestaurantReservation>>(`${this.env.apiUrl}/api/restaurant/reservations/${id}/cancel`, {})
      .pipe(map((response) => response.data));
  }

  updateReservationStatus(
    id: number,
    estado: RestaurantReservationStatus,
  ): Observable<RestaurantReservation> {
    return this.http
      .patch<ApiResponse<RestaurantReservation>>(
        `${this.env.apiUrl}/api/restaurant/reservations/${id}/status`,
        { estado },
      )
      .pipe(map((response) => response.data));
  }
}
