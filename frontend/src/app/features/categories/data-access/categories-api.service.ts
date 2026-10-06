import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Category, CategoryApplicability, CategoryRequest } from '../models/category.models';

@Injectable({ providedIn: 'root' })
export class CategoriesApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/categories`;

  list(includeInactive = false, applicability?: CategoryApplicability): Observable<Category[]> {
    let params = new HttpParams().set('includeInactive', includeInactive);
    if (applicability) params = params.set('applicability', applicability);
    return this.http.get<Category[]>(this.apiUrl, { params });
  }

  create(request: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, request);
  }

  update(id: string, request: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, request);
  }

  deactivate(id: string): Observable<Category> {
    return this.http.patch<Category>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  activate(id: string): Observable<Category> {
    return this.http.patch<Category>(`${this.apiUrl}/${id}/activate`, {});
  }
}
