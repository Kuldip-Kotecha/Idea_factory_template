
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
  loading = true;
  success = false;
  error = '';
  
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit() {
      const token = this.route.snapshot.queryParams['token'];
      if (!token) {
          this.loading = false;
          this.error = 'No verification token found.';
          return;
      }

      this.authService.verifyEmail(token).subscribe({
          next: () => {
              this.loading = false;
              this.success = true;
          },
          error: (err) => {
              this.loading = false;
              this.error = err.error?.error || 'Verification failed. Token may be expired.';
          }
      });
  }
}
