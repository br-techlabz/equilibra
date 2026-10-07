import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Tag, TagRequest } from '../models/tag.models';

@Injectable({ providedIn: 'root' })
export class TagsApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/tags`;

  list(includeInactive = false): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.url, { params: new HttpParams().set('includeInactive', includeInactive) });
  }
  create(request: TagRequest): Observable<Tag> { return this.http.post<Tag>(this.url, request); }
  update(id: string, request: TagRequest): Observable<Tag> { return this.http.put<Tag>(`${this.url}/${id}`, request); }
  deactivate(id: string): Observable<Tag> { return this.http.patch<Tag>(`${this.url}/${id}/deactivate`, {}); }
  activate(id: string): Observable<Tag> { return this.http.patch<Tag>(`${this.url}/${id}/activate`, {}); }
}
