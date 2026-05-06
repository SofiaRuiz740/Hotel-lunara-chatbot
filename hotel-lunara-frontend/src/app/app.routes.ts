import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { publicOnlyGuard } from './core/guards/public-only.guard';
import { roleGuard } from './core/guards/role.guard';
import { DashboardShellComponent } from './layout/dashboard-shell/dashboard-shell.component';
import { PublicShellComponent } from './layout/public-shell/public-shell.component';

export const routes: Routes = [
  {
    path: '',
    component: PublicShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      {
        path: 'home',
        loadChildren: () => import('./features/home/home.routes').then((module) => module.HOME_ROUTES),
      },
      {
        path: 'habitaciones',
        loadChildren: () =>
          import('./features/rooms/rooms.routes').then((module) => module.ROOMS_ROUTES),
      },
      {
        path: 'concierge',
        loadChildren: () =>
          import('./features/concierge/concierge.routes').then((module) => module.CONCIERGE_ROUTES),
      },
    ],
  },
  {
    path: 'auth',
    canActivate: [publicOnlyGuard],
    loadChildren: () => import('./features/auth/auth.routes').then((module) => module.AUTH_ROUTES),
  },
  {
    path: 'mi-cuenta',
    component: DashboardShellComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['GUEST'] },
    loadChildren: () => import('./features/guest/guest.routes').then((module) => module.GUEST_ROUTES),
  },
  {
    path: 'recepcion',
    component: DashboardShellComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['RECEPTIONIST', 'ADMIN'] },
    loadChildren: () =>
      import('./features/reception/reception.routes').then((module) => module.RECEPTION_ROUTES),
  },
  {
    path: 'admin',
    component: DashboardShellComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadChildren: () => import('./features/admin/admin.routes').then((module) => module.ADMIN_ROUTES),
  },
  {
    path: '**',
    redirectTo: 'home',
  },
];
