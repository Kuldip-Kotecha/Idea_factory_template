export interface User {
    id?: number;
    email: string;
    name?: string;
    role: string;
    profilePicture?: string;
    isActive?: boolean;
    authProvider?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface UserPageResponse {
    content: User[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}
