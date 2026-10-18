import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AppStore } from '../../services/app.store';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
    selector: 'app-header',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, BrandLogoComponent],
    template: `
    <nav class="sticky top-0 z-50 glass border-b border-white/5 bg-slate-950/80 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">

          <div class="flex-shrink-0">
            <a [routerLink]="store.isAuthenticated() ? '/dashboard' : '/'" class="group inline-flex items-center">
              <app-brand-logo title="IdeaFactory" class="transition-transform duration-200 group-hover:scale-[1.02]"></app-brand-logo>
            </a>
          </div>

          <div *ngIf="store.isAuthenticated()" class="hidden md:block">
            <div class="ml-10 flex items-baseline space-x-2">
              <a
                routerLink="/dashboard"
                routerLinkActive="bg-white/10 text-white"
                class="px-3 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-white/5 transition-all"
              >
                Dashboard
              </a>
              @if (store.isAdmin()) {
                <a
                  routerLink="/users"
                  routerLinkActive="bg-white/10 text-white"
                  class="px-3 py-2 rounded-lg text-sm font-medium text-slate-300 hover:text-white hover:bg-white/5 transition-all flex items-center gap-1.5"
                >
                  <span class="w-1.5 h-1.5 rounded-full bg-indigo-400"></span>
                  Users
                </a>
              }
            </div>
          </div>

          <div *ngIf="store.isAuthenticated()" class="relative ml-4">
            <div (click)="toggleProfile()" class="flex items-center gap-3 cursor-pointer group">
              <div *ngIf="store.userName()" class="hidden sm:block text-right">
                <div class="text-sm font-medium text-white group-hover:text-indigo-400 transition-colors">{{ store.userName() }}</div>
                <div class="text-xs text-slate-400">{{ store.userRole() }}</div>
              </div>

              <div *ngIf="store.userPicture(); else avatarPlaceholder" class="relative">
                <img [src]="store.userPicture()" alt="Profile" class="w-9 h-9 rounded-full border border-white/20 shadow-lg group-hover:ring-2 group-hover:ring-indigo-500/50 transition-all">
              </div>

              <ng-template #avatarPlaceholder>
                <div class="relative w-9 h-9 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-500 border border-white/20 shadow-lg group-hover:ring-2 group-hover:ring-indigo-500/50 transition-all flex items-center justify-center text-xs font-bold text-white">
                  {{ store.userInitial() }}
                </div>
              </ng-template>
            </div>

            <div *ngIf="isProfileOpen" class="absolute right-0 mt-2 w-52 bg-slate-900 border border-white/10 rounded-xl shadow-2xl py-1 transform transition-all animate-fade-in origin-top-right z-50">
              <div class="px-4 py-3 border-b border-white/5">
                <p class="text-sm text-white font-medium truncate">{{ store.userName() || 'Account' }}</p>
                <p class="text-xs text-slate-400 truncate">{{ store.userEmail() }}</p>
                <span class="mt-1 inline-block text-[10px] font-mono font-semibold px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  {{ store.userRole() }}
                </span>
              </div>

              <a routerLink="/dashboard" (click)="isProfileOpen = false" class="block px-4 py-2 text-sm text-slate-300 hover:bg-white/5 hover:text-white transition-colors cursor-pointer">
                Dashboard
              </a>
              <a routerLink="/profile" (click)="isProfileOpen = false" class="block px-4 py-2 text-sm text-slate-300 hover:bg-white/5 hover:text-white transition-colors cursor-pointer">
                Profile Settings
              </a>
              @if (store.isAdmin()) {
                <a routerLink="/users" (click)="isProfileOpen = false" class="block px-4 py-2 text-sm text-slate-300 hover:bg-white/5 hover:text-white transition-colors cursor-pointer">
                  User Management
                </a>
              }
              <div class="border-t border-white/5 my-1"></div>
              <button (click)="logout()" class="w-full text-left px-4 py-2 text-sm text-red-400 hover:bg-white/5 hover:text-red-300 transition-colors">
                Sign out
              </button>
            </div>

            <div *ngIf="isProfileOpen" (click)="isProfileOpen = false" class="fixed inset-0 z-40 cursor-default"></div>
          </div>

          <div *ngIf="!store.isAuthenticated()" class="relative ml-4 flex items-center gap-3">
            <a routerLink="/login" class="px-3 md:px-4 py-1.5 md:py-2 rounded-lg text-slate-300 hover:text-white text-xs md:text-sm font-semibold transition-colors">Sign In</a>
            <a routerLink="/signup" class="px-3 md:px-4 py-1.5 md:py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs md:text-sm font-semibold shadow-sm transition-colors">Get Started</a>
          </div>
        </div>
      </div>
    </nav>
  `,
    styles: [`
    .glass {
        background: rgba(15, 23, 42, 0.7);
        backdrop-filter: blur(10px);
    }
  `]
})
export class HeaderComponent {
    protected readonly store = inject(AppStore);
    private readonly authService = inject(AuthService);

    isProfileOpen = false;

    toggleProfile(): void {
        this.isProfileOpen = !this.isProfileOpen;
    }

    logout(): void {
        this.isProfileOpen = false;
        this.authService.logout();
    }
}
