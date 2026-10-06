import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Expense, ExpensePage, ExpenseRequest } from '../models/expense.models';

@Injectable({ providedIn: 'root' })
export class ExpensesApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/expenses`;

  list(page = 0, size = 20, includeCancelled = false): Observable<ExpensePage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('includeCancelled', includeCancelled);
    return this.http.get<ExpensePage>(this.apiUrl, { params });
  }

  create(request: ExpenseRequest): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, request);
  }

  get(id: string): Observable<Expense> {
    return this.http.get<Expense>(`${this.apiUrl}/${id}`);
  }

  update(id: string, request: ExpenseRequest): Observable<Expense> {
    return this.http.put<Expense>(`${this.apiUrl}/${id}`, request);
  }

  cancel(id: string): Observable<Expense> {
    return this.http.patch<Expense>(`${this.apiUrl}/${id}/cancel`, {});
  }
}
