import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { AppStore } from '../../services/app.store';
import { User } from '../../models/user.model';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
    protected readonly store = inject(AppStore);
    private userService = inject(UserService);
    private authService = inject(AuthService);
    private router = inject(Router);

    user = signal<User | null>(null);
    isLoading = signal<boolean>(true);
    isSaving = signal<boolean>(false);
    isDeleting = signal<boolean>(false);
    showDeleteModal = signal<boolean>(false);

    // Form model
    name = '';
    profilePicture = '';

    // Feedback
    successMessage = signal<string | null>(null);
    errorMessage = signal<string | null>(null);

    ngOnInit(): void {
        this.loadProfile();
    }

    loadProfile(): void {
        this.isLoading.set(true);
        this.userService.getProfile().subscribe({
            next: (data) => {
                this.user.set(data);
                this.name = data.name || '';
                this.profilePicture = data.profilePicture || '';
                this.isLoading.set(false);
            },
            error: (err) => {
                this.errorMessage.set(err?.error?.error || 'Failed to load profile');
                this.isLoading.set(false);
            }
        });
    }

    saveProfile(): void {
        this.isSaving.set(true);
        this.successMessage.set(null);
        this.errorMessage.set(null);

        this.userService.updateProfile({
            name: this.name,
            profilePicture: this.profilePicture
        }).subscribe({
            next: (updated) => {
                this.user.set(updated);
                this.successMessage.set('Profile updated successfully');
                this.isSaving.set(false);
                this.authService.refreshUserProfile().subscribe();
            },
            error: (err) => {
                this.errorMessage.set(err?.error?.error || 'Failed to update profile');
                this.isSaving.set(false);
            }
        });
    }

    confirmDeleteAccount(): void {
        this.isDeleting.set(true);
        this.userService.deleteAccount().subscribe({
            next: () => {
                this.authService.logout();
            },
            error: (err) => {
                this.errorMessage.set(err?.error?.error || 'Failed to delete account');
                this.isDeleting.set(false);
                this.showDeleteModal.set(false);
            }
        });
    }
}
