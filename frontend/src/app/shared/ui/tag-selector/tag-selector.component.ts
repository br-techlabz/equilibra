import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { Tag } from '../../../features/tags/models/tag.models';

@Component({
  selector: 'app-tag-selector',
  standalone: true,
  imports: [CommonModule, MatChipsModule, MatFormFieldModule, MatIconModule, MatSelectModule],
  template: `
    <mat-form-field appearance="outline" class="tag-selector">
      <mat-label>Tags</mat-label>
      <mat-select multiple [value]="value" (selectionChange)="changed.emit($event.value)">
        @for(tag of tags; track tag.id) { <mat-option [value]="tag.id">{{ tag.name }}</mat-option> }
      </mat-select>
      <mat-hint *ngIf="tags.length === 0">Você ainda não possui tags. Crie uma em Tags.</mat-hint>
    </mat-form-field>
    @if(value.length > 0) { <mat-chip-set aria-label="Tags selecionadas">@for(id of value; track id){@if(tag(id); as selected){<mat-chip [removable]="true" (removed)="remove(id)" [attr.aria-label]="'Tag ' + selected.name"><span>{{ selected.name }}</span><button matChipRemove [attr.aria-label]="'Remover tag ' + selected.name"><mat-icon>cancel</mat-icon></button></mat-chip>}}</mat-chip-set> }
  `,
})
export class TagSelectorComponent {
  @Input() tags: Tag[] = [];
  @Input() value: string[] = [];
  @Output() changed = new EventEmitter<string[]>();
  tag(id: string): Tag | undefined { return this.tags.find(tag => tag.id === id); }
  remove(id: string): void { this.changed.emit(this.value.filter(value => value !== id)); }
}
