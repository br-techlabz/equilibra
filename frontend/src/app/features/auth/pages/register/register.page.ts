import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PublicLayoutComponent } from '../../../../layout/public-layout/public-layout.component';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthApiService } from '../../data-access/auth-api.service';
import { ProblemDetails } from '../../../../core/http/problem-details';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

interface RegisterForm {
  email: FormControl<string>;
  password: FormControl<string>;
  confirmPassword: FormControl<string>;
}

const passwordsMatchValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const password = control.get('password')?.value as string | undefined;
  const confirmPassword = control.get('confirmPassword')?.value as string | undefined;

  if (!password || !confirmPassword) {
    return null;
  }

  return password === confirmPassword ? null : { passwordsMismatch: true };
};

@Component({
  selector: 'app-register-page',
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
    MatSnackBarModule,
  ],
  template: `
    <app-public-layout>
      <div class="register-container">
        <!-- Branding Section (Desktop) -->
        <div class="register-branding">
          <div class="brand-mark" aria-hidden="true">
            <mat-icon>account_balance_wallet</mat-icon>
          </div>
          <div class="branding-content">
            <h1 class="branding-title">Equilibra</h1>
            <p class="branding-tagline">Organize suas finanças. Encontre seu equilíbrio.</p>
            <p class="branding-description">
              Crie sua conta e comece a cuidar das suas finanças com mais clareza.
            </p>
            <div class="branding-visual" aria-hidden="true">
              <mat-icon>account_balance_wallet</mat-icon>
              <span></span><span></span><span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- Register Form Section -->
        <section class="register-panel" aria-labelledby="register-title">
          <div class="register-panel-header">
            <span class="panel-kicker">Equilibra</span>
            <h1 id="register-title">Crie sua conta</h1>
            <p>Comece a organizar suas finanças com clareza.</p>
          </div>

          <mat-card-content>
            <form [formGroup]="form" (ngSubmit)="submit()" novalidate class="register-form">
              <mat-form-field appearance="outline" class="full-width" subscriptSizing="dynamic">
                <mat-label>E-mail</mat-label>
                <input
                  matInput
                  type="email"
                  formControlName="email"
                  autocomplete="email"
                  aria-describedby="register-email-error"
                />
                <mat-icon matPrefix>mail</mat-icon>
                <mat-error id="register-email-error" *ngIf="form.controls.email.hasError('required')">
                  E-mail é obrigatório.
                </mat-error>
                <mat-error id="register-email-error" *ngIf="form.controls.email.hasError('email')">
                  Formato de e-mail inválido.
                </mat-error>
                <mat-error id="register-email-error" *ngIf="form.controls.email.hasError('maxlength')">
                  E-mail não pode exceder 255 caracteres.
                </mat-error>
                <mat-error id="register-email-error" *ngIf="form.controls.email.hasError('server')">
                  {{ form.controls.email.getError('server') }}
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width" subscriptSizing="dynamic">
                <mat-label>Senha</mat-label>
                <input
                  matInput
                  [type]="showPassword() ? 'text' : 'password'"
                  formControlName="password"
                  autocomplete="new-password"
                  aria-describedby="register-password-error"
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
                <mat-hint *ngIf="!form.controls.password.errors?.['required']">
                  Use pelo menos 8 caracteres. Máximo 128.
                </mat-hint>
                <mat-error id="register-password-error" *ngIf="form.controls.password.hasError('required')">
                  Senha é obrigatória.
                </mat-error>
                <mat-error id="register-password-error" *ngIf="form.controls.password.hasError('minlength')">
                  Senha deve possuir pelo menos 8 caracteres.
                </mat-error>
                <mat-error id="register-password-error" *ngIf="form.controls.password.hasError('maxlength')">
                  Senha excede o tamanho máximo de 128 caracteres.
                </mat-error>
                <mat-error id="register-password-error" *ngIf="form.controls.password.hasError('onlyWhitespace')">
                  Senha não pode conter apenas espaços em branco.
                </mat-error>
                <mat-error id="register-password-error" *ngIf="form.controls.password.hasError('server')">
                  {{ form.controls.password.getError('server') }}
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width" subscriptSizing="dynamic">
                <mat-label>Confirmar senha</mat-label>
                <input
                  matInput
                  [type]="showConfirmPassword() ? 'text' : 'password'"
                  formControlName="confirmPassword"
                  autocomplete="new-password"
                  aria-describedby="register-confirm-error"
                />
                <mat-icon matPrefix>lock</mat-icon>
                <button
                  type="button"
                  matSuffix
                  mat-icon-button
                  (click)="toggleConfirmPassword()"
                  [attr.aria-label]="showConfirmPassword() ? 'Ocultar senha' : 'Mostrar senha'"
                  [attr.aria-pressed]="showConfirmPassword()"
                >
                  <mat-icon>{{ showConfirmPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                <mat-error id="register-confirm-error" *ngIf="form.controls.confirmPassword.hasError('required')">
                  Confirmação de senha é obrigatória.
                </mat-error>
                <mat-error id="register-confirm-error" *ngIf="isPasswordMismatch()">
                  As senhas não coincidem.
                </mat-error>
              </mat-form-field>

              <div class="auth-error" *ngIf="serverError()" role="alert">
                <mat-icon>error</mat-icon>
                <span>{{ serverError() }}</span>
              </div>

              <button
                mat-flat-button
                color="primary"
                type="submit"
                class="submit-button full-width"
                [disabled]="form.invalid || isSubmitting()"
              >
                <mat-spinner diameter="20" *ngIf="isSubmitting()" />
                <span *ngIf="!isSubmitting()">Criar conta</span>
              </button>
            </form>
          </mat-card-content>

          <div class="card-footer">
            <p>Já possui conta? <a routerLink="/login">Entrar</a></p>
          </div>
        </section>
      </div>
    </app-public-layout>
  `,
  styles: `
    .register-container {
      display: grid;
      grid-template-columns: 1fr;
      min-height: 100vh;
      background: var(--card-bg);
    }

    @media (min-width: 960px) {
      .register-container {
        grid-template-columns: minmax(420px, 44%) 1fr;
      }
    }

    .register-branding {
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

    .register-branding::before {
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
      max-width: 480px;
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
      margin: 0 0 var(--space-8);
    }

    .branding-features {
      display: flex;
      flex-direction: column;
      gap: var(--space-3);
      text-align: left;
      max-width: 320px;
      margin: 0 auto;
    }

    .feature-item {
      display: flex;
      align-items: center;
      gap: var(--space-3);
      padding: var(--space-2) var(--space-3);
      background: rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-lg);
      font-size: var(--font-size-body-sm);
      color: rgba(255, 255, 255, 0.9);
    }

    .feature-item mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: var(--text-on-primary);
      flex-shrink: 0;
    }

    .register-panel {
      display: flex;
      flex-direction: column;
      justify-content: center;
      width: min(100% - var(--space-10), 440px);
      margin: 0 auto;
      padding: var(--space-8) 0;
    }

    .register-panel-header {
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

    .register-panel-header h1 {
      margin: 0 0 var(--space-2);
      color: var(--text-primary);
      font-size: clamp(var(--font-size-h2), 3vw, var(--font-size-h1));
      font-weight: var(--font-semibold);
      line-height: var(--line-height-tight);
    }

    .register-panel-header p {
      margin: 0;
      color: var(--text-secondary);
      font-size: var(--font-size-body);
    }

    .register-form .mat-mdc-form-field {
      --mdc-outlined-text-field-outline-color: var(--input-border);
      --mdc-outlined-text-field-hover-outline-color: var(--color-primary-300);
      --mdc-outlined-text-field-focus-outline-color: var(--color-primary-500);
    }

    .register-form {
      display: flex;
      flex-direction: column;
      gap: var(--space-4);
    }

    .register-form .mat-mdc-form-field {
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
      .register-branding {
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

      .register-panel {
        width: min(100% - var(--space-8), 440px);
      }
    }

    @media (max-width: 599px) {
      .register-branding {
        min-height: 180px;
        padding: var(--space-6) var(--space-4);
      }

      .register-panel {
        width: min(100% - var(--space-6), 440px);
        padding: var(--space-8) 0;
      }
    }
  `,
})
export class RegisterPageComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly isSubmitting = signal(false);
  readonly showPassword = signal(false);
  readonly showConfirmPassword = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form: FormGroup<RegisterForm> = this.formBuilder.group(
    {
      email: this.formBuilder.control('', [
        Validators.required,
        Validators.email,
        Validators.maxLength(255),
      ]),
      password: this.formBuilder.control('', [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(128),
        this.notOnlyWhitespaceValidator(),
      ]),
      confirmPassword: this.formBuilder.control('', [Validators.required]),
    },
    { validators: passwordsMatchValidator },
  );

  submit(): void {
    this.serverError.set(null);

    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    // confirmPassword é validado apenas no frontend e deliberadamente não é enviado.
    const request = {
      email: this.form.controls.email.value,
      password: this.form.controls.password.value,
    };

    this.authApi
      .register(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.snackBar.open('Conta criada com sucesso.', 'Fechar', {
            duration: 4000,
            politeness: 'polite',
          });
          void this.router.navigate(['/login']);
        },
        error: (error: unknown) => {
          this.isSubmitting.set(false);
          this.handleError(error);
        },
      });
  }

  togglePassword(): void {
    this.showPassword.update((visible) => !visible);
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword.update((visible) => !visible);
  }

  isPasswordMismatch(): boolean {
    const control = this.form.controls.confirmPassword;
    return (
      control.touched && this.form.hasError('passwordsMismatch')
    );
  }

  private notOnlyWhitespaceValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value as string;
      if (!value || value.trim().length > 0) {
        return null;
      }
      return { onlyWhitespace: true };
    };
  }

  private handleError(error: unknown): void {
    if (!(error instanceof HttpErrorResponse)) {
      this.serverError.set('Não foi possível criar sua conta. Tente novamente.');
      return;
    }

    const problem = this.asProblemDetails(error.error);

    if (error.status === 0) {
      const apiUrl = error.url ?? '/api/auth/register';
      this.serverError.set(
        `Não foi possível conectar ao serviço (${apiUrl}). Verifique se a API está disponível.`,
      );
      return;
    }

    if (error.status === 400 && problem?.errors) {
      this.applyFieldErrors(problem.errors);
      this.serverError.set(problem.detail || 'Revise os dados informados.');
      return;
    }

    if (error.status === 409) {
      this.serverError.set('Já existe uma conta cadastrada com este e-mail.');
      return;
    }

    this.serverError.set('Não foi possível criar sua conta. Tente novamente.');
  }

  private applyFieldErrors(errors: readonly { field: string; message: string }[]): void {
    for (const fieldError of errors) {
      const control = this.form.get(fieldError.field);
      if (control) {
        control.setErrors({ ...(control.errors ?? {}), server: fieldError.message });
        control.markAsTouched();
      }
    }
  }

  private asProblemDetails(value: unknown): ProblemDetails | null {
    if (
      typeof value === 'object' &&
      value !== null &&
      typeof (value as ProblemDetails).detail === 'string'
    ) {
      return value as ProblemDetails;
    }
    return null;
  }
}