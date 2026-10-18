import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'secureLinks',
  standalone: true
})
export class SecureLinksPipe implements PipeTransform {
  transform(value: string | undefined | null): string {
    if (!value) return '';

    // Regex to find <a ... target="_blank" ...>
    // Robust logic:
    // 1. If it has target="_blank", check for existing rel.
    // 2. If no rel, add rel="noopener noreferrer".
    // 3. If rel exists, append "noopener noreferrer" to it.

    return value.replace(/<a\s+([^>]*?)target=["']_blank["']([^>]*?)>/gi, (match, p1, p2) => {
        if (/rel=["'][^"']*["']/i.test(match)) {
            // Append to existing rel
            return match.replace(/rel=["']([^"']*)["']/i, 'rel="$1 noopener noreferrer"');
        }
        // Insert new rel
        return `<a ${p1}target="_blank" rel="noopener noreferrer"${p2}>`;
    });
  }
}
