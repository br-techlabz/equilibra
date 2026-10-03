import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

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
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  @Output() navigationClick = new EventEmitter<void>();

  readonly navigationItems: NavItem[] = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
    },
    {
      label: 'Cadastros',
      icon: 'folder',
      children: [
        { label: 'Contas', icon: 'account_balance', route: '/cadastros/contas' },
        { label: 'Categorias', icon: 'category', route: '/cadastros/categorias' },
        { label: 'Tags', icon: 'label', route: '/cadastros/tags' },
      ],
    },
    {
      label: 'Transações',
      icon: 'receipt_long',
      children: [
        { label: 'Despesas', icon: 'arrow_upward', route: '/transacoes/despesas' },
        { label: 'Receitas', icon: 'arrow_downward', route: '/transacoes/receitas' },
        { label: 'Transferências', icon: 'swap_horiz', route: '/transacoes/transferencias' },
      ],
    },
    {
      label: 'Relatórios',
      icon: 'bar_chart',
      route: '/relatorios',
    },
  ];

  onNavigationClick(): void {
    this.navigationClick.emit();
  }
}