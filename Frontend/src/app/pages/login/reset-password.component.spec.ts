import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let routeStub: any;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['resetPassword', 'validateResetToken']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    routeStub = {
      snapshot: { queryParams: { token: 'valid-token' } }
    };

    authServiceSpy.validateResetToken.and.returnValue(of({ valid: true }));

    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent, FormsModule, CommonModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: routeStub }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reject weak passwords', () => {
    component.password = 'weak';
    component.confirmPassword = 'weak';
    component.token = 'valid-token';
    component.onSubmit();

    expect(component.error).toMatch(/at least 8 characters/i);
    expect(authServiceSpy.resetPassword).not.toHaveBeenCalled();
  });

  it('should reject passwords without uppercase', () => {
    component.password = 'password123!';
    component.confirmPassword = 'password123!';
    component.token = 'valid-token';
    component.onSubmit();

    expect(component.error).toMatch(/uppercase/i);
    expect(authServiceSpy.resetPassword).not.toHaveBeenCalled();
  });

  it('should accept strong passwords', () => {
    component.password = 'Password123!';
    component.confirmPassword = 'Password123!';
    component.token = 'valid-token';

    authServiceSpy.resetPassword.and.returnValue(of({}));

    component.onSubmit();

    expect(authServiceSpy.resetPassword).toHaveBeenCalledWith('valid-token', 'Password123!');
  });
});
