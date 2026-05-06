import { Routes } from '@angular/router';
import { GuestDashboardPageComponent } from './guest-dashboard.page';
import { GuestProfilePageComponent } from './guest-profile.page';
import { GuestReservationsPageComponent } from './guest-reservations.page';
import { GuestRestaurantPageComponent } from './guest-restaurant.page';
import { GuestServicesPageComponent } from './guest-services.page';

export const GUEST_ROUTES: Routes = [
  {
    path: '',
    component: GuestDashboardPageComponent,
  },
  {
    path: 'reservas',
    component: GuestReservationsPageComponent,
  },
  {
    path: 'restaurante',
    component: GuestRestaurantPageComponent,
  },
  {
    path: 'servicios',
    component: GuestServicesPageComponent,
  },
  {
    path: 'perfil',
    component: GuestProfilePageComponent,
  },
];
