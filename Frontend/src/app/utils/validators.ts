export function isStrongPassword(password: string): { valid: boolean; message?: string } {
    if (!password) {
        return { valid: false, message: 'Password is required.' };
    }

    if (password.length < 8) {
        return { valid: false, message: 'Password must be at least 8 characters long.' };
    }

    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    if (!hasUpperCase) {
        return { valid: false, message: 'Password must contain at least one uppercase letter.' };
    }

    if (!hasLowerCase) {
        return { valid: false, message: 'Password must contain at least one lowercase letter.' };
    }

    if (!hasNumber) {
        return { valid: false, message: 'Password must contain at least one number.' };
    }

    if (!hasSpecialChar) {
        return { valid: false, message: 'Password must contain at least one special character.' };
    }

    return { valid: true };
}
