import { Component, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiStatusService, ApiStatus } from '../../core/http/api-status.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.scss',
})
export class DashboardPageComponent {
  private readonly apiStatusService = inject(ApiStatusService);

  // Estado reativo da API usando Signal
  readonly apiStatus = signal<ApiStatus>('loading');

  constructor() {
    // Efeito para verificar disponibilidade da API na inicialização
    effect(() => {
      this.checkApiStatus();
    });
  }

  /**
   * Verifica o status da API
   */
  checkApiStatus(): void {
    this.apiStatus.set('loading');
    this.apiStatusService.checkAvailability().subscribe({
      next: (status) => this.apiStatus.set(status),
      error: () => this.apiStatus.set('unavailable'),
    });
  }

  /**
   * Retorna ícone baseado no status
   */
  getStatusIcon(): string {
    switch (this.apiStatus()) {
      case 'available':
        return 'check_circle';
      case 'unavailable':
        return 'error';
      default:
        return 'sync';
    }
  }

  /**
   * Retorna cor do ícone baseado no status
   */
  getStatusColor(): string {
    switch (this.apiStatus()) {
      case 'available':
        return 'primary';
      case 'unavailable':
        return 'warn';
      default:
        return 'accent';
    }
  }

  /**
   * Retorna texto do status
   */
  getStatusText(): string {
    switch (this.apiStatus()) {
      case 'available':
        return 'API conectada';
      case 'unavailable':
        return 'API indisponível';
      default:
        return 'Verificando conexão...';
    }
  }
}