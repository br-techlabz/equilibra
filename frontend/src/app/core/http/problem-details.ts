/**
 * Interface para Problem Details conforme RFC 9457
 * Compatível com a implementação do backend (GlobalExceptionHandler)
 */
export interface ProblemDetails {
  /** URI que identifica o tipo de problema */
  type: string;
  /** Título curto, legível por humanos */
  title: string;
  /** Código de status HTTP */
  status: number;
  /** Explicação específica da ocorrência */
  detail: string;
  /** URI que identifica a ocorrência específica */
  instance: string;
  /** Timestamp da ocorrência (ISO 8601) */
  timestamp?: string;
  /** ID de correlação da requisição */
  requestId?: string;
  /** Erros de validação de campos (apenas para 400 validation-failed) */
  errors?: FieldValidationError[];
}

/**
 * Erro de validação de campo individual
 */
export interface FieldValidationError {
  /** Nome do campo */
  field: string;
  /** Mensagem de erro */
  message: string;
}

/**
 * Type guard para verificar se um objeto é ProblemDetails
 */
export function isProblemDetails(value: unknown): value is ProblemDetails {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ProblemDetails).type === 'string' &&
    typeof (value as ProblemDetails).title === 'string' &&
    typeof (value as ProblemDetails).status === 'number' &&
    typeof (value as ProblemDetails).detail === 'string' &&
    typeof (value as ProblemDetails).instance === 'string'
  );
}