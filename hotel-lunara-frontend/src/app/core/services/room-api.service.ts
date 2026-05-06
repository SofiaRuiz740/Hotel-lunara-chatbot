import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse, Room, RoomAvailabilityFilters, RoomAdminPayload, RoomStatus } from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';
import { buildHttpParams } from '../utils/http-params.util';

@Injectable({ providedIn: 'root' })
export class RoomApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getRooms(): Observable<Room[]> {
    return this.http
      .get<ApiResponse<Room[]>>(`${this.env.apiUrl}/api/rooms`)
      .pipe(map((response) => response.data));
  }

  getRoomById(id: number): Observable<Room> {
    return this.http
      .get<ApiResponse<Room>>(`${this.env.apiUrl}/api/rooms/${id}`)
      .pipe(map((response) => response.data));
  }

  getAvailability(filters: RoomAvailabilityFilters): Observable<Room[]> {
    return this.http
      .get<ApiResponse<Room[]>>(`${this.env.apiUrl}/api/rooms/availability`, {
        params: buildHttpParams(filters),
      })
      .pipe(map((response) => response.data));
  }

  createRoom(payload: RoomAdminPayload): Observable<Room> {
    return this.http
      .post<ApiResponse<Room>>(`${this.env.apiUrl}/api/rooms`, payload)
      .pipe(map((response) => response.data));
  }

  updateRoom(id: number, payload: RoomAdminPayload): Observable<Room> {
    return this.http
      .put<ApiResponse<Room>>(`${this.env.apiUrl}/api/rooms/${id}`, payload)
      .pipe(map((response) => response.data));
  }

  changeRoomStatus(id: number, status: RoomStatus): Observable<Room> {
    return this.http
      .put<ApiResponse<Room>>(`${this.env.apiUrl}/api/rooms/${id}/status`, null, {
        params: buildHttpParams({ status }),
      })
      .pipe(map((response) => response.data));
  }

  deactivateRoom(id: number): Observable<Room> {
    return this.http
      .delete<ApiResponse<Room>>(`${this.env.apiUrl}/api/rooms/${id}`)
      .pipe(map((response) => response.data));
  }
}
