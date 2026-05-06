import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ENVIRONMENT } from '../tokens/environment.token';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const env = inject(ENVIRONMENT);
  const token = authService.token();
  const isApiRequest = req.url.startsWith(env.apiUrl);

  const request = isApiRequest && token
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !req.url.includes('/api/auth/login') &&
        !req.url.includes('/api/auth/register') &&
        !req.url.includes('/api/auth/refresh')
      ) {
        authService.logout('/auth/login');
        void router.navigate(['/auth/login'], {
          queryParams: { returnUrl: router.url },
        });
      }

      return throwError(() => error);
    }),
  );
};
