import { Component } from '@angular/core';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [],
  template: `
    <main class="public-layout" aria-label="Área pública do Equilibra">
      <ng-content />
    </main>
  `,
  styles: `
    :host {
      display: block;
      min-height: 100vh;
    }

    .public-layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: var(--color-grey-50, #f8fafc);
      box-sizing: border-box;
    }
  `,
})
export class PublicLayoutComponent {}