import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatMenuModule, MatTooltipModule],
  template: `
    <header class="page-header">
      <mat-menu #actionMenu="matMenu">
        @for (item of actions()[0]?.menu ?? []; track item.label) {
          <button mat-menu-item type="button" (click)="item.handler()">
            <mat-icon>{{ item.icon }}</mat-icon>
            <span>{{ item.label }}</span>
          </button>
        }
      </mat-menu>
      <div class="header-content">
        <div class="title-group">
          <h1 class="page-title">{{ title() }}</h1>
          @if (subtitle()) {
            <p class="page-subtitle">{{ subtitle() }}</p>
          }
        </div>
        <div class="actions" *ngIf="actions().length > 0">
          @for (action of actions(); track action.label) {
            <button
              mat-flat-button
              color="primary"
              [matTooltip]="action.tooltip"
              [matMenuTriggerFor]="action.menu ? actionMenu : null"
              (click)="action.menu ? undefined : action.handler()"
              [disabled]="action.disabled">
              <mat-icon>{{ action.icon }}</mat-icon>
              <span>{{ action.label }}</span>
            </button>
          }
        </div>
      </div>
    </header>
  `,
  styles: `
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: var(--space-4);
      margin-bottom: var(--space-6);
      flex-wrap: wrap;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      width: 100%;
      gap: var(--space-4);
    }

    .title-group {
      display: flex;
      flex-direction: column;
      gap: var(--space-1);
      min-width: 0;
    }

    .page-title {
      font-size: var(--font-size-h2);
      font-weight: var(--font-semibold);
      color: var(--text-primary);
      line-height: var(--line-height-tight);
      margin: 0;
    }

    .page-subtitle {
      font-size: var(--font-size-body);
      color: var(--text-secondary);
      line-height: var(--line-height-normal);
      margin: 0;
    }

    .actions {
      display: flex;
      gap: var(--space-2);
      flex-shrink: 0;
    }

    @media (max-width: 767px) {
      .page-header {
        flex-direction: column;
        align-items: stretch;
      }

      .header-content {
        flex-direction: column;
        align-items: stretch;
      }

      .actions {
        width: 100%;
      }

      .actions button {
        flex: 1;
      }
    }
  `,
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string>('');

  readonly actions = input<Array<{
    label: string;
    icon: string;
    handler: () => void;
    tooltip?: string;
    disabled?: boolean;
    menu?: Array<{ label: string; icon: string; handler: () => void }>;
  }>>([]);
}