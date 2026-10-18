import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FaqService, Faq, FaqCategory } from '../../services/faq.service';
import { FormsModule } from '@angular/forms';
import { SecureLinksPipe } from '../../pipes/secure-links.pipe';
import { Router, RouterLink } from '@angular/router';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
  selector: 'app-faq-page',
  standalone: true,
  imports: [CommonModule, FormsModule, SecureLinksPipe, RouterLink, BrandLogoComponent],
  templateUrl: './faq-page.component.html'
})
export class FaqPageComponent implements OnInit {
    faqService = inject(FaqService);
    private router = inject(Router);
    
    faqs: Faq[] = [];
    filteredFaqs: Faq[] = [];
    searchQuery = '';
    selectedCategory: string | null = null;
    expandedFaq: Faq | null = null;

    categories = [
        { key: FaqCategory.TECHNICAL_HARDWARE, label: 'Technical & Hardware', icon: '📷' },
        { key: FaqCategory.AI_PRIVACY, label: 'AI Score & Feedback', icon: '🤖' },
        { key: FaqCategory.RESUME_PRIVACY, label: 'Resume & Privacy', icon: '🔒' },
        { key: FaqCategory.ACCOUNT_BILLING, label: 'Account & Billing', icon: '💳' }
    ];

    ngOnInit() {
        this.faqService.getAllFaqs().subscribe(data => {
            this.faqs = data;
            this.filteredFaqs = data;
        });
    }

    selectCategory(cat: string) {
        this.selectedCategory = cat;
        this.filter();
    }

    filter() {
        let temp = this.faqs;
        
        if (this.selectedCategory) {
            temp = temp.filter(f => f.category === this.selectedCategory);
        }

        if (this.searchQuery) {
            const q = this.searchQuery.toLowerCase();
            temp = temp.filter(f => 
                f.question.toLowerCase().includes(q) || 
                f.answer.toLowerCase().includes(q)
            );
        }

        this.filteredFaqs = temp;
    }

    toggleFaq(faq: Faq) {
        if (this.expandedFaq === faq) {
            this.expandedFaq = null;
        } else {
            this.expandedFaq = faq;
        }
    }

    formatCategory(cat: string) {
        return cat.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    }

    trackByFaqId(index: number, faq: Faq): number {
        return faq.id;
    }

    trackByCategoryKey(index: number, cat: { key: string }): string {
        return cat.key;
    }

    triggerContact() {
        this.router.navigate(['/contact']);
    }
}
