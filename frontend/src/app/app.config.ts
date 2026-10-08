import { ApplicationConfig, provideZoneChangeDetection, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { xRequestIdInterceptor } from './core/http/x-request-id.interceptor';
import { AuthService } from './features/auth/data-access/auth.service';
import { provideAppInitializer } from '@angular/core';
import { authTokenInterceptor } from './core/http/auth-token.interceptor';
import { authErrorInterceptor } from './core/http/auth-error.interceptor';
import { MAT_DATE_LOCALE } from '@angular/material/core';

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' },
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAppInitializer(() => inject(AuthService).initialize()),
    provideHttpClient(
      withInterceptors([
        // Ordem dos interceptors:
        // 1. xRequestIdInterceptor - deve ser o primeiro para garantir X-Request-ID em todas as requisições
        // 2. authTokenInterceptor - adiciona Authorization Bearer quando há token
        // 3. authErrorInterceptor - trata 401/403 centralizados
        xRequestIdInterceptor,
        authTokenInterceptor,
        authErrorInterceptor,
      ])
    ),
  ],
};