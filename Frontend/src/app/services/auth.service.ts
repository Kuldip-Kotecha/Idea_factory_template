import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap, finalize } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
    authenticated: boolean;
    id?: number;
    name?: string;
    email?: string;
    picture?: string;
    role?: string;
    isActive?: boolean;
    dbUser?: any;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly TOKEN_KEY = 'jwt_token';

    private currentUserSubject = new BehaviorSubject<User>({ authenticated: false });
    public currentUser$ = this.currentUserSubject.asObservable();

    /** Timestamp of the last successful /api/auth/user response (ms). */
    private authCheckedAt: number | null = null;

    /** Counter for active profile refreshes. */
    private activeRefreshCount = 0;
    private refreshCountSubject = new BehaviorSubject<number>(0);
    readonly activeRefreshCount$ = this.refreshCountSubject.asObservable();

    /** How long (ms) a successful auth check is considered fresh. */
    private readonly AUTH_CACHE_TTL_MS = 30_000;

    constructor(private http: HttpClient) { }

    setToken(token: string): void {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    clearToken(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        this.invalidateCache();
        this.currentUserSubject.next({ authenticated: false });
    }

    checkAuth(): Observable<User> {
        const cached = this.currentUserSubject.getValue();
        const cacheAge = this.authCheckedAt ? Date.now() - this.authCheckedAt : Infinity;

        if (cached.authenticated && cacheAge < this.AUTH_CACHE_TTL_MS) {
            return of(cached);
        }

        return this.http.get<User>(`${environment.apiUrl}/api/auth/user`, { withCredentials: true }).pipe(
            tap(user => {
                this.currentUserSubject.next(user);
                this.authCheckedAt = user.authenticated ? Date.now() : null;
            })
        );
    }

    invalidateCache(): void {
        this.authCheckedAt = null;
    }

    refreshUserProfile(): Observable<User> {
        this.invalidateCache();
        this.activeRefreshCount++;
        this.refreshCountSubject.next(this.activeRefreshCount);

        return this.http.get<User>(`${environment.apiUrl}/api/auth/user`, { withCredentials: true }).pipe(
            tap(user => {
                this.currentUserSubject.next(user);
                this.authCheckedAt = user.authenticated ? Date.now() : null;
            }),
            finalize(() => {
                this.activeRefreshCount--;
                this.refreshCountSubject.next(this.activeRefreshCount);
            })
        );
    }

    loginWithGoogle(): void {
        window.location.href = `${environment.authUrl}/oauth2/authorization/google`;
    }

    logout(): void {
        this.clearToken();
        window.location.href = `${environment.authUrl}/api/auth/logout`;
    }

    register(data: any): Observable<any> {
        return this.http.post(`${environment.apiUrl}/api/auth/register`, data);
    }

    verifyEmail(token: string): Observable<any> {
        return this.http.post(`${environment.apiUrl}/api/auth/verify`, { token });
    }

    resendVerification(email: string): Observable<any> {
        return this.http.post(`${environment.apiUrl}/api/auth/resend`, { email });
    }

    login(email: string, password: string): Observable<any> {
        return this.http.post<{ token?: string }>(`${environment.apiUrl}/api/auth/login`, { email, password }, { withCredentials: true }).pipe(
            tap(response => {
                if (response.token) {
                    this.setToken(response.token);
                }
            })
        );
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${environment.apiUrl}/api/auth/forgot-password`, { email });
    }

    validateResetToken(token: string): Observable<any> {
        return this.http.post(`${environment.apiUrl}/api/auth/validate-reset-token`, { token });
    }

    resetPassword(token: string, password: string): Observable<any> {
        return this.http.post(`${environment.apiUrl}/api/auth/reset-password`, { token, password });
    }
}
