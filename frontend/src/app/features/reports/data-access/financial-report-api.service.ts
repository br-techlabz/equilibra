import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { FinancialReportFilters, FinancialReportResponse } from '../models/financial-report.models';

@Injectable({ providedIn: 'root' })
export class FinancialReportApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/reports/financial`;

  query(filters: FinancialReportFilters): Observable<FinancialReportResponse> {
    let params = new HttpParams()
      .set('from', filters.from)
      .set('to', filters.to)
      .set('page', filters.page)
      .set('size', filters.size);
    for (const accountId of [...new Set(filters.accountIds)]) {
      params = params.append('accountIds', accountId);
    }
    return this.http.get<FinancialReportResponse>(this.url, { params });
  }
}
