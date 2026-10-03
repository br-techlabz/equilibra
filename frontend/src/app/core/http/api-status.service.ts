import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * Estados possíveis da API
 */
export type ApiStatus = 'loading' | 'available' | 'unavailable';

/**
 * Resposta de health do Actuator (simplificada)
 */
export interface HealthResponse {
  status: string;
  groups?: string[];
}

/**
 * Serviço para verificar status de disponibilidade da API backend.
 *
 * Responsável apenas por verificar disponibilidade técnica da API.
 * Não carrega dados de negócio nem implementa lógica de autenticação.
 */
@Injectable({
  providedIn: 'root',
})
export class ApiStatusService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Verifica se a API está disponível consultando o endpoint de liveness do Actuator.
   *
   * @returns Observable com o status da API
   */
  checkAvailability(): Observable<ApiStatus> {
    return this.http
      .get<HealthResponse>(`${this.apiUrl}/actuator/health/liveness`, {
        // Timeout de 5 segundos para health check
        // (Angular HttpClient não tem timeout nativo, seria necessário interceptor se necessário)
      }).pipe(
        map((response) => {
          // Actuator health liveness retorna { status: "UP", groups: ["liveness"] }
          if (response.status === 'UP') {
            return 'available';
          }
          return 'unavailable';
        }),
        catchError(() => {
          // Erro de rede, timeout, 5xx, etc. -> API indisponível
          return ['unavailable' as const];
        })
      );
  }
}