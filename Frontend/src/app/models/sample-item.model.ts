export interface SampleItem {
    id?: number;
    title: string;
    description?: string;
    status: 'DRAFT' | 'ACTIVE' | 'ARCHIVED';
    createdAt?: string;
    updatedAt?: string;
}
