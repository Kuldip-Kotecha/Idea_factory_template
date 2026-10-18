import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { map, catchError, of, Observable } from 'rxjs';

export const authGuard: CanActivateFn = (_route, _state): Observable<boolean | UrlTree> => {
    const router = inject(Router);
    const authService = inject(AuthService);

    return authService.checkAuth().pipe(
        map(user => {
            if (user?.authenticated) {
                return true;
            }

            // Server says not authenticated — decide whether to show "session expired"
            // by checking if a token was present before we cleared it.
            const hadToken = !!authService.getToken();
            authService.clearToken(); // single call — clears JWT + cache + BehaviorSubject

            return router.createUrlTree(
                ['/login'],
                hadToken ? { queryParams: { sessionExpired: 'true' } } : {}
            );
        }),
        catchError(() => {
            // Network error or 401 — treat as logged out
            const hadToken = !!authService.getToken();
            authService.clearToken();

            return of(router.createUrlTree(
                ['/login'],
                hadToken ? { queryParams: { sessionExpired: 'true' } } : {}
            ));
        })
    );
};
