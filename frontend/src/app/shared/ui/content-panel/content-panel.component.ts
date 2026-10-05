import { Component, input, contentChild, TemplateRef, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

interface PanelAction {
  label: string;
  icon: string;
  handler: () => void;
  tooltip?: string;
  color?: 'primary' | 'accent' | 'warn';
  variant?: 'flat' | 'stroked' | 'icon';
  disabled?: boolean;
}

@Component({
  selector: 'app-content-panel',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <mat-card class="content-panel">
      <mat-card-header class="panel-header" *ngIf="title() || actions().length > 0">
        <mat-card-title class="panel-title">{{ title() }}</mat-card-title>
        <mat-card-subtitle *ngIf="subtitle()">{{ subtitle() }}</mat-card-subtitle>
        <div class="panel-actions" *ngIf="actions().length > 0">
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
      </mat-card-header>

      <mat-card-content class="panel-content">
        <ng-content />
      </mat-card-content>

      <mat-card-footer *ngIf="footerTemplate()" class="panel-footer">
        <ng-template [ngTemplateOutlet]="footerTemplate()!" />
      </mat-card-footer>
    </mat-card>
  `,
  styles: `
    .content-panel {
      border: 1px solid var(--card-border);
      border-radius: var(--radius-xl);
      box-shadow: var(--card-shadow);
      background-color: var(--card-bg);
    }

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: var(--space-4);
      padding: var(--space-5) var(--space-5) var(--space-3);
      margin: 0;
      flex-wrap: wrap;
    }

    .panel-title {
      font-size: var(--font-size-h3);
      font-weight: var(--font-semibold);
      color: var(--text-primary);
      line-height: var(--line-height-tight);
      margin: 0;
    }

    .panel-header mat-card-subtitle {
      font-size: var(--font-size-body-sm);
      color: var(--text-secondary);
      margin-top: var(--space-1);
    }

    .panel-actions {
      display: flex;
      gap: var(--space-2);
      flex-shrink: 0;
      flex-wrap: wrap;
    }

    .panel-content {
      padding: 0 var(--space-5) var(--space-5);
    }

    .panel-content:only-child {
      padding: var(--space-5);
    }

    .panel-footer {
      display: flex;
      justify-content: flex-end;
      gap: var(--space-2);
      padding: var(--space-4) var(--space-5);
      margin: 0;
      border-top: 1px solid var(--card-border);
      flex-wrap: wrap;
    }

    @media (max-width: 767px) {
      .panel-header {
        flex-direction: column;
        align-items: stretch;
        gap: var(--space-3);
        padding: var(--space-4);
      }

      .panel-actions {
        width: 100%;
        justify-content: flex-end;
      }

      .panel-content {
        padding: 0 var(--space-4) var(--space-4);
      }

      .panel-content:only-child {
        padding: var(--space-4);
      }

      .panel-footer {
        padding: var(--space-3) var(--space-4);
        flex-direction: column-reverse;
      }

      .panel-footer button {
        width: 100%;
      }
    }
  `,
})
export class ContentPanelComponent {
  readonly title = input<string>('');
  readonly subtitle = input<string>('');

  readonly actions = input<Array<PanelAction>>([]);

  readonly footerTemplate = contentChild(TemplateRef);

  hasFooter = computed(() => this.footerTemplate() !== undefined);
}