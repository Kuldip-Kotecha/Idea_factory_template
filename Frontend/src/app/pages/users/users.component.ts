import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { AppStore } from '../../services/app.store';
import { User } from '../../models/user.model';

@Component({
    selector: 'app-users',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
    protected readonly store = inject(AppStore);
    private userService = inject(UserService);

    users = signal<User[]>([]);
    isLoading = signal<boolean>(true);
    currentPage = signal<number>(0);
    pageSize = signal<number>(10);
    totalPages = signal<number>(1);
    totalElements = signal<number>(0);
    searchQuery = '';

    statusFeedback = signal<string | null>(null);

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers(page: number = 0): void {
        this.isLoading.set(true);
        this.userService.getUsers(page, this.pageSize(), this.searchQuery).subscribe({
            next: (response) => {
                this.users.set(response.content);
                this.currentPage.set(response.number);
                this.totalPages.set(response.totalPages);
                this.totalElements.set(response.totalElements);
                this.isLoading.set(false);
            },
            error: () => {
                this.isLoading.set(false);
            }
        });
    }

    onSearch(): void {
        this.loadUsers(0);
    }

    toggleStatus(user: User): void {
        if (!user.id) return;
        const newStatus = !user.isActive;
        this.userService.updateUserStatus(user.id, newStatus).subscribe({
            next: (updated) => {
                user.isActive = updated.isActive;
                this.statusFeedback.set(`User ${user.email} status updated to ${newStatus ? 'Active' : 'Inactive'}.`);
                setTimeout(() => this.statusFeedback.set(null), 3000);
            }
        });
    }

    changeRole(user: User, newRole: string): void {
        if (!user.id) return;
        this.userService.updateUserRole(user.id, newRole).subscribe({
            next: (updated) => {
                user.role = updated.role;
                this.statusFeedback.set(`User ${user.email} role updated to ${newRole}.`);
                setTimeout(() => this.statusFeedback.set(null), 3000);
            }
        });
    }
}
