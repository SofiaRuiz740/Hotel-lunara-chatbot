import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  ApiResponse,
  CancelReservationPayload,
  CheckoutSummaryResponse,
  Reservation,
  ReservationFilters,
  RoomReservationPayload,
  TodayOperationsResponse,
} from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';
import { buildHttpParams } from '../utils/http-params.util';

@Injectable({ providedIn: 'root' })
export class ReservationApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  createReservation(payload: RoomReservationPayload): Observable<Reservation> {
    return this.http
      .post<ApiResponse<Reservation>>(`${this.env.apiUrl}/api/reservations`, payload)
      .pipe(map((response) => response.data));
  }

  getMyReservations(): Observable<Reservation[]> {
    return this.http
      .get<ApiResponse<Reservation[]>>(`${this.env.apiUrl}/api/reservations/me`)
      .pipe(map((response) => response.data));
  }

  getAllReservations(filters: ReservationFilters = {}): Observable<Reservation[]> {
    return this.http
      .get<ApiResponse<Reservation[]>>(`${this.env.apiUrl}/api/reservations`, {
        params: buildHttpParams(filters),
      })
      .pipe(map((response) => response.data));
  }

  getReservation(id: number): Observable<Reservation> {
    return this.http
      .get<ApiResponse<Reservation>>(`${this.env.apiUrl}/api/reservations/${id}`)
      .pipe(map((response) => response.data));
  }

  cancelReservation(id: number, payload: CancelReservationPayload): Observable<Reservation> {
    return this.http
      .patch<ApiResponse<Reservation>>(`${this.env.apiUrl}/api/reservations/${id}/cancel`, payload)
      .pipe(map((response) => response.data));
  }

  checkin(id: number): Observable<Reservation> {
    return this.http
      .post<ApiResponse<Reservation>>(`${this.env.apiUrl}/api/reservations/${id}/checkin`, {})
      .pipe(map((response) => response.data));
  }

  checkout(id: number): Observable<CheckoutSummaryResponse> {
    return this.http
      .post<ApiResponse<CheckoutSummaryResponse>>(`${this.env.apiUrl}/api/reservations/${id}/checkout`, {})
      .pipe(map((response) => response.data));
  }

  getTodayOperations(): Observable<TodayOperationsResponse> {
    return this.http
      .get<ApiResponse<TodayOperationsResponse>>(`${this.env.apiUrl}/api/reservations/today-operations`)
      .pipe(map((response) => response.data));
  }
}
