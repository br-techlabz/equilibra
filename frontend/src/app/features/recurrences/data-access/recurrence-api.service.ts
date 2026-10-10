import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CreateRecurrenceRequest, Recurrence, UpdateRecurrenceRequest } from '../models/recurrence.models';

@Injectable({ providedIn: 'root' })
export class RecurrenceApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/recurrences`;

  list(): Observable<Recurrence[]> { return this.http.get<Recurrence[]>(this.apiUrl); }
  get(id: string): Observable<Recurrence> { return this.http.get<Recurrence>(`${this.apiUrl}/${id}`); }
  create(request: CreateRecurrenceRequest): Observable<Recurrence> { return this.http.post<Recurrence>(this.apiUrl, request); }
  update(id: string, request: UpdateRecurrenceRequest): Observable<Recurrence> { return this.http.put<Recurrence>(`${this.apiUrl}/${id}`, request); }
  pause(id: string): Observable<Recurrence> { return this.http.post<Recurrence>(`${this.apiUrl}/${id}/pause`, {}); }
  resume(id: string): Observable<Recurrence> { return this.http.post<Recurrence>(`${this.apiUrl}/${id}/resume`, {}); }
  end(id: string): Observable<Recurrence> { return this.http.post<Recurrence>(`${this.apiUrl}/${id}/end`, {}); }
  cancel(id: string): Observable<Recurrence> { return this.http.post<Recurrence>(`${this.apiUrl}/${id}/cancel`, {}); }
}
