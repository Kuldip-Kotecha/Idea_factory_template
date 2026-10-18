import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SignupComponent } from './signup.component';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute, provideRouter } from '@angular/router'; // Updated
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('SignupComponent', () => {
  let component: SignupComponent;
  let fixture: ComponentFixture<SignupComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [SignupComponent, FormsModule, CommonModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([]) // Provide Router
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reject invalid email format', () => {
    component.data.name = 'Test User';
    component.data.email = 'invalid-email'; // No @
    component.data.password = 'Password123!';
    component.onSubmit();

    // Expect error message about valid email
    expect(component.error).toMatch(/valid email/i);
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should reject weak passwords (length)', () => {
    component.data.name = 'Test User';
    component.data.email = 'test@example.com';
    component.data.password = '123'; // Too short
    component.onSubmit();

    expect(component.error).toMatch(/at least 8 characters/i);
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should reject weak passwords (complexity)', () => {
    component.data.name = 'Test User';
    component.data.email = 'test@example.com';
    component.data.password = 'Password123'; // No special char
    component.onSubmit();

    expect(component.error).toMatch(/special character/i);
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should call register with valid data', () => {
    component.data.name = 'Test User';
    component.data.email = 'test@example.com';
    component.data.password = 'Password123!';

    authServiceSpy.register.and.returnValue(of({}));

    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();
  });
});
