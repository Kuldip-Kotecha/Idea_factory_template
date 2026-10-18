import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule, RouterModule, BrandLogoComponent],
  templateUrl: './about.component.html'
})
export class AboutComponent {}
