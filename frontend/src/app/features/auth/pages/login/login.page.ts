import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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
      <div class="login-container">
        <!-- Branding Section (Desktop) -->
        <div class="login-branding">
          <div class="brand-mark" aria-hidden="true">
            <mat-icon>account_balance_wallet</mat-icon>
          </div>
          <div class="branding-content">
            <h1 class="branding-title">Equilibra</h1>
            <p class="branding-tagline">Organize suas finanças. Encontre seu equilíbrio.</p>
            <p class="branding-description">
              Acompanhe suas receitas, despesas e patrimônio em um só lugar.
            </p>
            <div class="branding-visual" aria-hidden="true">
              <mat-icon>trending_up</mat-icon>
              <span></span><span></span><span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- Login Form Section -->
        <section class="login-panel" aria-labelledby="login-title">
          <div class="login-panel-header">
            <span class="panel-kicker">Equilibra</span>
            <h1 id="login-title">Bem-vindo de volta</h1>
            <p>Entre para acompanhar suas finanças com clareza.</p>
          </div>

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
                <mat-icon matPrefix>mail</mat-icon>
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
                <mat-icon matPrefix>lock</mat-icon>
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

          <div class="card-footer">
            <p>Não possui conta? <a routerLink="/register">Criar conta</a></p>
          </div>
        </section>
      </div>
    </app-public-layout>
  `,
  styles: `
    .login-container {
      display: grid;
      grid-template-columns: 1fr;
      min-height: 100vh;
      background: var(--card-bg);
    }

    @media (min-width: 960px) {
      .login-container {
        grid-template-columns: minmax(420px, 44%) 1fr;
      }
    }

    .login-branding {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      background: linear-gradient(155deg, var(--color-primary-800), var(--color-primary-600));
      padding: var(--space-10) var(--space-8);
      color: var(--text-on-primary);
      position: relative;
      overflow: hidden;
      text-align: center;
    }

    .login-branding::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -20%;
      width: 150%;
      height: 200%;
      background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
      pointer-events: none;
    }

    .brand-mark {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 76px;
      height: 76px;
      margin-bottom: var(--space-6);
      border: 1px solid rgba(255, 255, 255, 0.35);
      border-radius: var(--radius-full);
      background: rgba(255, 255, 255, 0.12);
    }

    .brand-mark mat-icon {
      width: 38px;
      height: 38px;
      font-size: 38px;
    }

    .branding-content {
      position: relative;
      z-index: 1;
      max-width: 400px;
      margin: 0 auto;
      text-align: center;
    }

    .branding-visual {
      position: relative;
      display: flex;
      align-items: flex-end;
      gap: var(--space-3);
      width: 220px;
      height: 110px;
      margin: var(--space-8) auto 0;
      padding: var(--space-5);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: var(--radius-xl);
      background: rgba(255, 255, 255, 0.1);
    }

    .branding-visual mat-icon {
      position: absolute;
      top: var(--space-4);
      left: var(--space-4);
      color: var(--text-on-primary);
      font-size: 24px;
    }

    .branding-visual span {
      flex: 1;
      min-width: 18px;
      border-radius: var(--radius-sm) var(--radius-sm) 0 0;
      background: rgba(255, 255, 255, 0.72);
    }

    .branding-visual span:nth-of-type(1) { height: 28%; }
    .branding-visual span:nth-of-type(2) { height: 44%; }
    .branding-visual span:nth-of-type(3) { height: 58%; }
    .branding-visual span:nth-of-type(4) { height: 76%; }
    .branding-visual span:nth-of-type(5) { height: 92%; }

    .branding-title {
      font-size: var(--font-size-3xl);
      font-weight: var(--font-bold);
      color: var(--text-on-primary);
      line-height: var(--line-height-tight);
      margin: 0 0 var(--space-2);
    }

    .branding-tagline {
      font-size: var(--font-size-lg);
      font-weight: var(--font-medium);
      color: rgba(255, 255, 255, 0.9);
      line-height: var(--line-height-tight);
      margin: 0 0 var(--space-4);
    }

    .branding-description {
      font-size: var(--font-size-body);
      color: rgba(255, 255, 255, 0.8);
      line-height: var(--line-height-relaxed);
      margin: 0;
    }

    .login-panel {
      display: flex;
      flex-direction: column;
      justify-content: center;
      width: min(100% - var(--space-10), 440px);
      margin: 0 auto;
      padding: var(--space-8) 0;
    }

    .login-panel-header {
      margin-bottom: var(--space-8);
    }

    .panel-kicker {
      display: block;
      margin-bottom: var(--space-3);
      color: var(--color-primary-600);
      font-size: var(--font-size-body-sm);
      font-weight: var(--font-semibold);
      letter-spacing: 0.12em;
      text-transform: uppercase;
    }

    .login-panel-header h1 {
      margin: 0 0 var(--space-2);
      color: var(--text-primary);
      font-size: clamp(var(--font-size-h2), 3vw, var(--font-size-h1));
      font-weight: var(--font-semibold);
      line-height: var(--line-height-tight);
    }

    .login-panel-header p {
      margin: 0;
      color: var(--text-secondary);
      font-size: var(--font-size-body);
    }

    .login-form .mat-mdc-form-field {
      --mdc-outlined-text-field-outline-color: var(--input-border);
      --mdc-outlined-text-field-hover-outline-color: var(--color-primary-300);
      --mdc-outlined-text-field-focus-outline-color: var(--color-primary-500);
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: var(--space-4);
    }

    .login-form .mat-mdc-form-field {
      --mdc-outlined-text-field-container-shape: var(--radius-md);
    }

    .full-width {
      width: 100%;
    }

    .auth-error {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      padding: var(--space-3) var(--space-4);
      border: 1px solid var(--color-danger-200);
      border-radius: var(--radius-lg);
      background: var(--color-danger-50);
      color: var(--color-danger-700);
      font-size: var(--font-size-body-sm);
    }

    .auth-error mat-icon {
      color: var(--color-danger-500);
      width: 20px;
      height: 20px;
      font-size: 20px;
      flex-shrink: 0;
    }

    .submit-button {
      height: 48px;
      border-radius: var(--radius-md);
      font-size: var(--font-size-body);
      font-weight: var(--font-semibold);
      margin-top: var(--space-2);
    }

    .card-footer {
      padding-top: var(--space-6);
      text-align: left;
    }

    .card-footer p {
      margin: 0;
      color: var(--text-secondary);
      font-size: var(--font-size-body-sm);
    }

    .card-footer a {
      color: var(--color-primary-500);
      font-weight: var(--font-medium);
      text-decoration: none;
      transition: color var(--transition-fast);
    }

    .card-footer a:hover {
      color: var(--color-primary-700);
      text-decoration: underline;
    }

    @media (max-width: 959px) {
      .login-branding {
        min-height: 220px;
        padding: var(--space-8) var(--space-6);
      }

      .brand-mark {
        width: 56px;
        height: 56px;
        margin-bottom: var(--space-3);
      }

      .brand-mark mat-icon {
        width: 28px;
        height: 28px;
        font-size: 28px;
      }

      .branding-title {
        font-size: var(--font-size-2xl);
      }

      .branding-description,
      .branding-visual {
        display: none;
      }

      .login-panel {
        width: min(100% - var(--space-8), 440px);
      }
    }

    @media (max-width: 599px) {
      .login-branding {
        min-height: 180px;
        padding: var(--space-6) var(--space-4);
      }

      .login-panel {
        width: min(100% - var(--space-6), 440px);
        padding: var(--space-8) 0;
      }
    }
  `,
})
export class LoginPageComponent {
  readonly authService = inject(AuthService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

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