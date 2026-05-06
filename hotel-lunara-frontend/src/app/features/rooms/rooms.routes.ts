import { Routes } from '@angular/router';
import { RoomDetailPageComponent } from './room-detail.page';
import { RoomsPageComponent } from './rooms.page';

export const ROOMS_ROUTES: Routes = [
  {
    path: '',
    component: RoomsPageComponent,
  },
  {
    path: ':id',
    component: RoomDetailPageComponent,
  },
];
