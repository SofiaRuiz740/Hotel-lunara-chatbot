import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  ApiResponse,
  HotelServiceAdminPayload,
  HotelServiceItem,
  ServiceAvailability,
  ServiceRequest,
  ServiceRequestFilters,
  ServiceRequestPayload,
  ServiceRequestStatus,
} from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';
import { buildHttpParams } from '../utils/http-params.util';

@Injectable({ providedIn: 'root' })
export class HotelServiceApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  getServices(): Observable<HotelServiceItem[]> {
    return this.http
      .get<ApiResponse<HotelServiceItem[]>>(`${this.env.apiUrl}/api/services`)
      .pipe(map((response) => response.data));
  }

  getService(id: number): Observable<HotelServiceItem> {
    return this.http
      .get<ApiResponse<HotelServiceItem>>(`${this.env.apiUrl}/api/services/${id}`)
      .pipe(map((response) => response.data));
  }

  getServiceAvailability(id: number, fecha: string, hora: string): Observable<ServiceAvailability> {
    return this.http
      .get<ApiResponse<ServiceAvailability>>(`${this.env.apiUrl}/api/services/${id}/availability`, {
        params: buildHttpParams({ fecha, hora }),
      })
      .pipe(map((response) => response.data));
  }

  createService(payload: HotelServiceAdminPayload): Observable<HotelServiceItem> {
    return this.http
      .post<ApiResponse<HotelServiceItem>>(`${this.env.apiUrl}/api/services`, payload)
      .pipe(map((response) => response.data));
  }

  updateService(id: number, payload: HotelServiceAdminPayload): Observable<HotelServiceItem> {
    return this.http
      .put<ApiResponse<HotelServiceItem>>(`${this.env.apiUrl}/api/services/${id}`, payload)
      .pipe(map((response) => response.data));
  }

  changeServiceStatus(id: number, active: boolean): Observable<HotelServiceItem> {
    return this.http
      .patch<ApiResponse<HotelServiceItem>>(`${this.env.apiUrl}/api/services/${id}/status`, null, {
        params: buildHttpParams({ active }),
      })
      .pipe(map((response) => response.data));
  }

  requestService(payload: ServiceRequestPayload): Observable<ServiceRequest> {
    return this.http
      .post<ApiResponse<ServiceRequest>>(`${this.env.apiUrl}/api/service-requests`, payload)
      .pipe(map((response) => response.data));
  }

  getMyRequests(): Observable<ServiceRequest[]> {
    return this.http
      .get<ApiResponse<ServiceRequest[]>>(`${this.env.apiUrl}/api/service-requests/me`)
      .pipe(map((response) => response.data));
  }

  getAllRequests(filters: ServiceRequestFilters = {}): Observable<ServiceRequest[]> {
    return this.http
      .get<ApiResponse<ServiceRequest[]>>(`${this.env.apiUrl}/api/service-requests`, {
        params: buildHttpParams(filters),
      })
      .pipe(map((response) => response.data));
  }

  updateRequestStatus(id: number, estado: ServiceRequestStatus): Observable<ServiceRequest> {
    return this.http
      .patch<ApiResponse<ServiceRequest>>(`${this.env.apiUrl}/api/service-requests/${id}/status`, {
        estado,
      })
      .pipe(map((response) => response.data));
  }
}
