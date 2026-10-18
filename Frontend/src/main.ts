import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { injectSpeedInsights } from '@vercel/speed-insights';

// Defer non-critical initialization to after page interactive
if (typeof window !== 'undefined') {
  // Initialize Vercel Speed Insights after DOMContentLoaded for better LCP
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      injectSpeedInsights();
    });
  } else {
    injectSpeedInsights();
  }
}

bootstrapApplication(AppComponent, appConfig)
    .catch((err) => console.error(err));
