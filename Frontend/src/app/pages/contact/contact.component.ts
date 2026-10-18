import { Component, OnInit, inject, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BrandLogoComponent],
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {
    http = inject(HttpClient);
    authService = inject(AuthService);
    cdr = inject(ChangeDetectorRef);

    data = {
        name: '',
        email: '',
        subject: '',
        message: '',
        userId: null as number | null,
        browserInfo: ''
    };

    loading = false;
    successMsg = false;
    error = '';
    browserInfo = '';

    @ViewChild('successMessage') successMessage!: ElementRef;
    @ViewChild('errorSummary') errorSummary!: ElementRef;

    ngOnInit() {
        this.browserInfo = window.navigator.userAgent;
        this.data.browserInfo = this.browserInfo;

        this.authService.checkAuth().subscribe(user => {
            if (user && user.dbUser) {
                this.data.name = user.dbUser.name || '';
                this.data.email = user.dbUser.email || '';
                this.data.userId = user.dbUser.id;
            }
        });
    }

    onSubmit() {
        // 1. Sanitize Inputs
        this.data.name = (this.data.name || '').trim();
        this.data.email = (this.data.email || '').trim();
        this.data.subject = (this.data.subject || '').trim();
        this.data.message = (this.data.message || '').trim();

        // 2. Validate Empty Fields
        if (!this.data.name || !this.data.email || !this.data.subject || !this.data.message) {
            this.error = 'Please fill in all fields.';
            this.focusError();
            return;
        }

        // 3. Validate Lengths (DoS Prevention)
        if (this.data.name.length > 100) {
            this.error = 'Name is too long.';
            this.focusError();
            return;
        }
        if (this.data.email.length > 255) {
            this.error = 'Email is too long.';
            this.focusError();
            return;
        }
        if (this.data.subject.length > 200) {
            this.error = 'Subject is too long.';
            this.focusError();
            return;
        }
        if (this.data.message.length > 2000) {
            this.error = 'Message is too long.';
            this.focusError();
            return;
        }
        if (this.data.message.length < 20) {
            this.error = 'Message must be at least 20 characters long.';
            this.focusError();
            return;
        }

        // 4. Validate Email Format
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(this.data.email)) {
            this.error = 'Please enter a valid email address.';
            this.focusError();
            return;
        }

        this.loading = true;
        this.error = '';

        this.http.post(`${environment.apiUrl}/api/contact`, this.data).subscribe({
            next: () => {
                this.loading = false;
                this.successMsg = true;
                setTimeout(() => this.successMessage?.nativeElement?.focus());
            },
            error: (err) => {
                this.loading = false;
                this.error = err.error?.error || 'Failed to send message.';
                this.focusError();
            }
        });
    }

    private focusError() {
        this.cdr.detectChanges();
        if (this.errorSummary?.nativeElement) {
            this.errorSummary.nativeElement.focus();
        }
    }

    resetForm() {
        this.data.subject = '';
        this.data.message = '';
    }
}
