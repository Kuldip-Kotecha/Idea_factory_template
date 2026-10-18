/**
 * AppStore — lightweight signal-based reactive state layer.
 */
import { Injectable, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService, User } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AppStore {
    private authService = inject(AuthService);

    /**
     * Signal mirror of AuthService.currentUser$.
     */
    readonly currentUser = toSignal(this.authService.currentUser$, {
        initialValue: { authenticated: false } as User
    });

    /** `true` once the server confirms a valid session. */
    readonly isAuthenticated = computed(() => this.currentUser().authenticated === true);

    /** Display name or empty string */
    readonly userName = computed(() => this.currentUser().name ?? '');

    /** First name only — safe for greetings */
    readonly userFirstName = computed(() => {
        const name = this.userName();
        return name ? name.split(' ')[0] : 'User';
    });

    /** User's email address */
    readonly userEmail = computed(() => this.currentUser().email ?? '');

    /** Profile picture URL or null */
    readonly userPicture = computed(() => this.currentUser().picture ?? null);

    /** Avatar initial letter fallback */
    readonly userInitial = computed(() => this.userName().charAt(0).toUpperCase() || 'U');

    /** User's role */
    readonly userRole = computed(() => this.currentUser().role ?? this.currentUser().dbUser?.role ?? 'ROLE_USER');

    /** Whether the current user has ADMIN privileges */
    readonly isAdmin = computed(() => this.userRole() === 'ROLE_ADMIN' || this.userRole() === 'ADMIN');

    /** Raw database user entity if present */
    readonly dbUser = computed(() => this.currentUser().dbUser ?? null);
}
