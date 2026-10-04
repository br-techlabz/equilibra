import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { RegisterPageComponent } from './register.page';
import { AuthApiService } from '../../data-access/auth-api.service';
import { MatSnackBar } from '@angular/material/snack-bar';

class AuthApiServiceStub {
  register = jasmine.createSpy('register').and.returnValue(
    of({ id: 'id', email: 'user@example.com', createdAt: '2026-10-04T12:00:00Z' }),
  );
}

class MatSnackBarStub {
  open = jasmine.createSpy('open');
}

describe('RegisterPageComponent', () => {
  let fixture: ComponentFixture<RegisterPageComponent>;
  let component: RegisterPageComponent;
  let authApi: AuthApiServiceStub;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterPageComponent],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useClass: AuthApiServiceStub },
        { provide: MatSnackBar, useClass: MatSnackBarStub },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterPageComponent);
    component = fixture.componentInstance;
    authApi = TestBed.inject(AuthApiService) as unknown as AuthApiServiceStub;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should start with an invalid form', () => {
    expect(component.form.invalid).toBeTrue();
  });

  it('should accept a valid email and password', () => {
    component.form.setValue({
      email: 'user@example.com',
      password: 'senha válida',
      confirmPassword: 'senha válida',
    });

    expect(component.form.valid).toBeTrue();
  });

  it('should reject an invalid email', () => {
    component.form.controls.email.setValue('not-an-email');
    expect(component.form.controls.email.hasError('email')).toBeTrue();
  });

  it('should reject a password below minimum length', () => {
    component.form.controls.password.setValue('1234567');
    expect(component.form.controls.password.hasError('minlength')).toBeTrue();
  });

  it('should reject a different confirmation', () => {
    component.form.setValue({
      email: 'user@example.com',
      password: 'senha válida',
      confirmPassword: 'outra senha',
    });

    expect(component.form.hasError('passwordsMismatch')).toBeTrue();
  });

  it('should preserve spaces in password and not send confirmation', () => {
    const password = ' senha com espaço ';
    component.form.setValue({
      email: 'user@example.com',
      password,
      confirmPassword: password,
    });

    component.submit();

    expect(authApi.register).toHaveBeenCalledOnceWith({
      email: 'user@example.com',
      password,
    });
    const calledArgs = authApi.register.calls.mostRecent().args[0];
    expect(calledArgs).toEqual({ email: 'user@example.com', password });
    expect('confirmPassword' in calledArgs).toBeFalse();
  });

  it('should not submit an invalid form', () => {
    component.submit();
    expect(authApi.register).not.toHaveBeenCalled();
  });

  it('should handle duplicate email without technical details', () => {
    authApi.register.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { title: 'Resource conflict', detail: 'Email already exists', status: 409 },
          }),
      ),
    );

    component.form.setValue({
      email: 'user@example.com',
      password: 'senha válida',
      confirmPassword: 'senha válida',
    });
    component.submit();
    fixture.detectChanges();

    expect(component.serverError()).toBe('Já existe uma conta cadastrada com este e-mail.');
  });

  it('should navigate to login after successful registration', () => {
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.form.setValue({
      email: 'user@example.com',
      password: 'senha válida',
      confirmPassword: 'senha válida',
    });
    component.submit();

    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});