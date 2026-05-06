import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 500) {
          toastService.error('Ocurrió un error interno. Intenta de nuevo en un momento.');
        } else {
          const serverMessage =
            (error.error?.message as string | undefined) ||
            (Array.isArray(error.error?.data) ? error.error.data.join(' · ') : undefined) ||
            error.message;
          toastService.error(serverMessage || 'No fue posible completar la solicitud.');
        }
      }

      return throwError(() => error);
    }),
  );
};
