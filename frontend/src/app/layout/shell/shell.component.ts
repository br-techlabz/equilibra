import { Component, HostBinding, HostListener, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { BreadcrumbComponent } from '../../shared/ui/breadcrumb/breadcrumb.component';
import { AuthService } from '../../features/auth/data-access/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatToolbarModule,
    MatSidenavModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatTooltipModule,
    HeaderComponent,
    SidebarComponent,
    BreadcrumbComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit {
  private readonly authService = inject(AuthService);

  @HostBinding('class') class = 'app-shell';

  readonly sidebarOpened = signal(true);
  readonly sidebarCollapsed = signal(false);
  readonly isMobile = signal(false);

  readonly sidenavMode = computed(() => (this.isMobile() ? 'over' : 'side'));
  readonly sidenavOpened = computed(() => (this.isMobile() ? this.sidebarOpened() : true));

  readonly currentUserEmail = computed(() => this.authService.currentUser()?.email ?? '');
  readonly isAuthenticated = computed(() => this.authService.isAuthenticated());

  ngOnInit(): void {
    this.checkMobile();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkMobile();
  }

  onMenuToggle(): void {
    this.sidebarOpened.update((open) => !open);
  }

  onSidebarClose(): void {
    if (this.isMobile()) {
      this.sidebarOpened.set(false);
    }
  }

  onSidebarCollapseToggle(): void {
    if (!this.isMobile()) {
      this.sidebarCollapsed.update((collapsed) => !collapsed);
    }
  }

  onLogout(): void {
    this.authService.logout();
    if (this.isMobile()) {
      this.sidebarOpened.set(false);
    }
  }

  private checkMobile(): void {
    this.isMobile.set(window.innerWidth < 768);
    if (!this.isMobile()) {
      this.sidebarOpened.set(true);
    }
  }
}
