import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { CashFlowApiService } from '../data-access/cash-flow-api.service';
import { CashFlowPoint, CashFlowProjection } from '../models/cash-flow.models';

type ChartBar = { x: number; y: number; width: number; height: number; date: string; balance: number };

@Component({
  selector: 'app-cash-flow-page',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, PageHeaderComponent, ContentPanelComponent, EmptyStateComponent],
  templateUrl: './cash-flow.page.html',
  styleUrl: './cash-flow.page.scss',
})
export class CashFlowPageComponent implements OnInit {
  private readonly api = inject(CashFlowApiService);
  readonly projection = signal<CashFlowProjection | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly from = signal(this.defaultFrom());
  readonly to = signal(this.defaultTo());
  readonly selectedPoint = signal<number | null>(null);
  readonly pageActions = [];

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api.project(this.from(), this.to()).subscribe({
      next: value => { this.projection.set(value); this.loading.set(false); },
      error: () => { this.error.set('Não foi possível carregar a projeção.'); this.loading.set(false); },
    });
  }

  money(value: number | string) { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value)); }
  date(value: string) { const [, month, day] = value.split('-').map(Number); return `${String(day).padStart(2, '0')}/${String(month).padStart(2, '0')}`; }
  defaultFrom() { const value = new Date(); value.setDate(value.getDate() - 29); return this.isoDate(value); }
  defaultTo() { return this.isoDate(new Date()); }
  private isoDate(value: Date) { return `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`; }

  /* chart removed */
  chartDates() {
    const projection = this.projection();
    if (!projection) return [];
    const dates: string[] = [];
    const current = new Date(`${projection.from}T00:00:00Z`);
    const end = new Date(`${projection.to}T00:00:00Z`);
    while (current <= end) { dates.push(current.toISOString().slice(0, 10)); current.setUTCDate(current.getUTCDate() + 1); }
    return dates;
  }
  chartLabels() { return this.chartDates().map(value => this.date(value)); }
  chartScale() {
    const max = Math.max(0, ...(this.projection()?.series ?? []).map(point => Number(point.balance)));
    return { max, range: Math.max(1000, Math.ceil(max / 1000) * 1000) };
  }
  chartAxisLabels() { const range = this.chartScale().range; return Array.from({ length: range / 1000 + 1 }, (_, index) => this.money(range - index * 1000)); }
  chartLabelX(index: number) { return ((index + 0.5) / Math.max(this.chartDates().length, 1)) * 100; }

  chartBar(index: number): ChartBar | null {
    const dates = this.chartDates();
    const date = dates[index];
    if (!date) return null;
    const item = (this.projection()?.series ?? []).find(point => point.date === date) as CashFlowPoint | undefined;
    if (!item) return null;
    const scale = this.chartScale();
    const plottedCount = Math.max((this.projection()?.series ?? []).length, 1); const cellWidth = 100 / plottedCount; const width = cellWidth * 0.8;
    const height = Number(item.balance) / scale.range * 90;
    return { x: index * cellWidth, y: 100 - height, width, height, date, balance: Number(item.balance) };
  }
  selectedChartBar() { const index = this.selectedPoint(); return index === null ? null : this.chartBar(index); }
  selectPoint(index: number) { this.selectedPoint.set(index); }
  tooltipX() { const bar = this.selectedChartBar(); return bar ? Math.min(Math.max(bar.x + bar.width / 2, 12), 88) : 0; }
  tooltipY() { const bar = this.selectedChartBar(); return bar ? Math.max(bar.y - 8, 12) : 0; }
}
