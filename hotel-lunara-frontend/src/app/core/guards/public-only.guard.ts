import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { getRoleDashboardRoute } from '../utils/media.utils';

export const publicOnlyGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree([getRoleDashboardRoute(authService.currentRole() ?? undefined)]);
};
