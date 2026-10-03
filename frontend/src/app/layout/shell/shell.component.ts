import { Component, HostBinding, HostListener, signal, computed, OnInit } from '@angular/core';
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
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit {
  @HostBinding('class') class = 'app-shell';

  readonly isSidebarOpen = signal(false);
  readonly isMobile = signal(false);

  readonly sidenavMode = computed(() => (this.isMobile() ? 'over' : 'side'));
  readonly sidenavOpened = computed(() => (this.isMobile() ? this.isSidebarOpen() : true));

  onMenuToggle(): void {
    this.isSidebarOpen.update((open) => !open);
  }

  onSidebarClose(): void {
    if (this.isMobile()) {
      this.isSidebarOpen.set(false);
    }
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkMobile();
  }

  ngOnInit(): void {
    this.checkMobile();
  }

  private checkMobile(): void {
    this.isMobile.set(window.innerWidth < 960);
    if (!this.isMobile()) {
      this.isSidebarOpen.set(false);
    }
  }
}