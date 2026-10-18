
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
    email = '';
    loading = false;
    successMsg = '';
    error = '';
    
    private authService = inject(AuthService);

    onSubmit() {
        if (!this.email) return;

        this.loading = true;
        this.error = '';
        this.successMsg = '';

        this.authService.forgotPassword(this.email).subscribe({
            next: (res: any) => {
                this.loading = false;
                this.successMsg = res.message || 'If an account matches that email, we have sent a reset link.';
            },
            error: (err) => {
                this.loading = false;
                this.error = 'Something went wrong. Please try again.';
            }
        });
    }
}
