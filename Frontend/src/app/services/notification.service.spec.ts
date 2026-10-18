import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NotificationService]
    });

    service = TestBed.inject(NotificationService);
  });

  afterEach(() => {
    service.clear();
  });

  it('should publish an error toast', () => {
    service.showError('Something failed');

    expect(service.toast()).toEqual({
      id: 1,
      type: 'error',
      message: 'Something failed'
    });
  });

  it('should publish a success toast', () => {
    service.showSuccess('Saved');

    expect(service.toast()).toEqual({
      id: 1,
      type: 'success',
      message: 'Saved'
    });
  });

  it('should auto-clear the active toast after the timeout', fakeAsync(() => {
    service.showError('Network issue', 2500);

    expect(service.toast()?.message).toBe('Network issue');

    tick(2499);
    expect(service.toast()?.message).toBe('Network issue');

    tick(1);
    expect(service.toast()).toBeNull();
  }));

  it('should replace a previous toast and clear only the latest one', fakeAsync(() => {
    service.showError('First', 2000);
    tick(1000);

    service.showError('Second', 2000);
    tick(1000);

    expect(service.toast()?.message).toBe('Second');

    tick(1000);
    expect(service.toast()).toBeNull();
  }));
});
