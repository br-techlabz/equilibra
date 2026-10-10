import { Component, inject, input, signal, computed, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../features/auth/data-access/auth.service';
import { filter } from 'rxjs/operators';

export interface NavItem {
  label: string;
  icon?: string;
  route?: string;
  children?: NavItem[];
  disabled?: boolean;
  expanded?: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatListModule,
    MatIconModule,
    MatExpansionModule,
    MatDividerModule,
    MatTooltipModule,
    MatButtonModule,
    MatMenuModule,
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  @Output() navigationClick = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  readonly collapsed = input(false);
  readonly isMobile = input(false);

  readonly currentRoute = signal('');

  readonly navigationItems: NavItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
    },
    {
      label: 'Metas Financeiras',
      icon: 'flag',
      route: '/goals',
    },
    {
      label: 'Orçamentos',
      icon: 'savings',
      route: '/budgets',
    },
    {
      label: 'Recorrências',
      icon: 'repeat',
      route: '/recurrences',
    },
    {
      label: 'Contas',
      icon: 'account_balance_wallet',
      route: '/accounts',
    },
    {
      label: 'Categorias',
      icon: 'category',
      route: '/categories',
    },
    {
      label: 'Tags',
      icon: 'label',
      route: '/tags',
    },
    {
      label: 'Transações',
      icon: 'receipt_long',
      route: '/transactions',
      children: [
        { label: 'Todas as transações', icon: 'list_alt', route: '/transactions' },
        { label: 'Despesas', icon: 'trending_down', route: '/expenses' },
        { label: 'Receitas', icon: 'trending_up', route: '/incomes' },
        { label: 'Transferências', icon: 'swap_horiz', route: '/transfers' },
      ],
    },
    {
      label: 'Relatórios',
      icon: 'assessment',
      children: [
        { label: 'Financeiro', icon: 'account_balance', route: '/reports/financial' },
        { label: 'Por categoria', icon: 'category', route: '/reports/categories' },
        { label: 'Auditoria', icon: 'fact_check', route: '/reports/audit' },
      ],
    },
  ];

  readonly userEmail = computed(() => this.authService.currentUser()?.email ?? '');

  ngOnInit(): void {
    this.currentRoute.set(this.router.url);

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute.set(event.urlAfterRedirects);
    });
  }

  isActive(route: string | undefined): boolean {
    if (!route) return false;
    const current = this.currentRoute();
    return current === route || current.startsWith(route + '/');
  }

  hasActiveChildren(item: NavItem): boolean {
    if (!item.children) return false;
    return item.children.some(child => this.isActive(child.route));
  }

  onNavigationClick(): void {
    this.navigationClick.emit();
  }

  onExpansionOpened(): void {
    // Abrir um submenu não é uma navegação; o drawer deve permanecer aberto.
  }

  onExpansionClosed(): void {
    // Fechar um submenu não é uma navegação; o drawer deve permanecer aberto.
  }

  trackByRoute(index: number, item: NavItem): string {
    return item.route ?? item.label;
  }

  trackByChildRoute(index: number, item: NavItem): string {
    return item.route ?? item.label;
  }
}