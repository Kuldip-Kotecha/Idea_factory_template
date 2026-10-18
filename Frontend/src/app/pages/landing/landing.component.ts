import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { HealthService } from '../../services/health.service';
import { BrandLogoComponent } from '../../components/brand-logo/brand-logo.component';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [RouterLink, CommonModule, NgOptimizedImage, BrandLogoComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './landing.component.html',
    styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {
    systemStatus: 'CHECKING' | 'ONLINE' | 'DEGRADED' | 'OFFLINE' = 'CHECKING';
    constructor(private healthService: HealthService, private cdr: ChangeDetectorRef) { }

    ngOnInit() {
        this.healthService.checkHealth().subscribe({
            next: (response) => {
                if (response && response.status === 'UP') this.systemStatus = 'ONLINE';
                else if (response && response.status === 'DEGRADED') this.systemStatus = 'DEGRADED';
                else this.systemStatus = 'OFFLINE';
                this.cdr.markForCheck();
            },
            error: (err) => {
                this.systemStatus = 'OFFLINE';
                this.cdr.markForCheck();
            }
        });
    }
}
