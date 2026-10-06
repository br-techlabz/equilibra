import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CategoriesApiService } from '../data-access/categories-api.service';
import {
  CATEGORY_APPLICABILITY_OPTIONS,
  Category,
  CategoryApplicability,
  CategoryRequest,
  categoryApplicabilityLabel,
} from '../models/category.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';

interface CategoryForm {
  name: FormControl<string>;
  applicability: FormControl<CategoryApplicability>;
}

type CategoryFilter = 'ALL' | 'EXPENSE' | 'INCOME';

@Component({
  selector: 'app-categories-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatSelectModule,
    MatSnackBarModule,
    ContentPanelComponent,
    EmptyStateComponent,
    PageHeaderComponent,
  ],
  template: `
    <main class="categories-page">
      <app-page-header
        title="Categorias"
        subtitle="Organize suas receitas e despesas."
        [actions]="pageActions" />

      <app-content-panel title="Categorias" subtitle="Classifique seus lançamentos com clareza.">
        <div class="categories-toolbar">
          <mat-form-field appearance="outline" class="filter-field">
            <mat-label>Aplicação</mat-label>
            <mat-select [value]="filter()" (selectionChange)="setFilter($event.value)">
              <mat-option value="ALL">Todas</mat-option>
              <mat-option value="EXPENSE">Despesas</mat-option>
              <mat-option value="INCOME">Receitas</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-checkbox [checked]="includeInactive()" (change)="setIncludeInactive($event.checked)">
            Mostrar categorias inativas
          </mat-checkbox>
          <span class="category-count">{{ categories().length }} {{ categories().length === 1 ? 'categoria' : 'categorias' }}</span>
        </div>

        @if (isLoading()) {
          <div class="state-block" role="status" aria-live="polite">
            <mat-icon class="state-icon spin" aria-hidden="true">sync</mat-icon>
            <span>Carregando categorias...</span>
          </div>
        } @else if (errorMessage()) {
          <div class="state-block error-state" role="alert">
            <mat-icon class="state-icon" aria-hidden="true">error_outline</mat-icon>
            <p>{{ errorMessage() }}</p>
            <button mat-stroked-button type="button" (click)="loadCategories()">Tentar novamente</button>
          </div>
        } @else if (categoriesLoaded() && categories().length === 0) {
          <app-empty-state
            [icon]="hasFilter() ? 'filter_alt_off' : 'category'"
            iconVariant="folder"
            [title]="hasFilter() ? 'Nenhuma categoria encontrada' : 'Nenhuma categoria cadastrada'"
            [description]="hasFilter() ? 'Não há categorias para os filtros selecionados.' : 'Crie categorias para organizar suas receitas e despesas.'"
            [centered]="false"
            [actions]="emptyActions" />
        } @else if (categories().length > 0) {
          <div class="category-list" role="list">
            @for (category of categories(); track category.id) {
              <article class="category-row" role="listitem">
                <div class="category-identity">
                  <span class="category-icon" aria-hidden="true"><mat-icon>category</mat-icon></span>
                  <div class="category-title">
                    <strong>{{ category.name }}</strong>
                    <span>{{ categoryApplicabilityLabel(category.applicability) }}</span>
                  </div>
                </div>
                <div class="category-applicability">
                  <span class="field-label">Aplicação</span>
                  <span class="applicability-badge" [attr.data-applicability]="category.applicability">
                    {{ categoryApplicabilityLabel(category.applicability) }}
                  </span>
                </div>
                <div class="category-status">
                  <span class="field-label">Status</span>
                  <span class="status" [class.status-inactive]="!category.active">
                    <span class="status-dot" aria-hidden="true"></span>{{ category.active ? 'Ativa' : 'Inativa' }}
                  </span>
                </div>
                <div class="category-updated">
                  <span class="field-label">Atualizada em</span>
                  <span>{{ formatDate(category.updatedAt) }}</span>
                </div>
                <button
                  mat-icon-button
                  class="category-actions"
                  [matMenuTriggerFor]="categoryMenu"
                  [matMenuTriggerData]="{ category: category }"
                  [attr.aria-label]="'Ações da categoria ' + category.name">
                  <mat-icon>more_vert</mat-icon>
                </button>
              </article>
            }
          </div>
        }
      </app-content-panel>

      @if (isFormOpen()) {
        <button class="drawer-backdrop" type="button" aria-label="Fechar formulário" (click)="closeForm()"></button>
        <aside class="category-drawer" role="dialog" aria-modal="true" aria-labelledby="category-drawer-title">
          <header class="drawer-header">
            <div>
              <span class="eyebrow">Categorias</span>
              <h2 id="category-drawer-title">{{ editingCategory() ? 'Editar categoria' : 'Nova categoria' }}</h2>
            </div>
            <button mat-icon-button type="button" aria-label="Fechar formulário" (click)="closeForm()"><mat-icon>close</mat-icon></button>
          </header>
          <form class="category-form" [formGroup]="form" (ngSubmit)="saveCategory()" novalidate>
            <mat-form-field appearance="outline">
              <mat-label>Nome da categoria</mat-label>
              <input matInput formControlName="name" placeholder="Ex.: Alimentação" />
              <mat-hint>Use um nome claro para seus lançamentos.</mat-hint>
              @if (form.controls.name.hasError('required')) { <mat-error>Informe o nome da categoria.</mat-error> }
              @if (form.controls.name.hasError('maxlength')) { <mat-error>Use no máximo 100 caracteres.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Usar esta categoria em</mat-label>
              <mat-select formControlName="applicability">
                @for (option of applicabilityOptions; track option.value) {
                  <mat-option [value]="option.value">{{ option.label }}</mat-option>
                }
              </mat-select>
              <mat-hint>Define em quais tipos de lançamento ela poderá ser usada.</mat-hint>
              @if (form.controls.applicability.hasError('required')) { <mat-error>Escolha uma aplicação.</mat-error> }
            </mat-form-field>
            @if (formError()) { <p class="form-error" role="alert"><mat-icon aria-hidden="true">error_outline</mat-icon>{{ formError() }}</p> }
            <div class="drawer-actions">
              <button mat-button type="button" (click)="closeForm()">Cancelar</button>
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSaving()">{{ editingCategory() ? 'Salvar alterações' : 'Salvar categoria' }}</button>
            </div>
          </form>
        </aside>
      }

      <mat-menu #categoryMenu="matMenu">
        <ng-template matMenuContent let-category="category">
          <button mat-menu-item (click)="openEditForm(category)"><mat-icon>edit</mat-icon><span>Editar</span></button>
          @if (category.active) {
            <button mat-menu-item (click)="deactivate(category)"><mat-icon>pause_circle</mat-icon><span>Desativar</span></button>
          } @else {
            <button mat-menu-item (click)="activate(category)"><mat-icon>play_circle</mat-icon><span>Reativar</span></button>
          }
        </ng-template>
      </mat-menu>
    </main>
  `,
  styleUrl: './categories.page.scss',
})
export class CategoriesPageComponent implements OnInit {
  private readonly api = inject(CategoriesApiService);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly categories = signal<Category[]>([]);
  readonly filter = signal<CategoryFilter>('ALL');
  readonly includeInactive = signal(false);
  readonly categoriesLoaded = signal(false);
  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly isFormOpen = signal(false);
  readonly editingCategory = signal<Category | null>(null);
  readonly isSaving = signal(false);
  readonly formError = signal<string | null>(null);
  readonly applicabilityOptions = CATEGORY_APPLICABILITY_OPTIONS;

  readonly form: FormGroup<CategoryForm> = this.formBuilder.group({
    name: this.formBuilder.control('', [Validators.required, Validators.maxLength(100)]),
    applicability: this.formBuilder.control<CategoryApplicability>('EXPENSE', Validators.required),
  });

  readonly pageActions = [{ label: 'Nova categoria', icon: 'add', handler: () => this.openCreateForm() }];
  readonly emptyActions = [{ label: 'Nova categoria', icon: 'add', handler: () => this.openCreateForm() }];

  ngOnInit(): void { this.loadCategories(); }

  loadCategories(): void {
    this.isLoading.set(true);
    this.categoriesLoaded.set(false);
    this.errorMessage.set(null);
    const selectedFilter = this.filter();
    const applicability: CategoryApplicability | undefined = selectedFilter === 'EXPENSE' || selectedFilter === 'INCOME'
      ? selectedFilter
      : undefined;
    this.api.list(this.includeInactive(), applicability).subscribe({
      next: (categories) => { this.categories.set(categories); this.categoriesLoaded.set(true); this.isLoading.set(false); },
      error: () => { this.categories.set([]); this.isLoading.set(false); this.errorMessage.set('Não foi possível carregar suas categorias.'); },
    });
  }

  setFilter(value: CategoryFilter): void { this.filter.set(value); this.loadCategories(); }
  setIncludeInactive(value: boolean): void { this.includeInactive.set(value); this.loadCategories(); }
  hasFilter(): boolean { return this.filter() !== 'ALL'; }
  categoryApplicabilityLabel(value: CategoryApplicability): string { return categoryApplicabilityLabel(value); }
  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '—' : new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  openCreateForm(): void {
    this.editingCategory.set(null); this.formError.set(null);
    this.form.reset({ name: '', applicability: 'EXPENSE' }); this.isFormOpen.set(true);
  }

  openEditForm(category: Category): void {
    this.editingCategory.set(category); this.formError.set(null);
    this.form.reset({ name: category.name, applicability: category.applicability }); this.isFormOpen.set(true);
  }

  closeForm(): void { if (!this.isSaving()) this.isFormOpen.set(false); }

  saveCategory(): void {
    if (this.form.invalid || this.isSaving()) { this.form.markAllAsTouched(); return; }
    const values = this.form.getRawValue();
    const request: CategoryRequest = { name: values.name, applicability: values.applicability };
    const current = this.editingCategory();
    const operation = current ? this.api.update(current.id, request) : this.api.create(request);
    this.isSaving.set(true); this.formError.set(null);
    operation.subscribe({
      next: () => { this.isSaving.set(false); this.isFormOpen.set(false); this.snackBar.open(current ? 'Categoria atualizada com sucesso.' : 'Categoria criada com sucesso.', 'Fechar', { duration: 3500 }); this.loadCategories(); },
      error: (error: HttpErrorResponse) => { this.isSaving.set(false); this.formError.set(error.status === 409 ? 'Já existe uma categoria ativa com esse nome.' : 'Não foi possível concluir a operação. Tente novamente.'); },
    });
  }

  deactivate(category: Category): void {
    if (!window.confirm('A categoria deixará de estar disponível para novos lançamentos, mas permanecerá no histórico.')) return;
    this.api.deactivate(category.id).subscribe({
      next: () => { this.snackBar.open('Categoria desativada.', 'Fechar', { duration: 3000 }); this.loadCategories(); },
      error: () => this.snackBar.open('Não foi possível desativar a categoria.', 'Fechar', { duration: 3500 }),
    });
  }

  activate(category: Category): void {
    this.api.activate(category.id).subscribe({
      next: () => { this.snackBar.open('Categoria reativada.', 'Fechar', { duration: 3000 }); this.loadCategories(); },
      error: (error: HttpErrorResponse) => this.snackBar.open(error.status === 409 ? 'Não é possível reativar: já existe uma categoria ativa com o mesmo nome.' : 'Não foi possível reativar a categoria.', 'Fechar', { duration: 4000 }),
    });
  }
}
