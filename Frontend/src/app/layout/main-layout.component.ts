import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, FooterComponent],
  template: `
    <div class="min-h-screen flex flex-col bg-slate-950 font-sans selection:bg-indigo-500/30">
      
      <app-header></app-header>

      <!-- Main Content -->
      <main class="flex-grow relative">
        <!-- Global Background Orbs -->
        <div class="fixed inset-0 pointer-events-none overflow-hidden -z-10">
          <div class="absolute top-[10%] left-[20%] w-[500px] h-[500px] bg-indigo-600/10 rounded-full blur-[100px] animate-pulse"></div>
          <div class="absolute bottom-[10%] right-[20%] w-[600px] h-[600px] bg-purple-600/10 rounded-full blur-[120px] animate-pulse" style="animation-delay: 2s;"></div>
        </div>

        <router-outlet></router-outlet>
      </main>

      <app-footer></app-footer>

    </div>
  `,
  styles: [],
})
export class MainLayoutComponent { }
