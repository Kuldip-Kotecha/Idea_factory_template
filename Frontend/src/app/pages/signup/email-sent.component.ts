
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-email-sent',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './email-sent.component.html'
})
export class EmailSentComponent {
   email = '';
   resending = false;
   message = '';
   error = '';

   private route = inject(ActivatedRoute);
   private authService = inject(AuthService);

   constructor() {
       this.route.queryParams.subscribe(params => {
           this.email = params['email'] || 'your email';
       });
   }

   resend() {
       if (!this.email || this.email === 'your email') return;
       
       this.resending = true;
       this.message = '';
       this.error = '';
       
       this.authService.resendVerification(this.email).subscribe({
           next: () => {
               this.resending = false;
               this.message = 'Email resent successfully!';
           },
           error: (err) => {
               this.resending = false;
               this.error = 'Failed to resend. Please try again.';
           }
       });
   }
}
