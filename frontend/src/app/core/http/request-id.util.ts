/**
 * Gera um UUID v4 para correlação (X-Request-ID, chaves de idempotência).
 *
 * `crypto.randomUUID()` só existe em secure contexts (HTTPS, localhost).
 * Em HTTP via IP/hostname (ex: acesso à VM em produção local), a API não
 * existe e a chamada lançaria TypeError antes da requisição ser enviada.
 * Por isso há fallbacks: `crypto.getRandomValues` e, em último caso,
 * `Math.random` (suficiente para correlação, não para uso criptográfico).
 */
export function newRequestId(): string {
  const cryptoRef =
    typeof globalThis !== 'undefined'
      ? (globalThis as { crypto?: Crypto }).crypto
      : undefined;

  if (cryptoRef?.randomUUID) {
    return cryptoRef.randomUUID();
  }

  if (cryptoRef?.getRandomValues) {
    const bytes = new Uint8Array(16);
    cryptoRef.getRandomValues(bytes);
    // UUID v4: version (4) e variant (10xx) nos bits correspondentes
    bytes[6] = (bytes[6] & 0x0f) | 0x40;
    bytes[8] = (bytes[8] & 0x3f) | 0x80;
    const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('');
    return (
      `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-` +
      `${hex.slice(16, 20)}-${hex.slice(20)}`
    );
  }

  // Fallback final: Math.random (apenas correlação, sem garantias criptográficas)
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (char) => {
    const random = Math.floor(Math.random() * 16);
    const value = char === 'x' ? random : (random & 0x3) | 0x8;
    return value.toString(16);
  });
}
