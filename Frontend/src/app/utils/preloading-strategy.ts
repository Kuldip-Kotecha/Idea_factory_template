import { Injectable } from '@angular/core';
import { PreloadingStrategy, Route } from '@angular/router';
import { Observable, of } from 'rxjs';
import { timer } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class OptimizedPreloadingStrategy implements PreloadingStrategy {
  
  // Routes that should be preloaded immediately (high priority)
  private highPriorityRoutes = ['dashboard', 'onboarding'];
  
  // Routes that should be preloaded after 2 seconds (medium priority)
  private mediumPriorityRoutes = ['profile', 'progress'];
  
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    if (!route.path) {
      return of(null);
    }

    const path = route.path.toString();

    // High priority: preload immediately
    if (this.highPriorityRoutes.includes(path)) {
      return load();
    }

    // Medium priority: preload after 2 seconds of idle time
    if (this.mediumPriorityRoutes.includes(path)) {
      return timer(2000).pipe(
        mergeMap(() => load())
      );
    }

    // Low priority: don't preload (user triggered only)
    return of(null);
  }
}
