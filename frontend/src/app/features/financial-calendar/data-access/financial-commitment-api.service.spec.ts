import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { FinancialCommitmentApiService } from './financial-commitment-api.service';

describe('FinancialCommitmentApiService', () => {
  let service: FinancialCommitmentApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [FinancialCommitmentApiService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(FinancialCommitmentApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('builds the owner-scoped list query without ownerId', () => {
    service.list({ from: '2026-10-01', to: '2026-10-31', type: 'EXPENSE', status: 'PENDING', page: 0, size: 100 }).subscribe();
    const request = http.expectOne(r => r.url.endsWith('/commitments'));
    expect(request.request.params.get('from')).toBe('2026-10-01');
    expect(request.request.params.get('to')).toBe('2026-10-31');
    expect(request.request.params.get('type')).toBe('EXPENSE');
    expect(request.request.params.get('status')).toBe('PENDING');
    expect(request.request.params.has('ownerId')).toBeFalse();
    request.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 100 });
  });

  it('uses the real settle endpoint and payload', () => {
    service.settle('c1', { occurredAt: '2026-10-10T12:00:00Z', amount: 200 }).subscribe();
    const request = http.expectOne('/api/commitments/c1/settle');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ occurredAt: '2026-10-10T12:00:00Z', amount: 200 });
    request.flush({ id: 'c1', status: 'SETTLED' });
  });
});
