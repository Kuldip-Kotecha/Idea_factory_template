import { Router, RouterModule } from '@angular/router';
import { Component, inject, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';
import { isStrongPassword } from '../../utils/validators';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BrandLogoComponent],
  templateUrl: './signup.component.html'
})
export class SignupComponent {
  data = {
      name: '',
      email: '',
      password: ''
  };
  showPassword = false;
  loading = false;
  error = '';
  
  @ViewChild('errorContainer') errorContainer!: ElementRef;

  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  focusError() {
    this.cdr.detectChanges();
    if (this.errorContainer) {
        this.errorContainer.nativeElement.focus();
    }
  }

  onSubmit() {
      // 1. Sanitize
      this.data.email = (this.data.email || '').trim();
      this.data.name = (this.data.name || '').trim();
      this.data.password = (this.data.password || '').trim();

      // 2. Validate Empty Fields
      if (!this.data.name || !this.data.email || !this.data.password) {
          this.error = 'Please fill in all fields.';
          this.focusError();
          return;
      }

      // 2.5 Validate Lengths (DoS Prevention)
      if (this.data.email.length > 255) {
          this.error = 'Email is too long.';
          this.focusError();
          return;
      }
      if (this.data.password.length > 128) {
          this.error = 'Password is too long.';
          this.focusError();
          return;
      }
      if (this.data.name.length > 100) {
          this.error = 'Name is too long.';
          this.focusError();
          return;
      }

      // 3. Validate Email Format
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailRegex.test(this.data.email)) {
          this.error = 'Please enter a valid email address.';
          this.focusError();
          return;
      }

      // 4. Validate Password Strength
      const passwordCheck = isStrongPassword(this.data.password);
      if (!passwordCheck.valid) {
          this.error = passwordCheck.message || 'Password is too weak.';
          this.focusError();
          return;
      }
      
      this.loading = true;
      this.error = '';
      
      this.authService.register(this.data).subscribe({
          next: () => {
              this.loading = false;
              // Redirect to "Limbo" screen
              this.router.navigate(['/email-sent'], { queryParams: { email: this.data.email } });
          },
          error: (err) => {
              this.loading = false;
              if (err.status === 409 && err.error?.error === 'Email already in use') {
                  this.error = 'This email is already registered.';
              } else {
                  this.error = err.error?.error || 'Registration failed. Please try again.';
              }
              this.focusError();
          }
      });
  }

  loginWithGoogle() {
    window.location.href = `${environment.authUrl}/oauth2/authorization/google`;
  }
}
