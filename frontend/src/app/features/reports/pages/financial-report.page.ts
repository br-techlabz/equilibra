import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { AssetAccountsApiService } from '../../asset-accounts/data-access/asset-accounts-api.service';
import { AssetAccount } from '../../asset-accounts/models/asset-account.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { FinancialReportApiService } from '../data-access/financial-report-api.service';
import { FinancialReportFilters, FinancialReportResponse, FinancialReportTransaction } from '../models/financial-report.models';

interface ReportForm { from: FormControl<Date>; to: FormControl<Date>; accountIds: FormControl<string[]>; }

@Component({
  selector: 'app-financial-report-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatDatepickerModule, MatNativeDateModule, MatPaginatorModule, MatSelectModule, ContentPanelComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <main class="financial-report-page">
      <app-page-header title="Relatório financeiro" subtitle="Analise receitas, despesas e a evolução dos saldos das suas contas." />
      <app-content-panel title="Filtros do relatório" subtitle="Escolha o período e as contas que deseja analisar.">
        <form class="report-filters" [formGroup]="form" (ngSubmit)="applyFilters()" novalidate>
          <mat-form-field appearance="outline"><mat-label>Data inicial</mat-label><input matInput [matDatepicker]="fromPicker" formControlName="from" placeholder="dd/MM/yyyy" aria-label="Data inicial no formato dia, mês e ano" /><button mat-icon-button matSuffix type="button" aria-label="Abrir calendário da data inicial" (click)="fromPicker.open()"><mat-icon>calendar_month</mat-icon></button><mat-datepicker #fromPicker></mat-datepicker><mat-error>Informe a data inicial.</mat-error></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Data final</mat-label><input matInput [matDatepicker]="toPicker" formControlName="to" placeholder="dd/MM/yyyy" aria-label="Data final no formato dia, mês e ano" /><button mat-icon-button matSuffix type="button" aria-label="Abrir calendário da data final" (click)="toPicker.open()"><mat-icon>calendar_month</mat-icon></button><mat-datepicker #toPicker></mat-datepicker><mat-error>Informe uma data final posterior à inicial.</mat-error></mat-form-field>
          <mat-form-field appearance="outline" class="account-filter"><mat-label>Contas</mat-label><mat-select multiple formControlName="accountIds"><mat-option [value]="ALL_ACCOUNTS">Todas as contas</mat-option>@for(account of accounts(); track account.id){<mat-option [value]="account.id">{{account.name}}{{account.active?'':' (Inativa)'}}</mat-option>}</mat-select></mat-form-field>
          <div class="filter-actions"><button mat-flat-button color="primary" type="submit" [disabled]="isLoading()">Aplicar filtros</button><button mat-button type="button" (click)="resetFilters()" [disabled]="isLoading()">Restaurar</button></div>
        </form>
        @if(formError()){<p class="form-error" role="alert">{{formError()}}</p>}
      </app-content-panel>
      @if(isLoading()){<div class="state-block" role="status"><mat-icon class="spin">sync</mat-icon><span>Carregando relatório...</span></div>}
      @else if(errorMessage()){<div class="state-block error-state" role="alert"><mat-icon>error_outline</mat-icon><p>{{errorMessage()}}</p><button mat-stroked-button type="button" (click)="retry()">Tentar novamente</button></div>}
      @if(report(); as value){
        <section class="summary-grid" aria-label="Resumo financeiro">
          <article class="metric-card"><span>Saldo inicial</span><strong>{{money(value.openingBalance)}}</strong></article><article class="metric-card income"><span>Receitas</span><strong>{{money(value.incomeTotal)}}</strong></article><article class="metric-card expense"><span>Despesas</span><strong>{{money(value.expenseTotal)}}</strong></article><article class="metric-card"><span>Resultado financeiro</span><strong>{{money(value.financialResult)}}</strong><small>Receitas menos despesas.</small></article><article class="metric-card"><span>Variação do saldo</span><strong>{{money(value.balanceChange)}}</strong><small>Inclui transferências de fronteira.</small></article><article class="metric-card highlight"><span>Saldo final</span><strong>{{money(value.closingBalance)}}</strong><small>Saldo inicial + variação.</small></article>
        </section>
        <app-content-panel title="Transferências" subtitle="Transferências não são receitas nem despesas."><div class="transfer-grid"><span>Internas <strong>{{money(value.internalTransferTotal)}}</strong></span><span>Recebidas <strong>{{money(value.incomingTransferTotal)}}</strong></span><span>Enviadas <strong>{{money(value.outgoingTransferTotal)}}</strong></span></div></app-content-panel>
        <app-content-panel title="Movimentações do período"><div class="details-table-wrapper"><table class="details-table"><thead><tr><th>Data</th><th>Descrição</th><th>Tipo</th><th>Valor</th><th>Status</th></tr></thead><tbody>@for(item of value.details.content; track item.id){<tr><td>{{date(item.occurredAt)}}</td><td>{{item.description}}</td><td>{{type(item)}}</td><td>{{money(item.amount)}}</td><td>{{item.status === 'CANCELLED' ? 'Cancelada' : 'Ativa'}}</td></tr>}</tbody></table></div>@if(value.details.content.length===0){<app-empty-state icon="receipt_long" iconVariant="inbox" title="Nenhuma movimentação no período" description="Os saldos continuam disponíveis no resumo." [centered]="false"/>}@if(value.details.totalPages>1){<mat-paginator [length]="value.details.totalElements" [pageIndex]="value.details.page" [pageSize]="value.details.size" [pageSizeOptions]="[10,20,50]" (page)="changePage($event)" aria-label="Paginação das movimentações"/>}</app-content-panel>
      } @else if(!isLoading() && !errorMessage() && accounts().length===0){<app-empty-state icon="account_balance_wallet" iconVariant="inbox" title="Você ainda não possui contas" description="Crie uma conta para gerar seu relatório financeiro." [centered]="false"/>}
    </main>
  `,
  styles: `
    .financial-report-page{display:flex;flex-direction:column;gap:var(--space-6);padding-bottom:var(--space-8)}.report-filters{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-4);align-items:start}.account-filter{min-width:0}.filter-actions{display:flex;gap:var(--space-2);align-items:center;min-height:56px}.form-error,.error-state{color:var(--color-danger-700)}.summary-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-4)}.metric-card{display:flex;flex-direction:column;gap:var(--space-2);padding:var(--space-5);border:1px solid var(--card-border);border-radius:var(--radius-lg);background:var(--card-bg);box-shadow:var(--shadow-sm)}.metric-card span,.metric-card small{color:var(--text-secondary);font-size:var(--font-size-body-sm)}.metric-card strong{color:var(--text-primary);font-size:var(--font-size-h3)}.metric-card.income strong{color:var(--color-income-text)}.metric-card.expense strong{color:var(--color-expense-text)}.metric-card.highlight{border-color:var(--color-primary-200)}.transfer-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:var(--space-4)}.transfer-grid span{display:flex;flex-direction:column;gap:var(--space-2);color:var(--text-secondary)}.transfer-grid strong{color:var(--text-primary);font-size:var(--font-size-h4)}.details-table-wrapper{overflow-x:auto}.details-table{width:100%;border-collapse:collapse}.details-table th,.details-table td{padding:var(--space-3);border-bottom:1px solid var(--card-border);text-align:left;white-space:nowrap}.state-block{display:flex;min-height:220px;flex-direction:column;align-items:center;justify-content:center;gap:var(--space-3);color:var(--text-secondary)}.spin{animation:report-spin 1s linear infinite}@keyframes report-spin{to{transform:rotate(360deg)}}@media(max-width:767px){.report-filters,.summary-grid{grid-template-columns:1fr}.filter-actions{min-height:auto;justify-content:flex-end}.transfer-grid{grid-template-columns:1fr}.details-table-wrapper{display:none}}
  `,
})
export class FinancialReportPageComponent implements OnInit {
  readonly ALL_ACCOUNTS = 'ALL';
  readonly accounts = signal<AssetAccount[]>([]); readonly report = signal<FinancialReportResponse | null>(null); readonly isLoading = signal(false); readonly errorMessage = signal<string | null>(null); readonly formError = signal<string | null>(null);
  readonly form: FormGroup<ReportForm>; private appliedFilters: FinancialReportFilters; private readonly api = inject(FinancialReportApiService); private readonly accountsApi = inject(AssetAccountsApiService);
  constructor(private readonly fb: NonNullableFormBuilder){const defaults=this.defaultDates();this.form=this.fb.group({from:this.fb.control(new Date(`${defaults.from}T00:00:00`),Validators.required),to:this.fb.control(new Date(`${defaults.to}T00:00:00`),Validators.required),accountIds:this.fb.control<string[]>([])});this.appliedFilters={from:this.start(defaults.from),to:this.end(defaults.to),accountIds:[],page:0,size:20};}
  ngOnInit(): void { this.accountsApi.list(true).subscribe({next:v=>this.accounts.set(v),error:()=>undefined}); this.load(this.appliedFilters); }
  applyFilters(): void { const v=this.form.getRawValue(); const from=this.dateInput(v.from); const to=this.dateInput(v.to); if(!from||!to||from>=to){this.formError.set('A data inicial deve ser anterior à data final.');return;} this.formError.set(null); const ids=v.accountIds.includes(this.ALL_ACCOUNTS)?[]:[...new Set(v.accountIds)]; this.appliedFilters={from:this.start(from),to:this.end(to),accountIds:ids,page:0,size:20}; this.load(this.appliedFilters); }
  resetFilters(): void {const d=this.defaultDates();this.form.reset({from:new Date(`${d.from}T00:00:00`),to:new Date(`${d.to}T00:00:00`),accountIds:[]});this.formError.set(null);this.appliedFilters={from:this.start(d.from),to:this.end(d.to),accountIds:[],page:0,size:20};this.load(this.appliedFilters);}
  retry(): void { this.load(this.appliedFilters); }
  changePage(e:PageEvent): void {this.appliedFilters={...this.appliedFilters,page:e.pageIndex,size:e.pageSize};this.load(this.appliedFilters);}
  money(value:number):string{return new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(value);}
  date(value:string):string{return new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short'}).format(new Date(value));}
  type(item:FinancialReportTransaction):string{return item.type==='EXPENSE'?'Despesa':item.type==='INCOME'?'Receita':'Transferência';}
  private load(filters:FinancialReportFilters):void{this.isLoading.set(true);this.errorMessage.set(null);this.api.query(filters).subscribe({next:v=>{this.report.set(v);this.isLoading.set(false)},error:()=>{this.isLoading.set(false);this.errorMessage.set('Não foi possível carregar o relatório financeiro.')}});}
  private defaultDates():{from:string;to:string}{const now=new Date();const from=new Date(now.getFullYear(),now.getMonth(),1);const to=new Date(now.getFullYear(),now.getMonth()+1,1);return{from:this.dateInput(from),to:this.dateInput(to)};}
  private dateInput(value:Date):string{return `${value.getFullYear()}-${String(value.getMonth()+1).padStart(2,'0')}-${String(value.getDate()).padStart(2,'0')}`;}
  private start(value:string):string{const [year,month,day]=value.split('-').map(Number);return new Date(Date.UTC(year,month-1,day)).toISOString();}
  private end(value:string):string{const [year,month,day]=value.split('-').map(Number);const next=new Date(Date.UTC(year,month-1,day+1));return next.toISOString();}
}
