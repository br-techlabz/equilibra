import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthApiService } from './auth-api.service';
import { LoginRequest } from './login-request';
import { LoginResponse } from './login-response';
import { CurrentUser } from './current-user';

describe('AuthService', () => {
  let service: AuthService;
  let authApi: jasmine.SpyObj<AuthApiService>;
  let router: Router;

  const mockLoginResponse: LoginResponse = {
    accessToken: 'mock-token-123',
    tokenType: 'Bearer',
    expiresIn: 900,
  };

  const mockCurrentUser: CurrentUser = {
    id: '123e4567-e89b-12d3-a456-426614174000',
    email: 'user@example.com',
    createdAt: '2026-10-04T12:00:00Z',
  };

  beforeEach(async () => {
    const authApiSpy = jasmine.createSpyObj('AuthApiService', ['login', 'getCurrentUser']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: authApiSpy },
        AuthService,
      ],
    }).compileComponents();

    service = TestBed.inject(AuthService);
    authApi = TestBed.inject(AuthApiService) as jasmine.SpyObj<AuthApiService>;
    router = TestBed.inject(Router);
  });

  it('should start with unauthenticated status and no token/user', () => {
    expect(service.status()).toBe('unauthenticated');
    expect(service.accessToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should login successfully and navigate to dashboard', () => {
    authApi.login.and.returnValue(of(mockLoginResponse));
    authApi.getCurrentUser.and.returnValue(of(mockCurrentUser));

    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    const request: LoginRequest = { email: 'user@example.com', password: 'password123' };
    service.login(request);

    // With synchronous observables, the full flow completes synchronously
    expect(authApi.login).toHaveBeenCalledOnceWith(request);
    expect(authApi.getCurrentUser).toHaveBeenCalledOnceWith(mockLoginResponse.accessToken);

    // Final state after async operations complete
    expect(service.accessToken()).toBe(mockLoginResponse.accessToken);
    expect(service.currentUser()).toEqual(mockCurrentUser);
    expect(service.status()).toBe('authenticated');
    expect(service.isAuthenticated()).toBeTrue();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should handle 401 login error and stay unauthenticated', () => {
    authApi.login.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 401,
            error: { title: 'Authentication failed', detail: 'Invalid email or password.', status: 401 },
          }),
      ),
    );

    const request: LoginRequest = { email: 'user@example.com', password: 'wrong' };
    service.login(request);

    expect(service.status()).toBe('unauthenticated');
    expect(service.accessToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(service.authError()).toBe('E-mail ou senha inválidos.');
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should handle network error during login', () => {
    authApi.login.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 0,
            error: 'Network error',
          }),
      ),
    );

    service.login({ email: 'user@example.com', password: 'password123' });

    expect(service.authError()).toBe('Não foi possível conectar ao serviço. Tente novamente.');
    expect(service.status()).toBe('unauthenticated');
  });

  it('should handle 500 error during login', () => {
    authApi.login.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 500,
            error: { title: 'Internal server error', detail: 'Unexpected error', status: 500 },
          }),
      ),
    );

    service.login({ email: 'user@example.com', password: 'password123' });

    expect(service.authError()).toBe('Não foi possível autenticar. Tente novamente.');
  });

  it('should clear session when /me fails after successful login', () => {
    authApi.login.and.returnValue(of(mockLoginResponse));
    authApi.getCurrentUser.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 404,
            error: { title: 'Resource not found', detail: 'User not found', status: 404 },
          }),
      ),
    );

    service.login({ email: 'user@example.com', password: 'password123' });

    expect(service.accessToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(service.status()).toBe('unauthenticated');
    expect(service.authError()).toBe('Usuário não encontrado. Tente fazer login novamente.');
  });

  it('should clear session on clearSession()', () => {
    authApi.login.and.returnValue(of(mockLoginResponse));
    authApi.getCurrentUser.and.returnValue(of(mockCurrentUser));

    service.login({ email: 'user@example.com', password: 'password123' });

    expect(service.isAuthenticated()).toBeTrue();

    service.clearSession();

    expect(service.accessToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(service.status()).toBe('unauthenticated');
    expect(service.authError()).toBeNull();
  });
});