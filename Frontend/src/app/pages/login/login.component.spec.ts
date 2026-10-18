import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let httpClientSpy: jasmine.SpyObj<HttpClient>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login', 'checkAuth']);
    httpClientSpy = jasmine.createSpyObj('HttpClient', ['get', 'post', 'put']);
    httpClientSpy.get.and.returnValue(of({ status: 'UP' }));

    await TestBed.configureTestingModule({
      imports: [LoginComponent, FormsModule, CommonModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: HttpClient, useValue: httpClientSpy },
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reject invalid email format', () => {
    component.email = 'invalid-email';
    component.password = 'password123';
    component.login();

    expect(component.error).toMatch(/valid email/i);
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should call login with valid data', () => {
    component.email = 'test@example.com';
    component.password = 'password123';

    authServiceSpy.login.and.returnValue(of({}));
    authServiceSpy.checkAuth.and.returnValue(of({ authenticated: true, dbUser: { profile: { resumePath: 'path', targetRole: 'role' } } }));

    component.login();

    expect(authServiceSpy.login).toHaveBeenCalled();
  });

  it('should show error and not call login when fields are empty', () => {
    component.email = '';
    component.password = '';
    component.login();

    expect(component.error).toBe('Please enter both email and password.');
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should expose saved guest report summary on login', () => {
    sessionStorage.setItem('guest_interview_summary', JSON.stringify({
      outcome: 'EARLY_EXIT',
      score: 'N/A',
      duration: '00:30',
      words: 0
    }));

    component.ngOnInit();

    expect(component.guestSummary).toEqual(jasmine.objectContaining({
      outcome: 'EARLY_EXIT',
      score: 'N/A',
      duration: '00:30',
      words: 0
    }));
  });
});
