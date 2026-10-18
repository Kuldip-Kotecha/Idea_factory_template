import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppToastComponent } from './app-toast.component';
import { NotificationService } from '../../services/notification.service';

describe('AppToastComponent', () => {
  let fixture: ComponentFixture<AppToastComponent>;
  let service: NotificationService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppToastComponent],
      providers: [NotificationService]
    }).compileComponents();

    service = TestBed.inject(NotificationService);
    fixture = TestBed.createComponent(AppToastComponent);
  });

  it('should render nothing when there is no toast', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-testid="app-toast"]')).toBeNull();
  });

  it('should render the current toast message', () => {
    service.showError('Backend unavailable');

    fixture.detectChanges();

    const toast = fixture.nativeElement.querySelector('[data-testid="app-toast"]');
    expect(toast.textContent).toContain('Backend unavailable');
    expect(toast.textContent).toContain('Error');
  });

  it('should dismiss the toast from the close button', () => {
    service.showSuccess('Saved');
    fixture.detectChanges();

    const closeButton: HTMLButtonElement = fixture.nativeElement.querySelector('[data-testid="app-toast-close"]');
    closeButton.click();
    fixture.detectChanges();

    expect(service.toast()).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-testid="app-toast"]')).toBeNull();
  });
});
