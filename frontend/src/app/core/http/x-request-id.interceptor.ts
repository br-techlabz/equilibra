import { HttpInterceptorFn } from '@angular/common/http';
import { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Header para correlação de requisições
 */
export const REQUEST_ID_HEADER = 'X-Request-ID';

/**
 * Interceptor que garante X-Request-ID em todas as requisições HTTP.
 * - Se o header estiver presente na requisição original, mantém
 * - Se não estiver, gera um UUID v4
 * - Adiciona o header na requisição de saída
 */
export const xRequestIdInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  // Verifica se já existe o header X-Request-ID
  const existingRequestId = req.headers.get('X-Request-ID');

  let requestId = existingRequestId;

  // Se não existe ou é inválido, gera novo UUID
  if (!requestId || !isValidRequestId(requestId)) {
    requestId = crypto.randomUUID();
  }

  // Clona a requisição adicionando o header
  const clonedReq = req.clone({
    setHeaders: {
      'X-Request-ID': requestId,
    },
  });

  return next(clonedReq);
};

/**
 * Valida se um request ID tem formato UUID válido
 */
function isValidRequestId(candidate: string | null): boolean {
  if (!candidate || candidate.length !== 36) {
    return false;
  }

  // Regex UUID v4 simplificada
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(candidate);
}