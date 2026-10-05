import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

type StatCardVariant = 'income' | 'expense' | 'balance' | 'asset' | 'neutral';

interface StatCardAction {
  label: string;
  icon: string;
  handler: () => void;
  tooltip?: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatCardModule, MatButtonModule, MatTooltipModule],
  template: `
    <mat-card class="stat-card" [class]="'stat-card--' + variant()">
      <div class="stat-card-content">
        <div class="stat-card-main">
          <div class="stat-card-icon" [class]="'stat-card-icon--' + variant()">
            <mat-icon>{{ icon() }}</mat-icon>
          </div>
          <div class="stat-card-text">
            <p class="stat-card-label">{{ label() }}</p>
            <p class="stat-card-value" [class.negative]="isNegative()">
              {{ value() }}
            </p>
            @if (trend()) {
              <p class="stat-card-trend" [class.positive]="trendPositive()" [class.negative]="!trendPositive()">
                <mat-icon>{{ trendPositive() ? 'trending_up' : 'trending_down' }}</mat-icon>
                <span>{{ trend() }}</span>
              </p>
            }
          </div>
        </div>
        @if (action()) {
          <div class="stat-card-action">
            <button
              mat-icon-button
              [matTooltip]="action()?.tooltip"
              (click)="action()?.handler()"
              [disabled]="action()?.disabled"
              class="stat-card-action-btn">
              <mat-icon>{{ action()?.icon }}</mat-icon>
            </button>
          </div>
        }
      </div>
    </mat-card>
  `,
  styles: `
    .stat-card {
      border: 1px solid var(--card-border);
      border-radius: var(--radius-xl);
      box-shadow: var(--card-shadow);
      transition: box-shadow var(--transition-normal), transform var(--transition-fast);
      overflow: hidden;
    }

    .stat-card:hover {
      box-shadow: var(--shadow-md);
      transform: translateY(-2px);
    }

    .stat-card-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: var(--space-5);
      gap: var(--space-4);
    }

    .stat-card-main {
      display: flex;
      align-items: flex-start;
      gap: var(--space-4);
      flex: 1;
      min-width: 0;
    }

    .stat-card-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: var(--radius-lg);
      color: var(--text-on-primary);
      flex-shrink: 0;
    }

    .stat-card-icon mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .stat-card-icon--income {
      background: linear-gradient(135deg, var(--color-success-500), var(--color-success-700));
    }

    .stat-card-icon--expense {
      background: linear-gradient(135deg, var(--color-danger-500), var(--color-danger-700));
    }

    .stat-card-icon--balance {
      background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700));
    }

    .stat-card-icon--asset {
      background: linear-gradient(135deg, var(--color-asset), var(--color-info-700));
    }

    .stat-card-icon--neutral {
      background: linear-gradient(135deg, var(--text-tertiary), var(--text-secondary));
    }

    .stat-card-text {
      display: flex;
      flex-direction: column;
      gap: var(--space-1);
      min-width: 0;
    }

    .stat-card-label {
      font-size: var(--font-size-caption);
      font-weight: var(--font-medium);
      color: var(--text-tertiary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin: 0;
    }

    .stat-card-value {
      font-size: var(--font-size-h3);
      font-weight: var(--font-bold);
      color: var(--text-primary);
      line-height: var(--line-height-tight);
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .stat-card-value.negative {
      color: var(--color-expense);
    }

    .stat-card-trend {
      display: inline-flex;
      align-items: center;
      gap: var(--space-1);
      font-size: var(--font-size-caption);
      font-weight: var(--font-medium);
      margin: 0;
    }

    .stat-card-trend.positive {
      color: var(--color-success-600);
    }

    .stat-card-trend.negative {
      color: var(--color-danger-600);
    }

    .stat-card-trend mat-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
    }

    .stat-card-action {
      display: flex;
      align-items: center;
      flex-shrink: 0;
    }

    .stat-card-action-btn {
      width: 36px;
      height: 36px;
      color: var(--text-tertiary);
      transition: color var(--transition-fast), background-color var(--transition-fast);
    }

    .stat-card-action-btn:hover {
      color: var(--color-primary-500);
      background-color: var(--color-primary-50);
    }

    /* Variant backgrounds */
    .stat-card--income {
      border-left: 4px solid var(--color-success-500);
    }

    .stat-card--expense {
      border-left: 4px solid var(--color-danger-500);
    }

    .stat-card--balance {
      border-left: 4px solid var(--color-primary-500);
    }

    .stat-card--asset {
      border-left: 4px solid var(--color-asset);
    }

    .stat-card--neutral {
      border-left: 4px solid var(--text-tertiary);
    }

    @media (max-width: 767px) {
      .stat-card-content {
        padding: var(--space-4);
        gap: var(--space-3);
      }

      .stat-card-icon {
        width: 40px;
        height: 40px;
      }

      .stat-card-value {
        font-size: var(--font-size-xl);
      }
    }
  `,
})
export class StatCardComponent {
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  readonly icon = input.required<string>();
  readonly variant = input<StatCardVariant>('neutral');
  readonly trend = input<string>('');
  readonly trendPositive = input(true);
  readonly action = input<StatCardAction | null>(null);

  readonly isNegative = computed(() => this.variant() === 'expense');
}