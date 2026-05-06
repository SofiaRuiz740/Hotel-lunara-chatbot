import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  ApiResponse,
  ConciergeChatResponse,
  ConciergeRequestPayload,
  ConversationMessage,
} from '../models/api.models';
import { ENVIRONMENT } from '../tokens/environment.token';

@Injectable({ providedIn: 'root' })
export class ConciergeApiService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  chat(payload: ConciergeRequestPayload): Observable<ConciergeChatResponse> {
    return this.http
      .post<ApiResponse<ConciergeChatResponse>>(`${this.env.apiUrl}/api/concierge/chat`, payload)
      .pipe(map((response) => response.data));
  }

  getHistory(): Observable<ConversationMessage[]> {
    return this.http
      .get<ApiResponse<ConversationMessage[]>>(`${this.env.apiUrl}/api/concierge/history`)
      .pipe(map((response) => response.data));
  }
}
