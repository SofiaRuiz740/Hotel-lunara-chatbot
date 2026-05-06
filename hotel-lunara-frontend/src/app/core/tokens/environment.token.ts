import { InjectionToken } from '@angular/core';
import { environment } from '../../../environments/environment';

export type AppEnvironment = typeof environment;

export const ENVIRONMENT = new InjectionToken<AppEnvironment>('ENVIRONMENT', {
  factory: () => environment,
});
