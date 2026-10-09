import { HttpParams } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReportExportService } from './report-export.service';

describe('ReportExportService', () => {
  let service: ReportExportService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [ReportExportService] }); service = TestBed.inject(ReportExportService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('tracks exporting and success states', () => { service.export('financial', 'CSV', new HttpParams()).subscribe(); expect(service.exportState('financial', 'CSV')()).toBe('exporting'); const request = http.expectOne(r => r.url.includes('/reports/financial/export')); request.flush(new Blob(['ok'], { type: 'text/csv' })); service.markSuccess('financial', 'CSV'); expect(service.exportState('financial', 'CSV')()).toBe('success'); });
  it('converts Problem Details blob to an error', (done) => { service.export('audit', 'PDF', new HttpParams()).subscribe({ error: error => { expect(error.message).toBe('Limite excedido'); done(); } }); const request = http.expectOne(r => r.url.includes('/reports/audit/export')); request.flush(new Blob([JSON.stringify({ detail: 'Limite excedido' })], { type: 'application/problem+json' }), { status: 413, statusText: 'Payload Too Large' }); });
});
