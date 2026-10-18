import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./pages/landing/landing.component').then(m => m.LandingComponent),
        pathMatch: 'full'
    },
    {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'signup',
        loadComponent: () => import('./pages/signup/signup.component').then(m => m.SignupComponent)
    },
    {
        path: 'email-sent',
        loadComponent: () => import('./pages/signup/email-sent.component').then(m => m.EmailSentComponent)
    },
    {
        path: 'verify-email',
        loadComponent: () => import('./pages/signup/verify-email.component').then(m => m.VerifyEmailComponent)
    },
    {
        path: 'forgot-password',
        loadComponent: () => import('./pages/login/forgot-password.component').then(m => m.ForgotPasswordComponent)
    },
    {
        path: 'reset-password',
        loadComponent: () => import('./pages/login/reset-password.component').then(m => m.ResetPasswordComponent)
    },
    {
        path: 'faq',
        loadComponent: () => import('./pages/faq-page/faq-page.component').then(m => m.FaqPageComponent)
    },
    {
        path: 'about',
        loadComponent: () => import('./pages/about/about.component').then(m => m.AboutComponent)
    },
    {
        path: 'contact',
        loadComponent: () => import('./pages/contact/contact.component').then(m => m.ContactComponent)
    },
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        canActivateChild: [authGuard],
        children: [
            { 
                path: 'dashboard', 
                loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) 
            },
            { 
                path: 'profile', 
                loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent) 
            },
            { 
                path: 'users', 
                loadComponent: () => import('./pages/users/users.component').then(m => m.UsersComponent),
                canActivate: [adminGuard]
            }
        ]
    },
    {
        path: '**',
        redirectTo: ''
    }
];
