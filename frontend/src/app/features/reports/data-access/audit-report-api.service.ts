import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuditReportFilters, AuditReportResponse } from '../models/audit-report.models';
@Injectable({providedIn:'root'}) export class AuditReportApiService{private readonly http=inject(HttpClient);private readonly url=`${environment.apiUrl}/reports/audit`;query(f:AuditReportFilters):Observable<AuditReportResponse>{let p=new HttpParams().set('from',f.from).set('to',f.to).set('page',f.page).set('size',f.size);if(f.type!=='ALL')p=p.set('type',f.type);if(f.status!=='ALL')p=p.set('status',f.status);if(f.categoryId!=='ALL')p=p.set('categoryId',f.categoryId);for(const id of [...new Set(f.accountIds)])p=p.append('accountIds',id);for(const id of [...new Set(f.tagIds)])p=p.append('tagIds',id);return this.http.get<AuditReportResponse>(this.url,{params:p});}}
