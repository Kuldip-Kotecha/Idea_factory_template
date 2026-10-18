import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppToastComponent } from './components/app-toast/app-toast.component';
import { FaqWidgetComponent } from './components/faq-widget/faq-widget.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FaqWidgetComponent, AppToastComponent],
  template: `
    <router-outlet></router-outlet>
    <app-faq-widget></app-faq-widget>
    <app-toast></app-toast>
  `,
  styles: [],
})
export class AppComponent {
  title = 'idea-factory';
}
