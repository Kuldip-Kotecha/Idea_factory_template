import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FaqPageComponent } from './faq-page.component';
import { FaqService, Faq, FaqCategory } from '../../services/faq.service';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('FaqPageComponent', () => {
  let component: FaqPageComponent;
  let fixture: ComponentFixture<FaqPageComponent>;
  let faqServiceSpy: jasmine.SpyObj<FaqService>;

  const mockFaqs: Faq[] = [
    { id: 1, question: 'Q1', answer: 'A1', category: FaqCategory.TECHNICAL_HARDWARE, contextTags: [], platforms: [] },
    { id: 2, question: 'Q2', answer: 'A2', category: FaqCategory.AI_PRIVACY, contextTags: [], platforms: [] }
  ];

  beforeEach(async () => {
    faqServiceSpy = jasmine.createSpyObj('FaqService', ['getAllFaqs', 'triggerHelp']);
    faqServiceSpy.getAllFaqs.and.returnValue(of(mockFaqs));

    await TestBed.configureTestingModule({
      imports: [FaqPageComponent, FormsModule, CommonModule],
      providers: [
        { provide: FaqService, useValue: faqServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FaqPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should track by faq id', () => {
    // Cast to any to access the method before it's strictly implemented in the interface during TDD
    expect((component as any).trackByFaqId(0, mockFaqs[0])).toBe(1);
  });

  it('should track by category key', () => {
      const cat = { key: 'TEST_KEY', label: 'Test Label', icon: 'T' };
      // Cast to any to access the method before it's strictly implemented in the interface during TDD
      expect((component as any).trackByCategoryKey(0, cat)).toBe('TEST_KEY');
  });
});
