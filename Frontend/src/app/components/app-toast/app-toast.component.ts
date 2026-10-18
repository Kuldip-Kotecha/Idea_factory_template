import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      *ngIf="notification.toast() as toast"
      data-testid="app-toast"
      class="fixed right-4 top-4 z-[200] w-[calc(100vw-2rem)] max-w-sm rounded-3xl border px-5 py-4 shadow-2xl backdrop-blur-xl animate-fade-in"
      [ngClass]="{
        'border-emerald-400/30 bg-emerald-950/80 shadow-emerald-950/40': toast.type === 'success',
        'border-rose-400/30 bg-rose-950/85 shadow-rose-950/40': toast.type === 'error'
      }"
      role="status"
      aria-live="polite"
    >
      <div class="flex items-start gap-4">
        <div
          class="mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl border text-lg font-black"
          [ngClass]="{
            'border-emerald-300/20 bg-emerald-400/15 text-emerald-200': toast.type === 'success',
            'border-rose-300/20 bg-rose-400/15 text-rose-200': toast.type === 'error'
          }"
          aria-hidden="true"
        >
          {{ toast.type === 'success' ? '✓' : '!' }}
        </div>

        <div class="min-w-0 flex-1">
          <p
            class="text-xs font-bold uppercase tracking-[0.3em]"
            [ngClass]="toast.type === 'success' ? 'text-emerald-200' : 'text-rose-200'"
          >
            {{ toast.type === 'success' ? 'Success' : 'Error' }}
          </p>
          <p class="mt-2 text-sm leading-6 text-slate-100 break-words">
            {{ toast.message }}
          </p>
        </div>

        <button
          type="button"
          data-testid="app-toast-close"
          class="rounded-full p-2 text-slate-300 hover:bg-white/10 hover:text-white transition-colors"
          aria-label="Dismiss notification"
          (click)="notification.clear()"
        >
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  `
})
export class AppToastComponent {
  readonly notification = inject(NotificationService);
}
