import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AssetAccount, AssetAccountRequest } from '../models/asset-account.models';

@Injectable({ providedIn: 'root' })
export class AssetAccountsApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/asset-accounts`;

  list(includeInactive = false): Observable<AssetAccount[]> {
    const params = new HttpParams().set('includeInactive', includeInactive);
    return this.http.get<AssetAccount[]>(this.apiUrl, { params });
  }

  create(request: AssetAccountRequest): Observable<AssetAccount> {
    return this.http.post<AssetAccount>(this.apiUrl, request);
  }

  update(id: string, request: AssetAccountRequest): Observable<AssetAccount> {
    return this.http.put<AssetAccount>(`${this.apiUrl}/${id}`, request);
  }

  deactivate(id: string): Observable<AssetAccount> {
    return this.http.patch<AssetAccount>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  activate(id: string): Observable<AssetAccount> {
    return this.http.patch<AssetAccount>(`${this.apiUrl}/${id}/activate`, {});
  }
}
