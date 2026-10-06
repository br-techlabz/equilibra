import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Income, IncomePage, IncomeRequest } from '../models/income.models';
@Injectable({providedIn:'root'}) export class IncomesApiService { private readonly http=inject(HttpClient); private readonly apiUrl=`${environment.apiUrl}/incomes`; list(page=0,size=20,includeCancelled=false):Observable<IncomePage>{const params=new HttpParams().set('page',page).set('size',size).set('includeCancelled',includeCancelled);return this.http.get<IncomePage>(this.apiUrl,{params});} create(r:IncomeRequest):Observable<Income>{return this.http.post<Income>(this.apiUrl,r);} update(id:string,r:IncomeRequest):Observable<Income>{return this.http.put<Income>(`${this.apiUrl}/${id}`,r);} cancel(id:string):Observable<Income>{return this.http.patch<Income>(`${this.apiUrl}/${id}/cancel`,{});} }
