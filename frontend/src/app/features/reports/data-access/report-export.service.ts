import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, finalize, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type ReportName = 'financial' | 'categories' | 'audit';
export type ExportFormat = 'PDF' | 'CSV';
export type ExportState = 'idle' | 'exporting' | 'success' | 'error';

@Injectable({ providedIn: 'root' })
export class ReportExportService {
  private readonly http = inject(HttpClient);
  private readonly states = new Map<string, ReturnType<typeof signal<ExportState>>>();
  private state(name: ReportName, format: ExportFormat) { const key = `${name}:${format}`; if (!this.states.has(key)) this.states.set(key, signal<ExportState>('idle')); return this.states.get(key)!; }
  exportState(name: ReportName, format: ExportFormat): ReturnType<typeof signal<ExportState>> { return this.state(name, format); }
  export(name: ReportName, format: ExportFormat, params: HttpParams): Observable<Blob> {
    const current = this.state(name, format); if (current() === 'exporting') return throwError(() => new Error('Exportação já está em andamento.'));
    current.set('exporting');
    return this.http.get(`${environment.apiUrl}/reports/${name}/export`, { params: params.set('format', format), responseType: 'blob' }).pipe(
      catchError(error => this.readBlobError(error)),
      finalize(() => { if (current() === 'exporting') current.set('error'); }),
    );
  }
  markSuccess(name: ReportName, format: ExportFormat): void { this.state(name, format).set('success'); }
  reset(name: ReportName, format: ExportFormat): void { this.state(name, format).set('idle'); }
  private readBlobError(error: { error?: Blob }) { if (!(error.error instanceof Blob)) return throwError(() => new Error('Não foi possível exportar o relatório.')); return new Observable<Blob>(subscriber => { error.error!.text().then(text => { let detail = 'Não foi possível exportar o relatório.'; try { detail = JSON.parse(text).detail ?? detail; } catch { /* texto simples */ } subscriber.error(new Error(detail)); }); }); }
  download(blob: Blob, filename: string): void { const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = filename; anchor.click(); window.setTimeout(() => URL.revokeObjectURL(url), 0); }
}
