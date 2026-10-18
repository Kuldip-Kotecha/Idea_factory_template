import { Component, OnInit, inject, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, BrandLogoComponent],
    templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
    @ViewChild('errorContainer') errorContainer!: ElementRef;
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private authService = inject(AuthService);
    private http = inject(HttpClient);

    showLogoutMessage = false;
    showSessionExpiredMessage = false;
    isProcessing = false;
    systemStatus: 'CHECKING' | 'ONLINE' | 'DEGRADED' | 'OFFLINE' = 'CHECKING';

    email = '';
    password = '';
    showPassword = false;
    loading = false;
    isGoogleLoading = false;
    error = '';

    ngOnInit() {
        this.checkSystemHealth();

        // Check if returning from OAuth2 redirect with JWT token
        this.route.queryParams.subscribe(params => {
            if (params['token']) {
                this.isProcessing = true;
                this.authService.setToken(params['token']);
                this.redirectUser();
            }

            if (params['loggedOut'] === 'true') {
                this.authService.clearToken();
                this.showLogoutMessage = true;
            }

            if (params['sessionExpired'] === 'true') {
                this.authService.clearToken();
                this.showSessionExpiredMessage = true;
            }
        });
    }

    checkSystemHealth() {
        this.http.get<any>(`${environment.apiUrl}/api/health`).subscribe({
            next: (res) => {
                if (res.status === 'UP') this.systemStatus = 'ONLINE';
                else if (res.status === 'DEGRADED') this.systemStatus = 'DEGRADED';
                else this.systemStatus = 'OFFLINE';
            },
            error: () => this.systemStatus = 'OFFLINE'
        });
    }

    focusError() {
        setTimeout(() => {
            if (this.errorContainer) {
                this.errorContainer.nativeElement.focus();
            }
        }, 0);
    }

    login() {
        this.email = (this.email || '').trim();
        this.password = (this.password || '').trim();

        if (!this.email || !this.password) {
            this.error = 'Please enter both email and password.';
            this.focusError();
            return;
        }

        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(this.email)) {
            this.error = 'Please enter a valid email address.';
            this.focusError();
            return;
        }

        this.loading = true;
        this.error = '';

        this.authService.login(this.email, this.password).subscribe({
            next: () => {
                this.loading = false;
                this.redirectUser();
            },
            error: (err) => {
                this.loading = false;
                if (err.status === 403) {
                    this.error = 'Email not verified. Please check your inbox.';
                } else {
                    this.error = 'Invalid email or password.';
                }
                this.focusError();
            }
        });
    }

    loginWithGoogle() {
        this.isGoogleLoading = true;
        this.authService.loginWithGoogle();
    }

    private redirectUser() {
        this.authService.checkAuth().subscribe({
            next: () => {
                this.router.navigate(['/dashboard']);
            },
            error: () => {
                this.router.navigate(['/dashboard']);
            }
        });
    }
}
