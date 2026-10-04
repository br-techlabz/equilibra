import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { PublicLayoutComponent } from '../../../../layout/public-layout/public-layout.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatCardModule, MatIconModule, PublicLayoutComponent],
  template: `
    <app-public-layout>
      <mat-card class="login-card" appearance="outlined">
        <mat-card-header>
          <mat-card-title>Login</mat-card-title>
          <mat-card-subtitle>Em implementação</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p>A funcionalidade de login será implementada na próxima etapa.</p>
          <p>Enquanto isso, você pode voltar para criar uma conta.</p>
        </mat-card-content>
        <mat-card-actions>
          <a mat-button routerLink="/register" color="primary"
            ><mat-icon>arrow_back</mat-icon>Voltar para cadastro</a
          >
        </mat-card-actions>
      </mat-card>
    </app-public-layout>
  `,
  styles: `
    .login-card {
      max-width: 420px;
      width: 100%;
    }
  `,
})
export class LoginPageComponent {}