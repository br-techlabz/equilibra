import { Component } from '@angular/core';
import { ShellComponent } from './layout/shell/shell.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ShellComponent],
  template: '<app-shell />',
  styles: [],
})
export class AppComponent {
  title = 'Equilibra';
}