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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TagsApiService } from '../data-access/tags-api.service';
import { Tag, TagRequest } from '../models/tag.models';
import { ContentPanelComponent } from '../../../shared/ui/content-panel/content-panel.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';

interface TagForm { name: FormControl<string>; }

@Component({
  selector: 'app-tags-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, MatMenuModule, MatSnackBarModule, ContentPanelComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <main class="tags-page">
      <app-page-header title="Tags" subtitle="Organize suas movimentações com marcadores personalizados." [actions]="pageActions" />
      <app-content-panel title="Minhas tags" subtitle="Crie marcadores para organizar suas movimentações.">
        <div class="tags-toolbar">
          <mat-checkbox [checked]="includeInactive()" (change)="setIncludeInactive($event.checked)">Mostrar tags inativas</mat-checkbox>
          <span class="tag-count">{{ tags().length }} {{ tags().length === 1 ? 'tag' : 'tags' }}</span>
        </div>
        @if (isLoading()) { <div class="state-block" role="status"><mat-icon class="spin">sync</mat-icon><span>Carregando tags...</span></div> }
        @else if (errorMessage()) { <div class="state-block error-state" role="alert"><mat-icon>error_outline</mat-icon><p>{{ errorMessage() }}</p><button mat-stroked-button type="button" (click)="loadTags()">Tentar novamente</button></div> }
        @else if (tagsLoaded() && tags().length === 0) { <app-empty-state icon="label" iconVariant="folder" title="Nenhuma tag cadastrada" description="Crie tags para organizar suas movimentações." [centered]="false" [actions]="emptyActions" /> }
        @else if (tags().length > 0) {
          <div class="tag-list" role="list">
            @for (tag of tags(); track tag.id) {
              <article class="tag-row" role="listitem">
                <div class="tag-identity"><span class="tag-icon"><mat-icon>label</mat-icon></span><strong>{{ tag.name }}</strong></div>
                <div class="tag-status"><span class="field-label">Status</span><span class="status" [class.status-inactive]="!tag.active"><span class="status-dot"></span>{{ tag.active ? 'Ativa' : 'Inativa' }}</span></div>
                <div class="tag-updated"><span class="field-label">Atualizada em</span><span>{{ formatDate(tag.updatedAt) }}</span></div>
                <button mat-icon-button [matMenuTriggerFor]="tagMenu" [matMenuTriggerData]="{ tag }" [attr.aria-label]="'Ações da tag ' + tag.name"><mat-icon>more_vert</mat-icon></button>
              </article>
            }
          </div>
        }
      </app-content-panel>
      @if (isFormOpen()) {
        <button class="drawer-backdrop" type="button" aria-label="Fechar formulário" (click)="closeForm()"></button>
        <aside class="tag-drawer" role="dialog" aria-modal="true" aria-labelledby="tag-drawer-title">
          <header class="drawer-header"><div><span class="eyebrow">Tags</span><h2 id="tag-drawer-title">{{ editingTag() ? 'Editar tag' : 'Nova tag' }}</h2></div><button mat-icon-button type="button" aria-label="Fechar formulário" (click)="closeForm()"><mat-icon>close</mat-icon></button></header>
          <form class="tag-form" [formGroup]="form" (ngSubmit)="saveTag()" novalidate>
            <mat-form-field appearance="outline"><mat-label>Nome</mat-label><input matInput formControlName="name" placeholder="Ex.: Viagem" maxlength="100" />@if(form.controls.name.hasError('required')){<mat-error>Informe o nome da tag.</mat-error>}@if(form.controls.name.hasError('maxlength')){<mat-error>Use no máximo 100 caracteres.</mat-error>}</mat-form-field>
            @if(formError()){<p class="form-error" role="alert"><mat-icon>error_outline</mat-icon>{{ formError() }}</p>}
            <div class="drawer-actions"><button mat-button type="button" (click)="closeForm()">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || isSaving()">{{ editingTag() ? 'Salvar alterações' : 'Criar tag' }}</button></div>
          </form>
        </aside>
      }
      <mat-menu #tagMenu="matMenu"><ng-template matMenuContent let-tag="tag"><button mat-menu-item (click)="openEditForm(tag)"><mat-icon>edit</mat-icon><span>Editar</span></button>@if(tag.active){<button mat-menu-item (click)="deactivate(tag)"><mat-icon>pause_circle</mat-icon><span>Desativar</span></button>}@else{<button mat-menu-item (click)="activate(tag)"><mat-icon>play_circle</mat-icon><span>Reativar</span></button>}</ng-template></mat-menu>
    </main>
  `,
  styleUrl: './tags.page.scss',
})
export class TagsPageComponent implements OnInit {
  private readonly api = inject(TagsApiService); private readonly formBuilder = inject(NonNullableFormBuilder); private readonly snackBar = inject(MatSnackBar);
  readonly tags = signal<Tag[]>([]); readonly includeInactive = signal(false); readonly tagsLoaded = signal(false); readonly isLoading = signal(false); readonly errorMessage = signal<string | null>(null); readonly isFormOpen = signal(false); readonly editingTag = signal<Tag | null>(null); readonly isSaving = signal(false); readonly formError = signal<string | null>(null);
  readonly form: FormGroup<TagForm> = this.formBuilder.group({ name: this.formBuilder.control('', [Validators.required, Validators.maxLength(100)]) });
  readonly pageActions = [{ label: 'Nova tag', icon: 'add', handler: () => this.openCreateForm() }]; readonly emptyActions = [{ label: 'Criar primeira tag', icon: 'add', handler: () => this.openCreateForm() }];
  ngOnInit(): void { this.loadTags(); }
  loadTags(): void { this.isLoading.set(true); this.tagsLoaded.set(false); this.errorMessage.set(null); this.api.list(this.includeInactive()).subscribe({ next: value => { this.tags.set(value); this.tagsLoaded.set(true); this.isLoading.set(false); }, error: () => { this.tags.set([]); this.isLoading.set(false); this.errorMessage.set('Não foi possível carregar as tags.'); } }); }
  setIncludeInactive(value: boolean): void { this.includeInactive.set(value); this.loadTags(); }
  openCreateForm(): void { this.editingTag.set(null); this.formError.set(null); this.form.reset({ name: '' }); this.isFormOpen.set(true); }
  openEditForm(tag: Tag): void { this.editingTag.set(tag); this.formError.set(null); this.form.reset({ name: tag.name }); this.isFormOpen.set(true); }
  closeForm(): void { if (!this.isSaving()) this.isFormOpen.set(false); }
  saveTag(): void { if (this.form.invalid || this.isSaving()) { this.form.markAllAsTouched(); return; } const request: TagRequest = { name: this.form.controls.name.value.trim() }; const tag = this.editingTag(); this.isSaving.set(true); this.formError.set(null); const operation = tag ? this.api.update(tag.id, request) : this.api.create(request); operation.subscribe({ next: () => { this.isSaving.set(false); this.isFormOpen.set(false); this.snackBar.open(tag ? 'Tag atualizada.' : 'Tag criada.', 'Fechar', { duration: 3000 }); this.loadTags(); }, error: (error: HttpErrorResponse) => { this.isSaving.set(false); this.formError.set(error.status === 409 ? 'Já existe uma tag ativa com esse nome.' : 'Não foi possível concluir a operação.'); } }); }
  deactivate(tag: Tag): void { if (!window.confirm('Desativar esta tag? Ela deixará de ficar disponível para novas associações.')) return; this.api.deactivate(tag.id).subscribe({ next: () => { this.snackBar.open('Tag desativada.', 'Fechar', { duration: 3000 }); this.loadTags(); }, error: () => this.snackBar.open('Não foi possível desativar a tag.', 'Fechar', { duration: 3500 }) }); }
  activate(tag: Tag): void { this.api.activate(tag.id).subscribe({ next: () => { this.snackBar.open('Tag reativada.', 'Fechar', { duration: 3000 }); this.loadTags(); }, error: (error: HttpErrorResponse) => this.snackBar.open(error.status === 409 ? 'Já existe uma tag ativa com esse nome.' : 'Não foi possível reativar a tag.', 'Fechar', { duration: 3500 }) }); }
  formatDate(value: string): string { const date = new Date(value); return Number.isNaN(date.getTime()) ? '—' : new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(date); }
}
