import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

export enum FaqCategory {
    TECHNICAL_HARDWARE = 'TECHNICAL_HARDWARE',
    AI_PRIVACY = 'AI_PRIVACY',
    RESUME_PRIVACY = 'RESUME_PRIVACY',
    ACCOUNT_BILLING = 'ACCOUNT_BILLING'
}

export interface Faq {
    id: number;
    question: string;
    answer: string;
    category: FaqCategory;
    contextTags: string[];
    platforms: string[];
}

@Injectable({
  providedIn: 'root'
})
export class FaqService {
    private http = inject(HttpClient);
    
    // Global signal to trigger the FAQ widget from anywhere
    // If null, widget is closed/default. If string, it's opening for specific context.
    openContext = signal<string | null>(null);

    getAllFaqs(category?: string): Observable<Faq[]> {
        let url = `${environment.apiUrl}/api/faqs`;
        if (category) url += `?category=${category}`;
        return this.http.get<Faq[]>(url);
    }

    getFaqsByContext(tag: string): Observable<Faq[]> {
        return this.http.get<Faq[]>(`${environment.apiUrl}/api/faqs/context/${tag}`);
    }

    submitSupportTicket(issue: string, email: string): Observable<any> {
        const browser = window.navigator.userAgent;
        return this.http.post(`${environment.apiUrl}/api/faqs/support/ticket`, { issue, email, browser });
    }

    // Helper to open the widget
    triggerHelp(contextTag: string) {
        console.log('FaqService: Triggering help for', contextTag);
        this.openContext.set(contextTag);
    }
}
