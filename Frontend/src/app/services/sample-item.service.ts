import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SampleItem } from '../models/sample-item.model';

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

@Injectable({
    providedIn: 'root'
})
export class SampleItemService {
    private readonly apiUrl = `${environment.apiUrl}/api/core/items`;

    constructor(private http: HttpClient) { }

    getItems(page: number = 0, size: number = 10, search?: string): Observable<PageResponse<SampleItem>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        if (search) {
            params = params.set('search', search);
        }
        return this.http.get<PageResponse<SampleItem>>(this.apiUrl, { params });
    }

    getItem(id: number): Observable<SampleItem> {
        return this.http.get<SampleItem>(`${this.apiUrl}/${id}`);
    }

    createItem(item: SampleItem): Observable<SampleItem> {
        return this.http.post<SampleItem>(this.apiUrl, item);
    }

    updateItem(id: number, item: SampleItem): Observable<SampleItem> {
        return this.http.put<SampleItem>(`${this.apiUrl}/${id}`, item);
    }

    deleteItem(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
