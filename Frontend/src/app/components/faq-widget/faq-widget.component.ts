
import { Component, OnInit, inject, effect, signal, ViewChild, ElementRef, HostListener, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { A11yModule } from '@angular/cdk/a11y';
import { FaqService, Faq, FaqCategory } from '../../services/faq.service';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { SecureLinksPipe } from '../../pipes/secure-links.pipe';
import { Router } from '@angular/router';

interface FaqViewModel extends Faq {
    formattedCategory: string;
}

@Component({
    selector: 'app-faq-widget',
    standalone: true,
    imports: [CommonModule, FormsModule, A11yModule, SecureLinksPipe],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
    <!-- Backdrop dimming overlay -->
    <div *ngIf="isOpen" (click)="togglePanel()" 
        class="fixed inset-0 bg-slate-950/60 backdrop-blur-sm z-50 transition-opacity duration-300">
    </div>

    <!-- Floating Trigger Button (hidden when open to prevent overlap on mobile) -->
    <button *ngIf="shouldShowButton && !isOpen" #triggerBtn (click)="togglePanel()"
        [attr.aria-label]="'Open Help & Support'"
        [attr.aria-expanded]="false"
        aria-controls="faq-panel"
        class="fixed bottom-4 right-4 md:bottom-6 md:right-6 w-12 h-12 md:w-14 md:h-14 bg-indigo-600 hover:bg-indigo-500 text-white rounded-full shadow-2xl flex items-center justify-center z-[60] transition-transform active:scale-95 group focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-indigo-400">
        <svg class="w-6 h-6 md:w-7 md:h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <!-- Pulse Effect if Context Triggered -->
        <span *ngIf="hasContextSuggestion" class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full animate-ping"></span>
        <span *ngIf="hasContextSuggestion" class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full border-2 border-[#0F172A]"></span>
    </button>

    <!-- Slide-out Panel -->
    <div *ngIf="isOpen" [@slideInOut]
        id="faq-panel" role="dialog" aria-modal="true" aria-label="Help & Support"
        cdkTrapFocus cdkTrapFocusAutoCapture
        class="fixed inset-y-0 right-0 w-full md:w-[400px] bg-[#1E293B] border-l border-slate-700 shadow-2xl z-[55] flex flex-col font-sans text-white">
        
        <!-- Header -->
        <div class="p-6 border-b border-slate-700 bg-[#0F172A] flex justify-between items-start">
            <div>
                <h2 class="text-xl font-bold">Help & Support</h2>
                <p class="text-sm text-slate-400">Context-aware assistance</p>
            </div>
            <button (click)="togglePanel()" aria-label="Close panel" class="text-slate-400 hover:text-white transition-colors p-1 rounded hover:bg-slate-700/50">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>

        <!-- Search / Context Bar -->
        <div class="p-4 bg-slate-800/50">
            <div class="relative">
                <input type="text" [(ngModel)]="searchQuery" (input)="onSearch()" 
                    placeholder="Search for help..." maxlength="100"
                    class="w-full bg-slate-900 border border-slate-700 rounded-lg py-2 pl-10 pr-4 text-sm text-white focus:outline-none focus:border-indigo-500">
                <svg class="w-4 h-4 absolute left-3 top-2.5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
            </div>
            
            <!-- Context Badge -->
            <div *ngIf="currentContext" class="mt-3 flex items-center gap-2">
                <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Suggested for you:</span>
                <span class="px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 text-xs border border-indigo-500/30 flex items-center gap-1">
                    {{ currentContext }}
                    <button (click)="clearContext()" class="hover:text-white">&times;</button>
                </span>
            </div>
        </div>

        <!-- Content Area -->
        <div class="flex-1 overflow-y-auto p-4 space-y-4">
            
            <!-- Support Form -->
            <div *ngIf="isReporting" class="p-4 bg-slate-800 rounded-lg border border-slate-700 animate-fade-in">
                <h3 class="font-bold text-white mb-2">Contact Support</h3>
                <textarea [(ngModel)]="reportMessage" rows="4" 
                    placeholder="Describe your issue..." maxlength="5000" aria-label="Describe your issue"
                    class="w-full bg-slate-900 border border-slate-700 rounded p-2 text-sm text-white mb-1 focus:border-indigo-500 outline-none"></textarea>
                <div class="text-xs text-slate-500 text-right mb-3">{{ reportMessage.length }}/5000</div>

                <div *ngIf="errorMessage" role="alert" aria-live="assertive" class="mb-3 p-2 bg-red-500/10 border border-red-500/50 rounded text-xs text-red-400 font-bold flex items-center gap-2">
                    <svg aria-hidden="true" class="w-4 h-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                    {{ errorMessage }}
                </div>

                <div class="flex gap-2 justify-end">
                    <button (click)="isReporting = false" class="px-3 py-1 text-slate-400 hover:text-white text-sm">Cancel</button>
                    <button (click)="submitTicket()" [disabled]="!reportMessage" class="px-3 py-1 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded text-sm font-bold">Send</button>
                </div>
            </div>

            <!-- Success Message -->
            <div *ngIf="ticketSent" class="p-4 bg-green-500/10 border border-green-500/50 rounded-lg flex items-center gap-3 animate-fade-in">
                <div class="w-8 h-8 rounded-full bg-green-500/20 flex items-center justify-center text-green-400">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                </div>
                <div>
                    <h4 class="font-bold text-green-400">Ticket Sent!</h4>
                    <p class="text-xs text-green-300/80">We'll check your logs and get back to you.</p>
                </div>
            </div>

            <!-- Panic Mode / Support Ticket Prompt -->
            <div *ngIf="showPanicButton && !isReporting && !ticketSent" class="p-4 bg-orange-500/10 border border-orange-500/50 rounded-lg mb-4 animate-fade-in">
                <h4 class="font-bold text-orange-400 mb-1 flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                    Having trouble?
                </h4>
                <p class="text-sm text-slate-300 mb-3">It looks like you're checking technical guides. Do you want to report a bug?</p>
                <button (click)="openSupportForm()" class="w-full py-2 bg-orange-600 hover:bg-orange-500 text-white text-sm font-bold rounded transition-colors">
                    Report Issue
                </button>
            </div>

            <!-- FAQ List -->
            <div *ngIf="!selectedFaq; else detailView">
                <button *ngFor="let faq of filteredFaqs; trackBy: trackByFaqId" (click)="viewFaq(faq)" type="button" [id]="'faq-item-' + faq.id"
                    class="w-full text-left p-4 bg-slate-800/50 hover:bg-slate-800 rounded-lg border border-slate-700/50 cursor-pointer transition-colors group">
                    <span class="font-medium text-slate-200 group-hover:text-indigo-400 transition-colors block">{{ faq.question }}</span>
                    <div class="flex items-center gap-2 mt-2">
                        <span class="text-xs px-1.5 py-0.5 rounded bg-slate-700 text-slate-400">{{ faq.formattedCategory }}</span>
                    </div>
                </button>
                
                <div *ngIf="filteredFaqs.length === 0" class="text-center py-10 text-slate-500">
                    No answers found. <br/> <button (click)="openSupportForm()" class="text-indigo-400 underline mt-2">Contact Support</button>
                </div>
            </div>

            <!-- Detail View -->
            <ng-template #detailView>
                <div class="animate-fade-in">
                    <button #backBtn (click)="clearSelection()" class="mb-4 text-indigo-400 hover:text-white flex items-center gap-1 text-sm font-bold">
                        &larr; Back to list
                    </button>
                    <h3 class="text-lg font-bold text-white mb-4">{{ selectedFaq?.question }}</h3>
                    <div class="prose prose-invert prose-sm max-w-none text-slate-300" [innerHTML]="selectedFaq?.answer | secureLinks"></div>
                    
                    <div class="mt-8 pt-4 border-t border-slate-700/50 flex justify-between items-center">
                        <span class="text-xs text-slate-500">Did this help?</span>
                        <div class="flex gap-2">
                            <button class="px-3 py-1 bg-slate-800 rounded text-xs hover:bg-green-900/30 hover:text-green-400 transition-colors">Yes</button>
                            <button class="px-3 py-1 bg-slate-800 rounded text-xs hover:bg-red-900/30 hover:text-red-400 transition-colors">No</button>
                        </div>
                    </div>
                </div>
            </ng-template>

        </div>
    </div>
  `,
    styles: [`
    :host { display: block; }
  `],
    animations: [
        trigger('slideInOut', [
            transition(':enter', [
                style({ transform: 'translateX(100%)' }),
                animate('300ms ease-out', style({ transform: 'translateX(0%)' }))
            ]),
            transition(':leave', [
                animate('200ms ease-in', style({ transform: 'translateX(100%)' }))
            ])
        ])
    ]
})
export class FaqWidgetComponent implements OnInit {
    private faqService = inject(FaqService);

    isOpen = false;
    faqs: FaqViewModel[] = [];
    filteredFaqs: FaqViewModel[] = [];
    selectedFaq: Faq | null = null;
    searchQuery = '';

    // Context Logic
    currentContext: string | null = null;
    hasContextSuggestion = false;

    get shouldShowButton(): boolean {
        // Hide FAQ widget button on purely immersive or authentication routes
        const hiddenRoutes = ['/login', '/signup', '/faq'];
        return !hiddenRoutes.some(route => this.router.url.includes(route));
    }

    // Panic Logic
    hardwareViewsCount = 0;
    lastHardwareViewTime = 0;
    showPanicButton = false;

    // Focus Logic
    lastFocusedId: number | null = null;
    @ViewChild('triggerBtn') triggerBtn!: ElementRef<HTMLButtonElement>;
    @ViewChild('backBtn') backBtn!: ElementRef<HTMLButtonElement>;

    constructor(public cdr: ChangeDetectorRef, private router: Router) {
        // Listen to global context triggers
        effect(() => {
            const context = this.faqService.openContext();
            if (context) {
                this.currentContext = context;
                this.hasContextSuggestion = true;
                this.cdr.markForCheck();
                this.openWithContext(context);
            }
        });
    }

    ngOnInit() {
        this.loadAll();
    }

    trackByFaqId(index: number, faq: FaqViewModel): number {
        return faq.id;
    }

    @HostListener('document:keydown.escape', ['$event'])
    handleEscape(event: Event) {
        if (this.isOpen) {
            this.togglePanel();
        }
    }

    loadAll() {
        this.faqService.getAllFaqs().subscribe(data => {
            const mapped = data.map(f => ({
                ...f,
                formattedCategory: f.category.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
            }));
            this.faqs = mapped;
            this.filteredFaqs = mapped;
            this.cdr.markForCheck();
        });
    }

    togglePanel() {
        this.isOpen = !this.isOpen;
        // Reset notification indicator when opened
        if (this.isOpen) {
            this.hasContextSuggestion = false;
        } else {
            // Restore focus to trigger button when closed
            setTimeout(() => {
                if (this.triggerBtn && this.triggerBtn.nativeElement) {
                    this.triggerBtn.nativeElement.focus();
                }
            });
        }
        this.cdr.markForCheck();
    }

    openWithContext(tag: string) {
        this.isOpen = true; // Force open
        this.faqService.getFaqsByContext(tag).subscribe(data => {
            // Prioritize context results but keep others available if searched?
            // For now, let's just filter to context
            const mapped = data.map(f => ({
                ...f,
                formattedCategory: f.category.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
            }));
            this.filteredFaqs = mapped;
            this.cdr.markForCheck();
            // Also update search query visual if needed or leave blank
        });
        this.cdr.markForCheck();
    }

    clearContext() {
        this.currentContext = null;
        this.loadAll();
        this.cdr.markForCheck();
    }

    onSearch() {
        if (!this.searchQuery) {
            this.filteredFaqs = this.faqs;
            this.cdr.markForCheck();
            return;
        }
        const q = this.searchQuery.toLowerCase();
        this.filteredFaqs = this.faqs.filter(f =>
            f.question.toLowerCase().includes(q) ||
            f.answer.toLowerCase().includes(q)
        );
        this.cdr.markForCheck();
    }

    viewFaq(faq: Faq) {
        this.selectedFaq = faq;
        this.lastFocusedId = faq.id;

        // Panic Detection Logic
        if (faq.category === FaqCategory.TECHNICAL_HARDWARE) {
            const now = Date.now();
            if (now - this.lastHardwareViewTime < 120000) { // Under 2 minutes
                this.hardwareViewsCount++;
            } else {
                this.hardwareViewsCount = 1; // Reset
            }
            this.lastHardwareViewTime = now;

            if (this.hardwareViewsCount >= 2) {
                this.showPanicButton = true;
            }
        }
        this.cdr.detectChanges(); // Force update so backBtn exists
        setTimeout(() => {
            if (this.backBtn) this.backBtn.nativeElement.focus();
        });
    }

    clearSelection() {
        this.selectedFaq = null;
        this.cdr.detectChanges(); // Force update so list exists

        if (this.lastFocusedId !== null) {
            setTimeout(() => {
                const el = document.getElementById('faq-item-' + this.lastFocusedId);
                if (el) {
                    el.focus();
                }
            });
        }
    }

    // Support Logic
    isReporting = false;
    reportMessage = '';
    ticketSent = false;
    errorMessage = '';

    openSupportForm() {
        this.isReporting = true;
        this.ticketSent = false;
        this.errorMessage = '';
        this.cdr.markForCheck();
    }

    submitTicket() {
        const message = (this.reportMessage || '').trim();

        if (!message) {
            this.errorMessage = 'Please describe your issue.';
            this.cdr.markForCheck();
            return;
        }

        if (message.length < 10) {
            this.errorMessage = 'Message must be at least 10 characters.';
            this.cdr.markForCheck();
            return;
        }

        if (message.length > 5000) {
            this.errorMessage = 'Message is too long.';
            this.cdr.markForCheck();
            return;
        }

        this.errorMessage = '';

        // Simulating API call
        this.faqService.submitSupportTicket(message, "user@current.context").subscribe(() => {
            this.isReporting = false;
            this.ticketSent = true;
            this.reportMessage = '';
            this.cdr.markForCheck();

            // Auto hide success after a few seconds? 
            // Maybe leaving it visible is fine until they close or navigate.
        });
        this.cdr.markForCheck();
    }

    // Deprecated but keeping signature if needed or removing
    openSupportModal() {
        this.openSupportForm();
    }
}
