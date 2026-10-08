import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CategoryReportFilters, CategoryReportResponse } from '../models/category-report.models';

@Injectable({ providedIn: 'root' })
export class CategoryReportApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/reports/categories`;
  query(filters: CategoryReportFilters): Observable<CategoryReportResponse> {
    let params = new HttpParams().set('from', filters.from).set('to', filters.to);
    for (const accountId of [...new Set(filters.accountIds)]) params = params.append('accountIds', accountId);
    return this.http.get<CategoryReportResponse>(this.url, { params });
  }
}
