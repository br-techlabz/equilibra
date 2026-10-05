import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { filter, startWith } from 'rxjs/operators';

interface BreadcrumbItem {
  label: string;
  url: string;
  isLast: boolean;
}

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <nav class="breadcrumb" aria-label="Navegação estrutural" *ngIf="items().length > 0">
      <ol class="breadcrumb-list">
        <li class="breadcrumb-item">
          <a routerLink="/" class="breadcrumb-link" aria-label="Início">
            <mat-icon class="breadcrumb-home">home</mat-icon>
          </a>
        </li>
        @for (item of items(); track item.url; let last = $last) {
          <li class="breadcrumb-item">
            <mat-icon class="breadcrumb-separator">chevron_right</mat-icon>
            @if (!last) {
              <a [routerLink]="item.url" class="breadcrumb-link">{{ item.label }}</a>
            } @else {
              <span class="breadcrumb-current" aria-current="page">{{ item.label }}</span>
            }
          </li>
        }
      </ol>
    </nav>
  `,
  styles: `
    .breadcrumb {
      display: flex;
      align-items: center;
      height: 100%;
    }

    .breadcrumb-list {
      display: flex;
      align-items: center;
      gap: 0;
      margin: 0;
      padding: 0;
      list-style: none;
      font-size: var(--font-size-body-sm);
    }

    .breadcrumb-item {
      display: flex;
      align-items: center;
    }

    .breadcrumb-item:not(:first-child)::before {
      content: '';
      display: none;
    }

    .breadcrumb-separator {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      color: var(--topbar-text-muted);
      font-size: 18px;
    }

    .breadcrumb-home {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: var(--topbar-text-muted);
    }

    .breadcrumb-link {
      display: flex;
      align-items: center;
      gap: var(--space-1);
      padding: var(--space-1) var(--space-2);
      color: var(--topbar-text-muted);
      text-decoration: none;
      border-radius: var(--radius-md);
      transition: color var(--transition-fast), background-color var(--transition-fast);
      white-space: nowrap;
    }

    .breadcrumb-link:hover {
      color: var(--topbar-text);
      background-color: var(--sidebar-hover);
    }

    .breadcrumb-current {
      display: flex;
      align-items: center;
      padding: var(--space-1) var(--space-2);
      color: var(--topbar-text);
      font-weight: var(--font-medium);
      white-space: nowrap;
    }

    @media (max-width: 767px) {
      .breadcrumb {
        display: none;
      }
    }
  `,
})
export class BreadcrumbComponent {
  private readonly router = inject(Router);

  readonly items = signal<BreadcrumbItem[]>([]);

  constructor() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      startWith(this.router.routerState.snapshot.root)
    ).subscribe(() => {
      this.buildBreadcrumbs();
    });
  }

  private buildBreadcrumbs(): void {
    const url = this.router.url;
    if (url === '/' || url === '/dashboard') {
      this.items.set([]);
      return;
    }

    const segments = url.split('/').filter(Boolean);
    const breadcrumbs: BreadcrumbItem[] = [];

    let currentPath = '';
    for (let i = 0; i < segments.length; i++) {
      currentPath += '/' + segments[i];
      const label = this.formatLabel(segments[i]);
      breadcrumbs.push({
        label,
        url: currentPath,
        isLast: i === segments.length - 1,
      });
    }

    this.items.set(breadcrumbs);
  }

  private formatLabel(segment: string): string {
    const labelMap: Record<string, string> = {
      'dashboard': 'Dashboard',
      'contas': 'Contas',
      'ativos': 'Contas de ativos',
      'despesas': 'Contas de despesas',
      'categorias': 'Categorias',
      'transacoes': 'Transações',
      'receitas': 'Receitas',
      'transferencias': 'Transferências',
      'relatorios': 'Relatórios',
    };

    return labelMap[segment] || segment.charAt(0).toUpperCase() + segment.slice(1);
  }
}