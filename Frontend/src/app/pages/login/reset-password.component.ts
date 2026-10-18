
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { isStrongPassword } from '../../utils/validators';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {
    token = '';
    password = '';
    confirmPassword = '';
    
    validatingToken = true;
    tokenValid = false;
    loading = false;
    success = false;
    error = '';
    showPassword = false;

    private route = inject(ActivatedRoute);
    private authService = inject(AuthService);
    private router = inject(Router);

    ngOnInit() {
        this.token = this.route.snapshot.queryParams['token'];
        if (!this.token) {
            this.validatingToken = false;
            this.error = 'No reset token provided';
            return;
        }

        // Stage B: Validate Token Immediately
        this.authService.validateResetToken(this.token).subscribe({
            next: (res: any) => {
                this.validatingToken = false;
                if (res.valid) {
                    this.tokenValid = true;
                } else {
                    this.tokenValid = false;
                    this.error = res.error || 'This link has expired';
                }
            },
            error: () => {
                this.validatingToken = false;
                this.tokenValid = false;
                this.error = 'Unable to verify link. Please try again.';
            }
        });
    }

    onSubmit() {
        if (this.password !== this.confirmPassword || !this.token) return;

        const passwordCheck = isStrongPassword(this.password);
        if (!passwordCheck.valid) {
            this.error = passwordCheck.message || 'Password is too weak.';
            return;
        }

        this.loading = true;
        this.error = '';

        this.authService.resetPassword(this.token, this.password).subscribe({
            next: () => {
                this.loading = false;
                this.success = true;
            },
            error: (err) => {
                this.loading = false;
                this.error = err.error?.error || 'Failed to reset password.';
            }
        });
    }
}
