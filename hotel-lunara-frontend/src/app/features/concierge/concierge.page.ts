import { NgClass } from '@angular/common';
import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, OnInit, ViewChild, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import {
  ConciergeActionSuggested,
  ConversationMessage,
  Reservation,
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { ConciergeApiService } from '../../core/services/concierge-api.service';
import { ReservationApiService } from '../../core/services/reservation-api.service';
import { formatDate, formatTime } from '../../core/utils/media.utils';
import { LoadingSkeletonComponent } from '../../shared/components/loading-skeleton/loading-skeleton.component';

interface ChatMessageView {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  timestamp: string;
  actionSuggested?: ConciergeActionSuggested;
}

const ANON_HISTORY_KEY = 'hotel-lunara-concierge-anon-history';
const ANON_SESSION_KEY = 'hotel-lunara-concierge-anon-session';

@Component({
  selector: 'app-concierge-page',
  standalone: true,
  imports: [ReactiveFormsModule, NgClass, LoadingSkeletonComponent],
  templateUrl: './concierge.page.html',
  styleUrl: './concierge.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConciergePageComponent implements OnInit, AfterViewInit {
  @ViewChild('viewport') private readonly viewport?: ElementRef<HTMLDivElement>;

  private readonly conciergeApi = inject(ConciergeApiService);
  private readonly reservationApi = inject(ReservationApiService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly authService = inject(AuthService);
  readonly loading = signal(true);
  readonly sending = signal(false);
  readonly sidebarOpen = signal(false);
  readonly messages = signal<ChatMessageView[]>([]);
  readonly reservations = signal<Reservation[]>([]);
  readonly sessionToken = signal(localStorage.getItem(ANON_SESSION_KEY) ?? '');

  readonly form = this.fb.nonNullable.group({
    message: ['', [Validators.required, Validators.maxLength(1200)]],
  });

  readonly currentStay = computed(() =>
    this.reservations().find((reservation) => reservation.estado === 'ACTIVA') ??
    this.reservations().find((reservation) => reservation.estado === 'CONFIRMADA') ??
    null,
  );
  readonly historyPreview = computed(() =>
    this.messages()
      .filter((message) => message.role === 'ASSISTANT')
      .slice(-8)
      .reverse(),
  );
  readonly formatDate = formatDate;
  readonly formatTime = formatTime;

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      forkJoin({
        history: this.conciergeApi.getHistory().pipe(catchError(() => of([]))),
        reservations: this.reservationApi.getMyReservations().pipe(catchError(() => of([]))),
      })
        .pipe(finalize(() => this.loading.set(false)))
        .subscribe(({ history, reservations }) => {
          this.messages.set(this.sortMessagesByTimestamp(history.map((message) => this.mapConversationMessage(message))));
          this.reservations.set(reservations);
          this.scrollToBottom();
        });
      return;
    }

    this.loading.set(false);
    this.restoreAnonymousHistory();
    this.scrollToBottom();
  }

  ngAfterViewInit(): void {
    this.scrollToBottom();
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((value) => !value);
  }

  sendMessage(): void {
    const message = this.form.controls.message.value.trim();
    if (!message) {
      return;
    }

    const userMessage: ChatMessageView = {
      id: crypto.randomUUID(),
      role: 'USER',
      content: message,
      timestamp: new Date().toISOString(),
    };

    this.messages.update((messages) => [...messages, userMessage]);
    this.persistAnonymousHistory();
    this.form.reset({ message: '' });
    this.sending.set(true);
    this.scrollToBottom();

    this.conciergeApi
      .chat({
        mensaje: message,
        sessionToken: this.sessionToken() || undefined,
      })
      .pipe(finalize(() => this.sending.set(false)))
      .subscribe({
        next: (response) => {
          if (response.sessionToken) {
            this.sessionToken.set(response.sessionToken);
            localStorage.setItem(ANON_SESSION_KEY, response.sessionToken);
          }

          const assistantMessage: ChatMessageView = {
            id: crypto.randomUUID(),
            role: 'ASSISTANT',
            content: response.respuesta,
            timestamp: new Date().toISOString(),
            actionSuggested: response.actionSuggested,
          };

          this.messages.update((messages) => [...messages, assistantMessage]);
          this.persistAnonymousHistory();
          this.scrollToBottom();
        },
        error: () => {
          const assistantMessage: ChatMessageView = {
            id: crypto.randomUUID(),
            role: 'ASSISTANT',
            content: 'No pude responder en este momento. Intenta de nuevo en unos segundos.',
            timestamp: new Date().toISOString(),
          };

          this.messages.update((messages) => [...messages, assistantMessage]);
          this.persistAnonymousHistory();
          this.scrollToBottom();
        },
      });
  }

  actionLabel(action: ConciergeActionSuggested): string {
    switch (action) {
      case 'BOOK_RESTAURANT':
        return 'Reservar mesa';
      case 'REQUEST_SERVICE':
        return 'Solicitar servicio';
      case 'NONE':
      default:
        return 'Abrir cuenta';
    }
  }

  handleAction(action: ConciergeActionSuggested): void {
    if (action === 'BOOK_RESTAURANT') {
      void this.router.navigateByUrl(this.authService.isLoggedIn() ? '/mi-cuenta/restaurante' : '/auth/login');
      return;
    }

    if (action === 'REQUEST_SERVICE') {
      void this.router.navigateByUrl(this.authService.isLoggedIn() ? '/mi-cuenta/servicios' : '/auth/login');
      return;
    }

    void this.router.navigateByUrl(this.authService.isLoggedIn() ? '/mi-cuenta' : '/auth/login');
  }

  private mapConversationMessage(message: ConversationMessage): ChatMessageView {
    return {
      id: message.id,
      role: message.role,
      content: message.contenido,
      timestamp: message.timestamp,
    };
  }

  private restoreAnonymousHistory(): void {
    try {
      const rawHistory = localStorage.getItem(ANON_HISTORY_KEY);
      if (rawHistory) {
        this.messages.set(this.sortMessagesByTimestamp(JSON.parse(rawHistory) as ChatMessageView[]));
      }
    } catch {
      localStorage.removeItem(ANON_HISTORY_KEY);
    }
  }

  private persistAnonymousHistory(): void {
    if (!this.authService.isLoggedIn()) {
      localStorage.setItem(ANON_HISTORY_KEY, JSON.stringify(this.messages()));
    }
  }

  private sortMessagesByTimestamp(messages: ChatMessageView[]): ChatMessageView[] {
    return [...messages].sort((left, right) => left.timestamp.localeCompare(right.timestamp));
  }

  private scrollToBottom(): void {
    queueMicrotask(() => {
      const viewport = this.viewport?.nativeElement;
      if (viewport) {
        viewport.scrollTop = viewport.scrollHeight;
      }
    });
  }
}
