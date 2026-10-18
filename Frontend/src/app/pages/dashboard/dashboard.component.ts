import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AppStore } from '../../services/app.store';
import { SampleItemService } from '../../services/sample-item.service';
import { SampleItem } from '../../models/sample-item.model';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    protected readonly store = inject(AppStore);
    private sampleItemService = inject(SampleItemService);

    items = signal<SampleItem[]>([]);
    isLoadingItems = signal<boolean>(true);
    totalItems = signal<number>(0);

    ngOnInit(): void {
        this.loadSampleItems();
    }

    loadSampleItems(): void {
        this.isLoadingItems.set(true);
        this.sampleItemService.getItems(0, 5).subscribe({
            next: (page) => {
                this.items.set(page.content);
                this.totalItems.set(page.totalElements);
                this.isLoadingItems.set(false);
            },
            error: () => {
                this.isLoadingItems.set(false);
            }
        });
    }
}
