import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User, UserPageResponse } from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly apiUrl = `${environment.apiUrl}/api/users`;

    constructor(private http: HttpClient) { }

    getProfile(): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/me`);
    }

    updateProfile(data: { name?: string; profilePicture?: string }): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/profile`, data);
    }

    deleteAccount(): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/me`);
    }

    getUsers(page: number = 0, size: number = 10, search?: string): Observable<UserPageResponse> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        if (search && search.trim().length > 0) {
            params = params.set('search', search.trim());
        }
        return this.http.get<UserPageResponse>(this.apiUrl, { params });
    }

    getUserById(id: number): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/${id}`);
    }

    updateUserStatus(id: number, isActive: boolean): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/${id}/status`, { isActive });
    }

    updateUserRole(id: number, role: string): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/${id}/role`, { role });
    }
}
