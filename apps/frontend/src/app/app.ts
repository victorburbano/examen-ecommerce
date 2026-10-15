import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from './shared/ui/header/header';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header],
  template: `
    <app-header />
    <main>
      <router-outlet />
    </main>
  `,
  styles: `
    main {
      display: block;
      background: #f8fafc;
      min-height: calc(100vh - 3.25rem);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
