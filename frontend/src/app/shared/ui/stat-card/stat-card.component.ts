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
      border: 0;
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-sm);
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
      background: var(--color-primary-50);
      color: var(--color-primary-600);
    }

    .stat-card-icon--expense {
      background: var(--color-danger-50);
      color: var(--color-danger-600);
    }

    .stat-card-icon--balance {
      background: var(--color-info-50);
      color: var(--color-info-600);
    }

    .stat-card-icon--asset {
      background: #faf5ff;
      color: #7e22ce;
    }

    .stat-card-icon--neutral {
      background: var(--color-grey-100);
      color: var(--color-grey-600);
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
    .stat-card--income { background: var(--color-primary-50); }
    .stat-card--expense { background: var(--color-danger-50); }
    .stat-card--balance { background: var(--color-info-50); }
    .stat-card--asset { background: var(--color-primary-90); }
    .stat-card--neutral { background: var(--color-grey-100); }

    .stat-card--income,
    .stat-card--expense,
    .stat-card--balance,
    .stat-card--asset,
    .stat-card--neutral {
      border-left: 0;
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