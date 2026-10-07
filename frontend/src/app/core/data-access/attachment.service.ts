import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TransactionAttachment { id:string; originalFileName:string; contentType:string; sizeBytes:number; createdAt:string; }
@Injectable({providedIn:'root'})
export class AttachmentService {
 private readonly http=inject(HttpClient); private readonly base=`${environment.apiUrl}`;
 list(transactionId:string):Observable<TransactionAttachment[]>{return this.http.get<TransactionAttachment[]>(`${this.base}/transactions/${transactionId}/attachments`);}
 upload(transactionId:string,file:File):Observable<HttpEvent<TransactionAttachment>>{const form=new FormData();form.append('file',file,file.name);return this.http.post<TransactionAttachment>(`${this.base}/transactions/${transactionId}/attachments`,form,{observe:'events',reportProgress:true});}
 download(id:string):Observable<Blob>{return this.http.get(`${this.base}/attachments/${id}/content`,{responseType:'blob'});}
 delete(id:string):Observable<void>{return this.http.delete<void>(`${this.base}/attachments/${id}`);}
}
