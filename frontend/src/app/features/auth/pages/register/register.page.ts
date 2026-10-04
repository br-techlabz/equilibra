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
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { PublicLayoutComponent } from '../../../../layout/public-layout/public-layout.component';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthApiService } from '../../data-access/auth-api.service';
import { ProblemDetails } from '../../../../core/http/problem-details';

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
    PublicLayoutComponent,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss',
})
export class RegisterPageComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly authApi = inject(AuthApiService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
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

    if (error.status === 400 && problem?.errors) {
      this.applyFieldErrors(problem.errors);
      this.serverError.set(problem.detail || 'Revise os dados informados.');
      return;
    }

    if (error.status === 409) {
      this.serverError.set('Já existe uma conta cadastrada com este e-mail.');
      return;
    }

    if (error.status === 0) {
      this.serverError.set('Não foi possível conectar ao serviço. Tente novamente.');
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