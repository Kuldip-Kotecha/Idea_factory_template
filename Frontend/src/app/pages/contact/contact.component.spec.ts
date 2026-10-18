import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ContactComponent } from './contact.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

describe('ContactComponent', () => {
  let component: ContactComponent;
  let fixture: ComponentFixture<ContactComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let httpClientSpy: jasmine.SpyObj<HttpClient>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['checkAuth']);
    authServiceSpy.checkAuth.and.returnValue(of({ authenticated: false }));

    httpClientSpy = jasmine.createSpyObj('HttpClient', ['post']);

    await TestBed.configureTestingModule({
      imports: [ContactComponent, HttpClientTestingModule, FormsModule], // ContactComponent is standalone
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        // We can override HttpClient here if needed, or rely on HttpClientTestingModule
        // But since we want to spy on post call clearly without mocking backend, spy is easier.
        // Actually HttpClientTestingModule is better for "real" http interaction mocks, but spy is simpler for unit logic.
        // Let's use the spy for specific method calls if we can override it.
        // ContactComponent injects HttpClient using `inject(HttpClient)`.
        // In TestBed, we can override the provider.
        { provide: HttpClient, useValue: httpClientSpy },
        { provide: ActivatedRoute, useValue: {} }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit if validation fails (empty fields)', () => {
    component.data.name = '';
    component.data.email = '';
    component.onSubmit();
    expect(httpClientSpy.post).not.toHaveBeenCalled();
    // Assuming validation logic sets error or returns early.
    // Since we haven't implemented it yet, this test might fail (if current code submits) or pass (if current code crashes/does nothing).
    // The current code DOES submit even if empty (relying on HTML validation). So this test SHOULD fail initially.
  });

  it('should not submit if email is invalid', () => {
    component.data.name = 'Test User';
    component.data.email = 'invalid-email';
    component.data.subject = 'Test Subject';
    component.data.message = 'Test Message content that is long enough.';

    component.onSubmit();
    expect(httpClientSpy.post).not.toHaveBeenCalled();
  });

  it('should not submit if message is too long', () => {
    component.data.name = 'Test User';
    component.data.email = 'test@example.com';
    component.data.subject = 'Test Subject';
    component.data.message = 'a'.repeat(5001); // Too long

    component.onSubmit();
    expect(httpClientSpy.post).not.toHaveBeenCalled();
  });

  it('should submit if data is valid', () => {
    component.data.name = 'Test User';
    component.data.email = 'test@example.com';
    component.data.subject = 'Test Subject';
    component.data.message = 'Valid message content.';

    httpClientSpy.post.and.returnValue(of({}));

    component.onSubmit();
    expect(httpClientSpy.post).toHaveBeenCalled();
  });
});
