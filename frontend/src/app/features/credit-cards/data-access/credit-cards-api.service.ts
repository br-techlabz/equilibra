import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CreditCard, CreditCardRequest, CreditCardStatement, StatementPaymentRequest } from '../models/credit-card.models';

@Injectable({ providedIn: 'root' })
export class CreditCardsApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/credit-cards`;

  list(includeInactive = false): Observable<CreditCard[]> {
    const params = new HttpParams().set('includeInactive', includeInactive);
    return this.http.get<CreditCard[]>(this.apiUrl, { params, headers: { 'Cache-Control': 'no-cache' } });
  }

  create(request: CreditCardRequest): Observable<CreditCard> {
    return this.http.post<CreditCard>(this.apiUrl, request);
  }

  update(id: string, request: CreditCardRequest): Observable<CreditCard> {
    return this.http.put<CreditCard>(`${this.apiUrl}/${id}`, request);
  }

  deactivate(id: string): Observable<CreditCard> {
    return this.http.patch<CreditCard>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  activate(id: string): Observable<CreditCard> {
    return this.http.patch<CreditCard>(`${this.apiUrl}/${id}/activate`, {});
  }

  currentStatement(cardId: string): Observable<CreditCardStatement> { return this.http.get<CreditCardStatement>(`${this.apiUrl}/${cardId}/current-statement`, { params: { refresh: Date.now() }, headers: { 'Cache-Control': 'no-cache' } }); }
  statements(cardId: string): Observable<CreditCardStatement[]> { return this.http.get<CreditCardStatement[]>(`${this.apiUrl}/${cardId}/statements`); }
  pay(statementId: string, request: StatementPaymentRequest, idempotencyKey: string): Observable<unknown> { return this.http.post(`${environment.apiUrl}/credit-card-statements/${statementId}/payments`, request, { headers: { 'Idempotency-Key': idempotencyKey } }); }
  payCurrent(cardId: string, request: StatementPaymentRequest, idempotencyKey: string): Observable<unknown> { return this.http.post(`${environment.apiUrl}/credit-card-statements/current/${cardId}/payments`, request, { headers: { 'Idempotency-Key': idempotencyKey } }); }
}
