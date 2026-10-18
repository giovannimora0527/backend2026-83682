import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

/**
 * Configuración global de la aplicación: enrutamiento y cliente HTTP.
 * Todo el estado de las pantallas usa signals, por lo que funciona igual con o sin zone.js.
 */
export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient()],
};
