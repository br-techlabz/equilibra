import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  template: `
    <div class="empty-state" [class.centered]="centered()">
      <div class="empty-state-icon" [class]="'empty-state-icon--' + iconVariant()">
        <mat-icon>{{ icon() }}</mat-icon>
      </div>

      <h3 class="empty-state-title">{{ title() }}</h3>

      @if (description()) {
        <p class="empty-state-description">{{ description() }}</p>
      }

      @if (actions().length > 0) {
        <div class="empty-state-actions">
          @for (action of actions(); track action.label) {
            @if (action.variant === 'icon') {
              <button
                mat-icon-button
                [color]="action.color || 'primary'"
                [matTooltip]="action.tooltip"
                (click)="action.handler()"
                [disabled]="action.disabled">
                <mat-icon>{{ action.icon }}</mat-icon>
              </button>
            } @else if (action.variant === 'stroked') {
              <button
                mat-stroked-button
                [color]="action.color || 'primary'"
                [matTooltip]="action.tooltip"
                (click)="action.handler()"
                [disabled]="action.disabled">
                <mat-icon>{{ action.icon }}</mat-icon>
                <span>{{ action.label }}</span>
              </button>
            } @else {
              <button
                mat-flat-button
                [color]="action.color || 'primary'"
                [matTooltip]="action.tooltip"
                (click)="action.handler()"
                [disabled]="action.disabled">
                <mat-icon>{{ action.icon }}</mat-icon>
                <span>{{ action.label }}</span>
              </button>
            }
          }
        </div>
      }
    </div>
  `,
  styles: `
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: var(--space-10) var(--space-6);
      gap: var(--space-4);
    }

    .empty-state.centered {
      justify-content: center;
      min-height: 300px;
    }

    .empty-state-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      border-radius: var(--radius-full);
      color: var(--text-on-primary);
      flex-shrink: 0;
    }

    .empty-state-icon mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
    }

    .empty-state-icon--income {
      background: linear-gradient(135deg, var(--color-success-500), var(--color-success-700));
    }

    .empty-state-icon--expense {
      background: linear-gradient(135deg, var(--color-danger-500), var(--color-danger-700));
    }

    .empty-state-icon--balance {
      background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700));
    }

    .empty-state-icon--asset {
      background: linear-gradient(135deg, var(--color-asset), var(--color-info-700));
    }

    .empty-state-icon--neutral {
      background: linear-gradient(135deg, var(--text-tertiary), var(--text-secondary));
    }

    .empty-state-icon--search {
      background: linear-gradient(135deg, var(--text-tertiary), var(--text-secondary));
    }

    .empty-state-icon--inbox {
      background: linear-gradient(135deg, var(--color-info-500), var(--color-info-700));
    }

    .empty-state-icon--chart {
      background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700));
    }

    .empty-state-icon--folder {
      background: linear-gradient(135deg, var(--color-warning-500), var(--color-warning-700));
    }

    .empty-state-title {
      font-size: var(--font-size-h3);
      font-weight: var(--font-semibold);
      color: var(--text-primary);
      line-height: var(--line-height-tight);
      margin: 0;
      max-width: 400px;
    }

    .empty-state-description {
      font-size: var(--font-size-body);
      color: var(--text-secondary);
      line-height: var(--line-height-relaxed);
      margin: 0;
      max-width: 480px;
    }

    .empty-state-actions {
      display: flex;
      gap: var(--space-3);
      flex-wrap: wrap;
      justify-content: center;
      margin-top: var(--space-2);
    }

    @media (max-width: 767px) {
      .empty-state {
        padding: var(--space-6) var(--space-4);
        gap: var(--space-3);
      }

      .empty-state-icon {
        width: 64px;
        height: 64px;
      }

      .empty-state-icon mat-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
      }

      .empty-state-title {
        font-size: var(--font-size-xl);
      }

      .empty-state-actions {
        flex-direction: column;
        width: 100%;
      }

      .empty-state-actions button {
        width: 100%;
      }
    }
  `,
})
export class EmptyStateComponent {
  readonly icon = input.required<string>();
  readonly iconVariant = input<'income' | 'expense' | 'balance' | 'asset' | 'neutral' | 'search' | 'inbox' | 'chart' | 'folder'>('neutral');
  readonly title = input.required<string>();
  readonly description = input<string>('');
  readonly centered = input(true);

  readonly actions = input<Array<{
    label: string;
    icon?: string;
    handler: () => void;
    color?: 'primary' | 'accent' | 'warn';
    variant?: 'flat' | 'stroked' | 'icon';
    tooltip?: string;
    disabled?: boolean;
  }>>([]);
}