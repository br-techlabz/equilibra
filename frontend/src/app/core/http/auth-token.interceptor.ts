import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../features/auth/data-access/auth.service';

/**
 * Interceptor que adiciona Authorization: Bearer <token> automaticamente
 * para chamadas à API do Equilibra quando há token válido em memória.
 *
 * Regras:
 * - Adiciona Bearer apenas para URLs que começam com a base URL da API
 * - NÃO adiciona para endpoints públicos de autenticação (/auth/login, /auth/register)
 * - NÃO adiciona se não houver token
 * - NÃO adiciona para domínios externos (proteção contra vazamento)
 */
export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  // Sem token -> passa direto
  if (!token) {
    return next(req);
  }

  const apiBaseUrl = environment.apiUrl;
  const requestUrl = req.url.startsWith('http')
    ? req.url
    : `${location.origin}${req.url}`;

  // A configuração local usa `/api`, então URLs relativas já pertencem à API.
  // Para URLs absolutas, compara somente com a origem/base configuradas.
  const isApiRequest = apiBaseUrl.startsWith('/')
    ? req.url.startsWith(apiBaseUrl)
    : requestUrl.startsWith(apiBaseUrl);

  if (!isApiRequest) {
    // Domínio externo -> NUNCA envia token (proteção contra vazamento)
    return next(req);
  }

  // Endpoints públicos de autenticação não precisam de Authorization
  // (mesmo que token exista residualmente durante nova tentativa)
  if (isPublicAuthEndpoint(requestUrl, apiBaseUrl)) {
    return next(req);
  }

  // Adiciona Authorization header
  const clonedReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(clonedReq);
};

/**
 * Verifica se a URL é um endpoint público de autenticação
 * que não deve receber Authorization header.
 */
function isPublicAuthEndpoint(requestUrl: string, apiBaseUrl: string): boolean {
  const publicEndpoints = [
    `${apiBaseUrl}/auth/login`,
    `${apiBaseUrl}/auth/register`,
  ];

  // Health/actuator/docs também são públicos mas normalmente não passam por aqui
  // pois são chamados diretamente ou pelo ApiStatusService
  return publicEndpoints.some((endpoint) => requestUrl.startsWith(endpoint));
}