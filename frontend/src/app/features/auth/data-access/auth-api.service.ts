import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RegisterRequest } from './register-request';
import { RegisterResponse } from './register-response';

/**
 * Serviço para operações de autenticação/registro contra a API backend.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Registra um novo usuário.
   *
   * @param request Dados de cadastro (email e senha)
   * @returns Observable com a resposta do cadastro
   */
  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/auth/register`, request);
  }
}