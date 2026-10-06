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
      label: 'Contas',
      icon: 'account_balance_wallet',
      children: [
        { label: 'Contas de ativos', icon: 'account_balance', route: '/accounts' },
        { label: 'Contas de despesas', icon: 'credit_card', route: '/contas/despesas' },
      ],
    },
    {
      label: 'Categorias',
      icon: 'category',
      route: '/categories',
    },
    {
      label: 'Transações',
      icon: 'receipt_long',
      children: [
        { label: 'Despesas', icon: 'trending_down', route: '/expenses' },
        { label: 'Receitas', icon: 'trending_up', route: '/incomes' },
        { label: 'Transferências', icon: 'swap_horiz', route: '/transacoes/transferencias' },
      ],
    },
    {
      label: 'Relatórios',
      icon: 'assessment',
      route: '/relatorios',
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

  trackByRoute(index: number, item: NavItem): string {
    return item.route ?? item.label;
  }

  trackByChildRoute(index: number, item: NavItem): string {
    return item.route ?? item.label;
  }
}