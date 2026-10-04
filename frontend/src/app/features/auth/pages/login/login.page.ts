import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../data-access/auth.service';
import { PublicLayoutComponent } from '../../../../layout/public-layout/public-layout.component';

interface LoginForm {
  email: FormControl<string>;
  password: FormControl<string>;
}

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    PublicLayoutComponent,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <app-public-layout>
      <mat-card class="login-card" appearance="outlined">
        <mat-card-header class="card-header">
          <mat-card-title>Entre na sua conta</mat-card-title>
          <mat-card-subtitle>Continue sua jornada com o Equilibra.</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()" novalidate class="login-form">
            <mat-form-field appearance="outline" class="full-width" subscriptSizing="dynamic">
              <mat-label>E-mail</mat-label>
              <input
                matInput
                type="email"
                formControlName="email"
                autocomplete="email"
                aria-describedby="login-email-error"
              />
              <mat-error id="login-email-error" *ngIf="form.controls.email.hasError('required')">
                E-mail é obrigatório.
              </mat-error>
              <mat-error id="login-email-error" *ngIf="form.controls.email.hasError('email')">
                Formato de e-mail inválido.
              </mat-error>
              <mat-error id="login-email-error" *ngIf="form.controls.email.hasError('maxlength')">
                E-mail não pode exceder 255 caracteres.
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width" subscriptSizing="dynamic">
              <mat-label>Senha</mat-label>
              <input
                matInput
                [type]="showPassword() ? 'text' : 'password'"
                formControlName="password"
                autocomplete="current-password"
                aria-describedby="login-password-error"
              />
              <button
                type="button"
                matSuffix
                mat-icon-button
                (click)="togglePassword()"
                [attr.aria-label]="showPassword() ? 'Ocultar senha' : 'Mostrar senha'"
                [attr.aria-pressed]="showPassword()"
              >
                <mat-icon>{{ showPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error id="login-password-error" *ngIf="form.controls.password.hasError('required')">
                Senha é obrigatória.
              </mat-error>
            </mat-form-field>

            <div class="auth-error" *ngIf="authService.authError()" role="alert">
              <mat-icon>error</mat-icon>
              <span>{{ authService.authError() }}</span>
            </div>

            <button
              mat-flat-button
              color="primary"
              type="submit"
              class="submit-button full-width"
              [disabled]="form.invalid || authService.isAuthenticating()"
            >
              <mat-spinner diameter="20" *ngIf="authService.isAuthenticating()" />
              <span *ngIf="!authService.isAuthenticating()">Entrar</span>
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions class="card-footer">
          <p>Não possui conta? <a routerLink="/register">Criar conta</a></p>
        </mat-card-actions>
      </mat-card>
    </app-public-layout>
  `,
  styles: `
    .login-card {
      width: 100%;
      max-width: 420px;
      border-radius: 12px;
    }

    .card-header {
      display: block;
      text-align: center;
      margin-bottom: 1.5rem;
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .full-width {
      width: 100%;
    }

    .auth-error {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      border: 1px solid #fecaca;
      border-radius: 8px;
      background: #fef2f2;
      color: #991b1b;
      font-size: 0.875rem;
    }

    .auth-error mat-icon {
      width: 1.125rem;
      height: 1.125rem;
      font-size: 1.125rem;
    }

    .submit-button {
      height: 44px;
      border-radius: 8px;
      font-size: 1rem;
    }

    .card-footer {
      justify-content: center;
      padding-bottom: 1.25rem;
    }

    .card-footer p {
      margin: 0;
      color: #64748b;
      font-size: 0.875rem;
    }

    .card-footer a {
      color: #16a34a;
      font-weight: 500;
      text-decoration: none;
    }

    .card-footer a:hover {
      text-decoration: underline;
    }

    @media (max-width: 599px) {
      .login-card {
        max-width: none;
        min-height: 100vh;
        border: 0;
        border-radius: 0;
      }
    }
  `,
})
export class LoginPageComponent {
  readonly authService = inject(AuthService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly showPassword = signal(false);
  readonly form: FormGroup<LoginForm> = this.formBuilder.group({
    email: this.formBuilder.control('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(255),
    ]),
    password: this.formBuilder.control('', [Validators.required]),
  });

  submit(): void {
    if (this.form.invalid || this.authService.isAuthenticating()) {
      this.form.markAllAsTouched();
      return;
    }

    // A senha não é modificada nem registrada; permanece somente no fluxo da requisição.
    this.authService.login({
      email: this.form.controls.email.value,
      password: this.form.controls.password.value,
    });
  }

  togglePassword(): void {
    this.showPassword.update((visible) => !visible);
  }
}