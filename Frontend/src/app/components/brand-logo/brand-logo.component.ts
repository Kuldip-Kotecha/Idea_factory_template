import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-brand-logo',
    standalone: true,
    imports: [CommonModule],
    host: {
        'class': 'inline-flex shrink-0 items-center select-none'
    },
    template: `
      <div class="inline-flex items-center gap-2.5 font-bold tracking-tight text-slate-900">
        <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600 text-white shadow-sm">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        @if (!markOnly) {
          <span class="text-lg font-bold tracking-tight text-slate-900">
            {{ title }}
          </span>
        }
      </div>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BrandLogoComponent {
    @Input() markOnly = false;
    @Input() title = 'IdeaFactory';
    @Input() alt = 'App Logo';
}
