import { Routes } from '@angular/router';
import { LoginPageComponent } from './login.page';
import { RegisterPageComponent } from './register.page';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    component: LoginPageComponent,
  },
  {
    path: 'registro',
    component: RegisterPageComponent,
  },
];
