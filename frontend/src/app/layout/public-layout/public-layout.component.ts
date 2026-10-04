import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <main class="public-layout" aria-label="Área pública do Equilibra">
      <div class="public-brand" aria-label="Equilibra">Equilibra</div>
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
      align-items: center;
      padding: clamp(2rem, 8vh, 6rem) 1rem 2rem;
      background: var(--color-grey-50, #f8fafc);
      box-sizing: border-box;
    }

    .public-brand {
      color: var(--color-primary-700, #166534);
      font-size: clamp(1.75rem, 4vw, 2.5rem);
      font-weight: 700;
      letter-spacing: -0.04em;
      margin-bottom: 2rem;
    }
  `,
})
export class PublicLayoutComponent {}