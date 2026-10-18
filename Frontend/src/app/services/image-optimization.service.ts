import { Injectable } from '@angular/core';

/**
 * Image optimization service for lazy loading and responsive images
 * Implements ngSrc and priority loading strategies
 */
@Injectable({
  providedIn: 'root'
})
export class ImageOptimizationService {
  
  /**
   * Directive for lazy loading images with IntersectionObserver
   * Usage: <img appLazyLoad [src]="imagePath" />
   */
  preloadCriticalImages(imagePaths: string[]): void {
    imagePaths.forEach(path => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.as = 'image';
      link.href = path;
      document.head.appendChild(link);
    });
  }

  /**
   * Generate responsive image srcset
   * For Next-Gen image formats (WebP with fallback)
   */
  generateSrcSet(basePath: string, sizes: number[] = [320, 640, 1280]): string {
    return sizes
      .map(size => `${basePath}?w=${size} ${size}w`)
      .join(', ');
  }

  /**
   * Optimize lazy loading with native loading attribute
   * Replaces IntersectionObserver for simple use cases
   */
  getLazyLoadingConfig() {
    return {
      loading: 'lazy',
      decoding: 'async'
    };
  }
}
