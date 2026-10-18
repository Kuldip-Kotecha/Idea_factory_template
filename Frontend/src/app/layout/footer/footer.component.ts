import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="glass border-t border-white/5 py-6 mt-auto bg-slate-950/50">
      <div class="max-w-7xl mx-auto px-4 text-center text-slate-500 text-sm">
        &copy; 2026 Idea Factory. Built with Angular & Spring Boot.
      </div>
    </footer>
  `,
  styles: []
})
export class FooterComponent {}
