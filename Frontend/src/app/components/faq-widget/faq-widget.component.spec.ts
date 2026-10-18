import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FaqWidgetComponent } from './faq-widget.component';
import { FaqService, FaqCategory } from '../../services/faq.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { signal } from '@angular/core';

class MockFaqService {
  openContext = signal<string | null>(null);
  getAllFaqs() {
    return of([
      {
        id: 1,
        question: 'Test Question',
        answer: 'Test Answer',
        category: FaqCategory.ACCOUNT_BILLING,
        contextTags: [],
        platforms: []
      }
    ]);
  }
  getFaqsByContext(tag: string) {
    return of([]);
  }
  submitSupportTicket(issue: string, email: string) {
    return of({ success: true });
  }
}

describe('FaqWidgetComponent', () => {
  let component: FaqWidgetComponent;
  let fixture: ComponentFixture<FaqWidgetComponent>;
  let faqService: FaqService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FaqWidgetComponent, HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        { provide: FaqService, useClass: MockFaqService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FaqWidgetComponent);
    component = fixture.componentInstance;
    faqService = TestBed.inject(FaqService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open panel when togglePanel is called', () => {
    component.togglePanel();
    fixture.detectChanges();
    expect(component.isOpen).toBeTrue();
    const panel = fixture.debugElement.query(By.css('#faq-panel'));
    expect(panel).toBeTruthy();
  });

  it('should close panel on ESC key press', () => {
    component.isOpen = true;
    fixture.detectChanges();

    const event = new KeyboardEvent('keydown', { key: 'Escape' });
    document.dispatchEvent(event);
    fixture.detectChanges();

    expect(component.isOpen).toBeFalse();
  });

  it('should have a close button inside the panel', () => {
    component.isOpen = true;
    component.cdr.markForCheck();
    fixture.detectChanges();

    // Look for a button with aria-label="Close panel" inside the panel
    const closeButton = fixture.debugElement.query(By.css('#faq-panel button[aria-label="Close panel"]'));
    expect(closeButton).toBeTruthy();

    closeButton.nativeElement.click();
    fixture.detectChanges();
    expect(component.isOpen).toBeFalse();
  });

  it('should validate empty message', () => {
    component.isReporting = true;
    component.reportMessage = '   '; // Whitespace
    component.submitTicket();
    expect(component.errorMessage).toBe('Please describe your issue.');
    expect(component.ticketSent).toBeFalse();
  });

  it('should validate short message', () => {
    component.isReporting = true;
    component.reportMessage = 'Too short';
    component.submitTicket();
    expect(component.errorMessage).toBe('Message must be at least 10 characters.');
    expect(component.ticketSent).toBeFalse();
  });

  it('should validate long message', () => {
    component.isReporting = true;
    component.reportMessage = 'a'.repeat(5001);
    component.submitTicket();
    expect(component.errorMessage).toBe('Message is too long.');
    expect(component.ticketSent).toBeFalse();
  });

  it('should submit valid message', () => {
    spyOn(faqService, 'submitSupportTicket').and.callThrough();
    component.isReporting = true;
    component.reportMessage = 'This is a valid support request message.';
    component.submitTicket();

    expect(component.errorMessage).toBe('');
    expect(faqService.submitSupportTicket).toHaveBeenCalled();
    expect(component.ticketSent).toBeTrue();
    expect(component.isReporting).toBeFalse();
  });

  it('should render faq items as accessible buttons', () => {
    // Open panel to see list
    component.togglePanel();
    fixture.detectChanges();

    // Verify list is rendered (exclude trigger button by checking for type="button")
    const buttons = fixture.debugElement.queryAll(By.css('button[type="button"].group'));
    expect(buttons.length).toBeGreaterThan(0);

    const firstBtn = buttons[0];
    // Check it is a button
    expect(firstBtn.nativeElement.tagName).toBe('BUTTON');
    expect(firstBtn.nativeElement.getAttribute('type')).toBe('button');

    // Check click works
    firstBtn.nativeElement.click();
    fixture.detectChanges();
    expect(component.selectedFaq).toBeTruthy();
    expect(component.selectedFaq?.question).toBe('Test Question');
  });

  it('should move focus to back button when viewing an FAQ', fakeAsync(() => {
    component.togglePanel();
    fixture.detectChanges();

    // Find list item
    const buttons = fixture.debugElement.queryAll(By.css('button[type="button"].group'));
    expect(buttons.length).toBeGreaterThan(0);

    // Click it
    buttons[0].nativeElement.click();
    fixture.detectChanges();
    tick(); // Wait for setTimeout in viewFaq

    // Verify detail view is open
    expect(component.selectedFaq).toBeTruthy();

    // Verify focus
    const backBtn = fixture.debugElement.query(By.css('button.text-indigo-400')); // The back button has specific classes
    expect(backBtn).toBeTruthy();
    expect(document.activeElement).toBe(backBtn.nativeElement);
  }));

  it('should restore focus to list item when clearing selection', fakeAsync(() => {
    // Setup: Open panel, Click item
    component.togglePanel();
    fixture.detectChanges();
    const buttons = fixture.debugElement.queryAll(By.css('button[type="button"].group'));
    const firstBtn = buttons[0];
    const firstBtnId = firstBtn.nativeElement.id;

    expect(firstBtnId).toContain('faq-item-');

    // Click to view
    firstBtn.nativeElement.click();
    fixture.detectChanges();
    tick();

    // Click back (Clear selection)
    component.clearSelection();
    fixture.detectChanges();
    tick(); // Wait for setTimeout in clearSelection

    // Verify focus was restored to the recreated element with the same ID
    const restoredBtn = fixture.debugElement.query(By.css('#' + firstBtnId));
    expect(restoredBtn).toBeTruthy();
    expect(document.activeElement).toBe(restoredBtn.nativeElement);
  }));

  it('should display error message when validation fails', () => {
    component.isOpen = true; // Panel must be open
    component.isReporting = true;
    fixture.detectChanges();

    component.reportMessage = 'Too short';
    component.submitTicket();
    fixture.detectChanges();

    const errorAlert = fixture.debugElement.query(By.css('[role="alert"]'));
    expect(errorAlert).toBeTruthy();
    expect(errorAlert.nativeElement.textContent).toContain('Message must be at least 10 characters');
  });
});
