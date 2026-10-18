import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { map, catchError, of, Observable } from 'rxjs';

export const adminGuard: CanActivateFn = (_route, _state): Observable<boolean | UrlTree> => {
    const router = inject(Router);
    const authService = inject(AuthService);

    return authService.checkAuth().pipe(
        map(user => {
            const role = user?.role || user?.dbUser?.role;
            if (user?.authenticated && (role === 'ROLE_ADMIN' || role === 'ADMIN')) {
                return true;
            }
            return router.createUrlTree(['/dashboard']);
        }),
        catchError(() => of(router.createUrlTree(['/login'])))
    );
};
