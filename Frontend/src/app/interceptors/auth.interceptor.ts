import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Attach Bearer token if available
  const clonedRequest = token
    ? req.clone({
        headers: req.headers
          .set('Authorization', `Bearer ${token}`)
          .set('X-Requested-With', 'XMLHttpRequest')
      })
    : req.clone({
        headers: req.headers.set('X-Requested-With', 'XMLHttpRequest')
      });

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        const publicPaths = [
          '/api/auth/login', 
          '/api/auth/register', 
          '/api/auth/verify', 
          '/api/auth/forgot-password', 
          '/api/auth/reset-password',
          '/api/ai/smoke-check',
          '/api/core/health'
        ];
        const isPublicRequest = publicPaths.some(p => req.url.includes(p));

        if (!isPublicRequest) {
          // Clear token + cache + BehaviorSubject in one call
          authService.clearToken();
          if (!router.url.startsWith('/login')) {
            router.navigate(['/login'], { queryParams: { sessionExpired: 'true' } });
          }
        } else {
          authService.invalidateCache();
        }
      }
      return throwError(() => error);
    })
  );
};
