import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let httpClientSpy: jasmine.SpyObj<HttpClient>;

  beforeEach(async () => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['getProfile', 'uploadResume', 'scrapeJob', 'updateProfile', 'deleteResume', 'downloadResumeBlob', 'deleteAccount']);
    authServiceSpy = jasmine.createSpyObj('AuthService', [], { currentUser$: of({ name: 'Test User' }) });
    httpClientSpy = jasmine.createSpyObj('HttpClient', ['get', 'post', 'put', 'delete']);

    userServiceSpy.getProfile.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [ProfileComponent, FormsModule, CommonModule],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: HttpClient, useValue: httpClientSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate file size (valid)', () => {
    const file = new File(['a'.repeat(1024)], 'test.pdf', { type: 'application/pdf' });
    expect(component.validateFile(file)).toBeTrue();
    expect(component.resumeError).toBeNull();
  });

  it('should validate file size (invalid)', () => {
    const file = new File([''], 'huge.pdf', { type: 'application/pdf' });
    // Mock size property since it's read-only and difficult to set via constructor for fake huge files without memory issues
    Object.defineProperty(file, 'size', { value: 11 * 1024 * 1024 });

    expect(component.validateFile(file)).toBeFalse();
    expect(component.resumeError).toContain('too large');
  });

  it('should validate file type (valid)', () => {
    const file = new File(['content'], 'test.docx', { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });
    expect(component.validateFile(file)).toBeTrue();
  });

  it('should validate file type (invalid)', () => {
    const file = new File(['content'], 'test.exe', { type: 'application/x-msdownload' });
    expect(component.validateFile(file)).toBeFalse();
    expect(component.resumeError).toContain('Invalid file type');
  });

  it('should call uploadResume when validation passes', () => {
    const file = new File(['content'], 'valid.pdf', { type: 'application/pdf' });
    spyOn(component, 'validateFile').and.callThrough();
    spyOn(component, 'uploadResume').and.returnValue(of({}).subscribe() as any); // Stub it out

    // Mock event
    const event = { target: { files: [file] } };
    component.onFileSelected(event as any);

    expect(component.validateFile).toHaveBeenCalledWith(file);
    expect(component.uploadResume).toHaveBeenCalledWith(file);
  });

  it('should NOT call uploadResume when validation fails', () => {
    const file = new File(['content'], 'invalid.exe', { type: 'application/x-msdownload' });
    spyOn(component, 'validateFile').and.callThrough();
    spyOn(component, 'uploadResume');

    const event = { target: { files: [file] } };
    component.onFileSelected(event as any);

    expect(component.validateFile).toHaveBeenCalledWith(file);
    expect(component.uploadResume).not.toHaveBeenCalled();
  });
});
