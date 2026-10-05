import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthApiService } from './auth-api.service';
import { RegisterRequest } from './register-request';
import { RegisterResponse } from './register-response';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthApiService],
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should POST to /api/auth/register with correct body', () => {
    const request: RegisterRequest = {
      email: 'user@example.com',
      password: 'senhaValida123',
    };

    const mockResponse: RegisterResponse = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      email: 'user@example.com',
      createdAt: '2026-10-04T12:00:00Z',
    };

    service.register(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    expect('confirmPassword' in req.request.body).toBeFalse();

    req.flush(mockResponse);
  });

  it('should not include confirmPassword in request body', () => {
    const request: RegisterRequest = {
      email: 'user@example.com',
      password: 'senhaValida123',
    };

    service.register(request).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.body).toEqual(request);
    expect(req.request.body).not.toEqual({ ...request, confirmPassword: 'any' });
    req.flush({ id: 'id', email: 'user@example.com', createdAt: '2026-10-04T12:00:00Z' });
  });
});

// Mock environment for tests
const environment = {
  apiUrl: '/api',
};