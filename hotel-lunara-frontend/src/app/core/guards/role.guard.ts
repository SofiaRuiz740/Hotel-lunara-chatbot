import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/api.models';
import { getRoleDashboardRoute } from '../utils/media.utils';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = (route.data?.['roles'] as UserRole[] | undefined) ?? [];

  if (authService.isLoggedIn() && authService.hasAnyRole(allowedRoles)) {
    return true;
  }

  return router.createUrlTree([getRoleDashboardRoute(authService.currentRole() ?? undefined)]);
};
