import { Injectable, signal } from '@angular/core';
import { Subscription, timer } from 'rxjs';

export type ToastType = 'success' | 'error';

export interface AppToast {
  id: number;
  type: ToastType;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  readonly toast = signal<AppToast | null>(null);

  private nextId = 1;
  private autoDismissSubscription?: Subscription;

  showSuccess(message: string, timeoutMs = 4000): void {
    this.publishToast('success', message, timeoutMs);
  }

  showError(message: string, timeoutMs = 6000): void {
    this.publishToast('error', message, timeoutMs);
  }

  clear(): void {
    this.autoDismissSubscription?.unsubscribe();
    this.autoDismissSubscription = undefined;
    this.toast.set(null);
  }

  private publishToast(type: ToastType, message: string, timeoutMs: number): void {
    this.autoDismissSubscription?.unsubscribe();
    this.toast.set({
      id: this.nextId++,
      type,
      message
    });

    if (timeoutMs > 0) {
      this.autoDismissSubscription = timer(timeoutMs).subscribe(() => {
        this.toast.set(null);
        this.autoDismissSubscription = undefined;
      });
    }
  }
}
