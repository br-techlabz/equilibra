import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, finalize, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type ReportName = 'financial' | 'categories' | 'audit';
export type ExportFormat = 'PDF' | 'CSV';

@Injectable({ providedIn: 'root' })
export class ReportExportService {
  private readonly http = inject(HttpClient);
  readonly exporting = new Set<string>();
  readonly lastError = new Map<string, string>();
  readonly lastSuccess = new Map<string, number>();
  isExporting(name: ReportName, format: ExportFormat): boolean { return this.exporting.has(`${name}:${format}`); }
  export(name: ReportName, format: ExportFormat, params: HttpParams): Observable<Blob> {
    const key = `${name}:${format}`;
    if (this.exporting.has(key)) return throwError(() => new Error('Exportação já está em andamento.'));
    this.exporting.add(key); this.lastError.delete(key); this.lastSuccess.delete(key);
    return this.http.get(`${environment.apiUrl}/reports/${name}/export`, { params: params.set('format', format), responseType: 'blob' }).pipe(
      catchError(error => this.readBlobError(error)),
      finalize(() => this.exporting.delete(key)),
    );
  }
  private readBlobError(error: { error?: Blob; status?: number }) {
    if (!(error.error instanceof Blob)) return throwError(() => new Error('Não foi possível exportar o relatório.'));
    return new Observable<Blob>(subscriber => { error.error!.text().then(text => { let detail = 'Não foi possível exportar o relatório.'; try { detail = JSON.parse(text).detail ?? detail; } catch { /* resposta não JSON */ } subscriber.error(new Error(detail)); }); });
  }
  download(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob); const anchor = document.createElement('a');
    anchor.href = url; anchor.download = filename; anchor.click();
    window.setTimeout(() => URL.revokeObjectURL(url), 0);
  }
}
