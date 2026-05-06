import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: number;
  type: ToastType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly nextId = signal(0);
  readonly toasts = signal<ToastMessage[]>([]);

  show(message: string, type: ToastType = 'info', timeout = 4500): void {
    const id = this.nextId() + 1;
    this.nextId.set(id);
    this.toasts.update((toasts) => [...toasts, { id, type, message }]);

    window.setTimeout(() => this.dismiss(id), timeout);
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  warning(message: string): void {
    this.show(message, 'warning');
  }

  dismiss(id: number): void {
    this.toasts.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }
}
